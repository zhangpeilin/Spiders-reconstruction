package cn.zpl.util;

import cn.zpl.common.bean.Bika;
import cn.zpl.common.bean.ExceptionList;
import cn.zpl.common.bean.RestResponse;
import cn.zpl.common.bean.VideoInfo;
import cn.zpl.config.UrlConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Data
@Slf4j
public class CrudTools<T> {

    private static RestTemplate restTemplate;

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
        ResponseEntity<VideoInfo> forEntity = restTemplate.getForEntity(UrlConfig.getVideoInfoById + id, VideoInfo.class, id);
        log.debug(String.valueOf(forEntity));
        return forEntity.getBody();
    }

    public static ExceptionList getExceptionListById(String id) {
        ResponseEntity<ExceptionList> forEntity = restTemplate.getForEntity(UrlConfig.getExceptionListById + id, ExceptionList.class, id);
        log.debug(String.valueOf(forEntity));
        return forEntity.getBody();
    }



}
