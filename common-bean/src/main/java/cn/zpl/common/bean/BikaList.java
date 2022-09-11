package cn.zpl.common.bean;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author zpl
 * @since 2022-08-20
 */
@ApiModel(value = "BikaList对象", description = "")
public class BikaList implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String title;

    private String description;

    private String author;

    private String categories;

    private String tags;

    private Integer pagesCount;

    private Integer epsCount;

    private Integer finished;

    private String updatedAt;

    private String createdAt;

    private Integer viewsCount;

    private Integer likesCount;

    private Integer isFavourite;

    private Integer isLiked;

    private Integer commentsCount;

    private String downloadedAt;

    private Integer realPagesCount;

    private Integer isDeleted;

    private String localPath;

    private String updateTime;

    private String uuidStr;

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
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }
    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
    public Integer getPagesCount() {
        return pagesCount;
    }

    public void setPagesCount(Integer pagesCount) {
        this.pagesCount = pagesCount;
    }
    public Integer getEpsCount() {
        return epsCount;
    }

    public void setEpsCount(Integer epsCount) {
        this.epsCount = epsCount;
    }
    public Integer getFinished() {
        return finished;
    }

    public void setFinished(Integer finished) {
        this.finished = finished;
    }
    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    public Integer getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(Integer viewsCount) {
        this.viewsCount = viewsCount;
    }
    public Integer getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(Integer likesCount) {
        this.likesCount = likesCount;
    }
    public Integer getIsFavourite() {
        return isFavourite;
    }

    public void setIsFavourite(Integer isFavourite) {
        this.isFavourite = isFavourite;
    }
    public Integer getIsLiked() {
        return isLiked;
    }

    public void setIsLiked(Integer isLiked) {
        this.isLiked = isLiked;
    }
    public Integer getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(Integer commentsCount) {
        this.commentsCount = commentsCount;
    }
    public String getDownloadedAt() {
        return downloadedAt;
    }

    public void setDownloadedAt(String downloadedAt) {
        this.downloadedAt = downloadedAt;
    }
    public Integer getRealPagesCount() {
        return realPagesCount;
    }

    public void setRealPagesCount(Integer realPagesCount) {
        this.realPagesCount = realPagesCount;
    }
    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    @Override
    public String toString() {
        return "BikaList{" +
            "id=" + id +
            ", title=" + title +
            ", description=" + description +
            ", author=" + author +
            ", categories=" + categories +
            ", tags=" + tags +
            ", pagesCount=" + pagesCount +
            ", epsCount=" + epsCount +
            ", finished=" + finished +
            ", updatedAt=" + updatedAt +
            ", createdAt=" + createdAt +
            ", viewsCount=" + viewsCount +
            ", likesCount=" + likesCount +
            ", isFavourite=" + isFavourite +
            ", isLiked=" + isLiked +
            ", commentsCount=" + commentsCount +
            ", downloadedAt=" + downloadedAt +
            ", realPagesCount=" + realPagesCount +
            ", isDeleted=" + isDeleted +
            ", localPath=" + localPath +
        "}";
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getUuidStr() {
        return uuidStr;
    }

    public void setUuidStr(String uuidStr) {
        this.uuidStr = uuidStr;
    }
}
