package cn.zpl.common.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author zpl
 * @since 2022-05-02
 */
@ApiModel(value = "ExceptionList对象", description = "")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExceptionList implements Serializable {

    private static final long serialVersionUID = 1L;

    private String videoId;

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    @Override
    public String toString() {
        return "ExceptionList{" +
            "videoId=" + videoId +
        "}";
    }
}
