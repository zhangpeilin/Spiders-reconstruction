package cn.zpl.common.bean;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.sql.Blob;
import io.swagger.annotations.ApiModel;

/**
 * <p>
 * 
 * </p>
 *
 * @author zpl
 * @since 2024-08-15
 */
@TableName("pixiv_pictures")
@ApiModel(value = "PixivPictures对象", description = "")
public class PixivPictures implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String illustTitle;

    private String url;

    private String userId;

    private String userName;

    private String tags;

    private Integer width;

    private Integer height;

    private Blob imgObject;

    private Integer bookmarkCount;

    private String savePath;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getIllustTitle() {
        return illustTitle;
    }

    public void setIllustTitle(String illustTitle) {
        this.illustTitle = illustTitle;
    }
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }
    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }
    public Blob getImgObject() {
        return imgObject;
    }

    public void setImgObject(Blob imgObject) {
        this.imgObject = imgObject;
    }
    public Integer getBookmarkCount() {
        return bookmarkCount;
    }

    public void setBookmarkCount(Integer bookmarkCount) {
        this.bookmarkCount = bookmarkCount;
    }
    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    @Override
    public String toString() {
        return "PixivPictures{" +
            "id=" + id +
            ", illustTitle=" + illustTitle +
            ", url=" + url +
            ", userId=" + userId +
            ", userName=" + userName +
            ", tags=" + tags +
            ", width=" + width +
            ", height=" + height +
            ", imgObject=" + imgObject +
            ", bookmarkCount=" + bookmarkCount +
            ", savePath=" + savePath +
        "}";
    }
}
