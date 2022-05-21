package cn.zpl.common.bean;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author zpl
 * @since 2022-05-18
 */
@Data
@ApiModel(value = "PictureAnalyze对象", description = "")
public class PictureAnalyze implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String path;

    private byte[] baiduResult;

    private byte[] tencentResult;

    private String baiduJsonResult;

    private String tencentJsonResult;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    @Override
    public String toString() {
        return "PictureAnalyze{" +
            "id=" + id +
            ", path=" + path +
            ", baiduResult=" + baiduResult +
            ", tencentResult=" + tencentResult +
        "}";
    }
}
