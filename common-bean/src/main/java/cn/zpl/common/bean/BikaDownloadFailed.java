package cn.zpl.common.bean;

import io.swagger.annotations.ApiModel;

import java.io.Serializable;

/**
 * <p>
 * bika下载失败记录表
 * </p>
 *
 * @author zpl
 * @since 2022-07-06
 */
@ApiModel(value = "BikaDownloadFailed对象", description = "bika下载失败记录表")
public class BikaDownloadFailed implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String title;

    private String error;

    private String downloadAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
    public String getDownloadAt() {
        return downloadAt;
    }

    public void setDownloadAt(String downloadAt) {
        this.downloadAt = downloadAt;
    }

    @Override
    public String toString() {
        return "BikaDownloadFailed{" +
            "id=" + id +
            ", title=" + title +
            ", error=" + error +
            ", downloadAt=" + downloadAt +
        "}";
    }
}
