package cn.zpl.util;

import cn.zpl.common.bean.Bika;
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

@Data
@Slf4j
public class CrudTools<T> {

    private static RestTemplate restTemplate = new RestTemplate();

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
        for (HttpMessageConverter<?> messageConverter : restTemplate.getMessageConverters()) {
            if (messageConverter instanceof GsonHttpMessageConverter) {
                Gson gson = new GsonBuilder().registerTypeAdapter(new TypeToken<RestResponse>(){}.getType(), new ObjectTypeAdapterRewrite()).create();
                ((GsonHttpMessageConverter) messageConverter).setGson(gson);
            }
        }
        ResponseEntity<RestResponse> forEntity = restTemplate.getForEntity(UrlConfig.getExceptionListById + id, RestResponse.class, id);
        log.debug(String.valueOf(forEntity));
        return forEntity.getBody();
    }


}
