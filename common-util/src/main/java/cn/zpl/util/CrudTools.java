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
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Data
@Slf4j
@Component
@EnableConfigurationProperties(UrlConfig.class)
public class CrudTools<T> {
    @Resource
    UrlConfig config;

    private static RestTemplate restTemplate = new RestTemplate();

    static {
        for (HttpMessageConverter<?> messageConverter : restTemplate.getMessageConverters()) {
            if (messageConverter instanceof GsonHttpMessageConverter) {
                Gson gson = new GsonBuilder().registerTypeAdapter(new TypeToken<RestResponse>() {
                }.getType(), new ObjectTypeAdapterRewrite()).create();
                ((GsonHttpMessageConverter) messageConverter).setGson(gson);
            }
        }
    }

    public static CrudTools<Object> getInstance(UrlConfig config) {
        CrudTools<Object> crudTools = new CrudTools<>();
        crudTools.setConfig(config);
        return crudTools;
    }

    public RestResponse commonSave(Object data) {
        ResponseEntity<RestResponse> responseEntity = restTemplate.postForEntity(config.getCommonSaveUrl(), data, RestResponse.class);
        log.debug(String.valueOf(responseEntity));
        return responseEntity.getBody();
    }


    @SuppressWarnings("unchecked")
    public static RestResponse commonApiSave(Object bean) {

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
        ResponseEntity<RestResponse> restResponseResponseEntity = restTemplate.postForEntity("http://localhost:8080/common/dao/api/save/", params, RestResponse.class);
        log.debug(String.valueOf(restResponseResponseEntity));
        return restResponseResponseEntity.getBody();

    }

    public <T> List<T> commonApiQueryBySql(String sql, Class<T> tClass) {
        return commonApiQuery(sql, null, tClass);
    }

    public T commonApiQuery(String id, Class<T> tClass) {
        return commonApiQuery("id=" + id, null, tClass).get(0);
    }

    public List<Bika> queryAllBika() {
        return commonApiQuery("", null, Bika.class);
    }

    public List<T> queryAll(Class<T> tClass) {
        return commonApiQuery("", null, tClass);
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
//        System.out.printf("http://localhost:8080/common/dao/api/query/%1$s?fetchProperties=[%2$s]&condition=[%3$s]&page=%4$s", entity, fetchProperties, "condition", page);
//        String url = String.format("http://localhost:8080/common/dao/api/query/", "");
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

    public static void main(String[] args) {
        NasPic pic = new NasPic();
        pic.setId("234234");
        pic.setUnitId("asfdas");
        commonApiSave(pic);
    }

}
