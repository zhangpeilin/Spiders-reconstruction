package cn.zpl.util;

import cn.zpl.common.bean.Bika;
import cn.zpl.common.bean.NasPic;
import cn.zpl.common.bean.Page;
import cn.zpl.common.bean.RestResponse;
import cn.zpl.config.UrlConfig;
import cn.zpl.thirdParty.ObjectTypeAdapterRewrite;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Data
@Slf4j
@Component
@EnableConfigurationProperties({UrlConfig.class, CommonProperties.class})
public class CrudTools {
    @Resource
    UrlConfig config;

    @Resource
    private RestTemplate restTemplate;

    public void init() {
        for (HttpMessageConverter<?> messageConverter : restTemplate.getMessageConverters()) {
            if (messageConverter instanceof GsonHttpMessageConverter) {
                Gson gson = new GsonBuilder().registerTypeAdapter(new TypeToken<RestResponse>() {
                }.getType(), new ObjectTypeAdapterRewrite()).create();
                ((GsonHttpMessageConverter) messageConverter).setGson(gson);
            }
        }
    }

    @Async("BikaAsync")
    public void AsyncApiSave(Object bean){
        commonApiSave(bean);
    }


    @SuppressWarnings("unchecked")
    public RestResponse commonApiSave(Object bean) {

        String entity;
        if (bean instanceof List) {
            List<Object> list = (List<Object>) bean;
            entity = list.get(0).getClass().getSimpleName();
        } else {
            entity = bean.getClass().getSimpleName();

        }
        HashMap<String, Object> params = new HashMap<>();
        params.put("entity", entity);
        params.put("data", bean);
        ResponseEntity<RestResponse> responseEntity = restTemplate.postForEntity(config.getCommonSaveUrl(), params, RestResponse.class);
        log.debug(String.valueOf(responseEntity));
        return responseEntity.getBody();

    }

    public <T> List<T> commonApiQueryBySql(String sql, Class<T> tClass) {
        if (!sql.startsWith("sql:")) {
            sql = "sql:" + sql;
        }
        return commonApiQuery(sql, null, tClass);
    }

    public RestResponse commonDelete(String sql, List<LinkedHashMap<String, Object>> paramObjects) {
        Map<String, Object> requestMap = new HashMap<>();
        List<String> params = paramObjects.stream().map(soMap -> soMap.values().stream().map(String::valueOf).collect(Collectors.joining(","))).collect(Collectors.toList());
        requestMap.put("sql", sql);
        requestMap.put("params", params);
        ResponseEntity<RestResponse> forEntity = restTemplate.postForEntity(config.getCommonDelete(), requestMap, RestResponse.class);
        return forEntity.getBody();
    }

    public <R> List<R> commonApiQuery(String condition, Class<R> tClass) {
        return commonApiQuery(condition, null, tClass, new Page(1, 20));
    }


    public <R> List<R> commonApiQuery(String condition, String fetchProperties, Class<R> tClass) {
        return commonApiQuery(condition, fetchProperties, tClass, new Page(1, 20));
    }

    public <R> List<R> commonApiQuery(String condition, String fetchProperties, Class<R> tClass, Page page) {
        String entity = tClass.getSimpleName();
        if (StringUtils.isEmpty(fetchProperties)) {
            fetchProperties = config.getNothing();
        }
        if (StringUtils.isEmpty(condition)) {
            condition = config.getNothing();
        }
        String requestUrl = formatRequestUrl(config.getCommonQueryUrl(), entity, fetchProperties, condition, page);
        log.debug("请求url：-->{}", requestUrl);
        ResponseEntity<RestResponse> forEntity = restTemplate.getForEntity(requestUrl, RestResponse.class);
        RestResponse response = forEntity.getBody();
        if (response == null || !response.isSuccess()) {
            return Collections.emptyList();
        } else {
            return Objects.requireNonNull(response).getList(tClass);
        }
    }

    private String formatRequestUrl(String url, Object... args) {
        return String.format(url, args);
    }

    public static <T> boolean commonApiDelete(String condition, Class<T> tClass) {

        return RestResponse.ok().isSuccess();
    }

}
