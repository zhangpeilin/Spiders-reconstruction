package cn.zpl.util;

import cn.zpl.common.bean.Bika;
import cn.zpl.common.bean.Ehentai;
import cn.zpl.common.bean.PictureAnalyze;
import cn.zpl.common.bean.RestResponse;
import cn.zpl.common.bean.VideoInfo;
import cn.zpl.config.UrlConfig;
import cn.zpl.thirdParty.ObjectTypeAdapterRewrite;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Slf4j
public class CrudTools<T> {

    private static RestTemplate restTemplate = new RestTemplate();
    static {
        for (HttpMessageConverter<?> messageConverter : restTemplate.getMessageConverters()) {
            if (messageConverter instanceof GsonHttpMessageConverter) {
                Gson gson = new GsonBuilder().registerTypeAdapter(new TypeToken<RestResponse>(){}.getType(), new ObjectTypeAdapterRewrite()).create();
                ((GsonHttpMessageConverter) messageConverter).setGson(gson);
            }
        }
    }

    public static RestResponse saveBika(Bika bika) {
        ResponseEntity<RestResponse> responseEntity = restTemplate.postForEntity(UrlConfig.saveOrUpdateBika, bika, RestResponse.class);
        log.debug(String.valueOf(responseEntity));
        return responseEntity.getBody();
    }

    public static RestResponse saveVideoInfo(VideoInfo videoInfo) {
        ResponseEntity<RestResponse> responseEntity = restTemplate.postForEntity(UrlConfig.saveOrUpdateVideoInfo, videoInfo, RestResponse.class);
        log.debug(String.valueOf(responseEntity));
        return responseEntity.getBody();
    }

    public RestResponse commonSave(T data) {
        ResponseEntity<RestResponse> responseEntity = restTemplate.postForEntity(UrlConfig.saveOrUpdateVideoInfo, data, RestResponse.class);
        log.debug(String.valueOf(responseEntity));
        return responseEntity.getBody();
    }

    public static VideoInfo getVideoById(String id) {
//        restTemplate.setMessageConverters(Collections.singletonList(new FastJsonHttpMessageConverter()));
        ResponseEntity<VideoInfo> forEntity = restTemplate.getForEntity(UrlConfig.getVideoInfoById + id, VideoInfo.class, id);
        log.debug(String.valueOf(forEntity));
        return forEntity.getBody();
    }

    public static RestResponse getExceptionListById(String id) {
//        restTemplate.setMessageConverters(Collections.singletonList(new FastJsonHttpMessageConverter()));
        ResponseEntity<RestResponse> forEntity = restTemplate.getForEntity(UrlConfig.getExceptionListById + id, RestResponse.class, id);
        log.debug(String.valueOf(forEntity));
        return forEntity.getBody();
    }

    public static RestResponse saveEhentai(Ehentai ehentai) {
        ResponseEntity<RestResponse> responseEntity = restTemplate.postForEntity(UrlConfig.saveOrUpdateEhentai, ehentai, RestResponse.class);
        log.debug(String.valueOf(responseEntity));
        return responseEntity.getBody();
    }

    public static RestResponse commonApiSave(Object bean) {
        String entity = bean.getClass().getSimpleName();
        ResponseEntity<RestResponse> restResponseResponseEntity = restTemplate.postForEntity("http://localhost:8080/common/dao/api/save/" + entity, bean, RestResponse.class);
        log.debug(String.valueOf(restResponseResponseEntity));
        return restResponseResponseEntity.getBody();

    }

    public static <T> List<T> commonApiQueryBySql(String sql, Class<T> tClass) {
        return RestResponse.ok().getList(tClass);
    }

    public static <T> List<T> commonApiQuery(String condition, String[] fetchProperties, Class<T> tClass) {
        String entity = tClass.getSimpleName();
        ResponseEntity<RestResponse> forEntity = restTemplate.getForEntity("http://localhost:8080/common/dao/api/query/" + entity + "?", RestResponse.class);
        return Objects.requireNonNull(forEntity.getBody()).getList(tClass);
    }

    public static <T> boolean commonApiDelete(String condition, Class<T> tClass) {
        return RestResponse.ok().isSuccess();
    }

    public static RestResponse savePA(PictureAnalyze pictureAnalyze) {
        ResponseEntity<RestResponse> responseEntity = restTemplate.postForEntity(UrlConfig.saveOrUpdatePA, pictureAnalyze, RestResponse.class);
        log.debug(String.valueOf(responseEntity));
        return responseEntity.getBody();
    }

    public static RestResponse getPAById(String id) {
        ResponseEntity<RestResponse> forEntity = restTemplate.getForEntity(UrlConfig.getPAById + id, RestResponse.class);
        return forEntity.getBody();
    }

    public static RestResponse queryListWithKey(String key) {
        ResponseEntity<RestResponse> forEntity = restTemplate.getForEntity(UrlConfig.queryPAList + key, RestResponse.class);
        return forEntity.getBody();
    }

    public static RestResponse queryPAListByCondition(String condition) {
        ResponseEntity<RestResponse> forEntity = restTemplate.getForEntity(UrlConfig.queryPAListByCondition + condition, RestResponse.class);
        return forEntity.getBody();
    }


}
