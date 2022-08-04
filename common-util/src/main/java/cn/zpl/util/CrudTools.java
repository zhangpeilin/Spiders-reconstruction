package cn.zpl.util;

import cn.zpl.common.bean.Bika;
import cn.zpl.common.bean.RestResponse;
import cn.zpl.config.UrlConfig;
import cn.zpl.thirdParty.ObjectTypeAdapterRewrite;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Arrays;
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

//    public RestResponse saveBika(Bika bika) {
//        ResponseEntity<RestResponse> responseEntity = restTemplate.postForEntity(config.getSaveOrUpdateBika(), bika, RestResponse.class);
//        log.debug(String.valueOf(responseEntity));
//        return responseEntity.getBody();
//    }

//    public RestResponse saveVideoInfo(VideoInfo videoInfo) {
//        ResponseEntity<RestResponse> responseEntity = restTemplate.postForEntity(config.getSaveOrUpdateVideoInfo(), videoInfo, RestResponse.class);
//        log.debug(String.valueOf(responseEntity));
//        return responseEntity.getBody();
//    }

    public RestResponse commonSave(Object data) {
        ResponseEntity<RestResponse> responseEntity = restTemplate.postForEntity(config.getCommonSaveUrl(), data, RestResponse.class);
        log.debug(String.valueOf(responseEntity));
        return responseEntity.getBody();
    }

//    public VideoInfo getVideoById(String id) {
////        restTemplate.setMessageConverters(Collections.singletonList(new FastJsonHttpMessageConverter()));
//        ResponseEntity<VideoInfo> forEntity = restTemplate.getForEntity(config.getGetVideoInfoById() + id, VideoInfo.class, id);
//        log.debug(String.valueOf(forEntity));
//        return forEntity.getBody();
//    }

//    public RestResponse getExceptionListById(String id) {
////        restTemplate.setMessageConverters(Collections.singletonList(new FastJsonHttpMessageConverter()));
//        ResponseEntity<RestResponse> forEntity = restTemplate.getForEntity(config.getGetexceptionlistbyid() + id, RestResponse.class, id);
//        log.debug(String.valueOf(forEntity));
//        return forEntity.getBody();
//    }

//    public RestResponse saveEhentai(Ehentai ehentai) {
//        ResponseEntity<RestResponse> responseEntity = restTemplate.postForEntity(config.getSaveOrUpdateEhentai(), ehentai, RestResponse.class);
//        log.debug(String.valueOf(responseEntity));
//        return responseEntity.getBody();
//    }

    public static RestResponse commonApiSave(Object bean) {
        String entity = bean.getClass().getSimpleName();
        ResponseEntity<RestResponse> restResponseResponseEntity = restTemplate.postForEntity("http://localhost:8080/common/dao/api/save/" + entity, bean, RestResponse.class);
        log.debug(String.valueOf(restResponseResponseEntity));
        return restResponseResponseEntity.getBody();

    }

    public static <T> List<T> commonApiQueryBySql(String sql, Class<T> tClass) {
        return RestResponse.ok().getList(tClass);
    }
//    public PictureAnalyze queryPA(String id){
//        List<PictureAnalyze> pictureAnalyzes = commonApiQuery("id=" + id, null, PictureAnalyze.class);
//        return pictureAnalyzes.get(0);
//    }

//    public VideoInfo queryVideoInfo(String id) {
//        return commonApiQuery("id=" + id, null, VideoInfo.class).get(0);
//    }

    public T commonApiQuery(String id, Class<T> tClass) {
        return commonApiQuery("id=" + id, null, tClass).get(0);
    }

    public List<Bika> queryAllBika() {
        return commonApiQuery("", null, Bika.class);
    }

    public List<T> queryAll(Class<T> tClass) {
        return commonApiQuery("", null, tClass);
    }

    public <R> List<R> commonApiQuery(String condition, String fetchProperties, Class<R> tClass, int... size) {
        String entity = tClass.getSimpleName();
        if (StringUtils.isEmpty(fetchProperties)) {
            fetchProperties = "*";
        }
        System.out.printf("http://localhost:8080/common/dao/api/query/%1$s?fetchProperties=[%2$s]&condition=[%3$s]&size=%4$s", entity, fetchProperties, "condition", "999");
//        String url = String.format("http://localhost:8080/common/dao/api/query/", "");
        String requestUrl = formatRequestUrl(config.getCommonQueryUrl(), entity, fetchProperties, condition, size);
        log.debug("请求url：-->{}", requestUrl);
        ResponseEntity<RestResponse> forEntity = restTemplate.getForEntity(requestUrl, RestResponse.class);
        return Objects.requireNonNull(forEntity.getBody()).getList(tClass);
    }

    private String formatRequestUrl(String url, Object... args) {
        return String.format(url, args);
    }

    public static <T> boolean commonApiDelete(String condition, Class<T> tClass) {
        return RestResponse.ok().isSuccess();
    }

//    public RestResponse savePA(PictureAnalyze pictureAnalyze) {
//        ResponseEntity<RestResponse> responseEntity = restTemplate.postForEntity(config.getSaveOrUpdatePA(), pictureAnalyze, RestResponse.class);
//        log.debug(String.valueOf(responseEntity));
//        return responseEntity.getBody();
//    }

//    public RestResponse getPAById(String id) {
//        ResponseEntity<RestResponse> forEntity = restTemplate.getForEntity(config.getGetPAById() + id, RestResponse.class);
//        return forEntity.getBody();
//    }

//    public RestResponse queryListWithKey(String key) {
//        ResponseEntity<RestResponse> forEntity = restTemplate.getForEntity(config.getQueryPAList() + key, RestResponse.class);
//        return forEntity.getBody();
//    }

//    public RestResponse queryPAListByCondition(String condition) {
//        ResponseEntity<RestResponse> forEntity = restTemplate.getForEntity(config.getQueryPAListByCondition() + condition, RestResponse.class);
//        return forEntity.getBody();
//    }


}
