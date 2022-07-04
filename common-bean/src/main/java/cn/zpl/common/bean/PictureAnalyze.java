package cn.zpl.common.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author zpl
 * @since 2022-05-18
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(value = "PictureAnalyze对象", description = "")
public class PictureAnalyze implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String path;

    private byte[] baiduResult;

    private byte[] tencentResult;

    private String baiduJsonResult;

    private String tencentJsonResult;

    private String qualityResult;
}
