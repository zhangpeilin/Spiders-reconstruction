package cn.zpl.common.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author zpl
 * @since 2022-04-02
 */
@ApiModel(value = "VideoInfo对象", description = "")
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VideoInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String videoId;

    private String m3u8FilePath;

    private String savePath;

    @ApiModelProperty("存储下载的url地址，如果是分段的，每个分段存储一次，最后视频合并后存储一个总的，url地址为空，parent_id为空")
    private String url;

    private String videoName;

    private String savedLocalName;

    private String dataTime;

    private String website;

    private String length;

    private String command;

    private String hold1;

    private String timeLength;

    private String fileList;

    private String title;

    private Integer pageCount;

    private String localPath;

    private String ownerName;

    private String downloadDate;

    private String aid;

    private String bid;

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }
    @Deprecated
    public String getSavedLocalName() {
        return savedLocalName;
    }

    @Deprecated
    public void setSavedLocalName(String savedLocalName) {
        this.savedLocalName = savedLocalName;
    }
    public String getDataTime() {
        return dataTime;
    }

    public void setDataTime(String dataTime) {
        this.dataTime = dataTime;
    }
    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
    public String getHold1() {
        return hold1;
    }

    public void setHold1(String hold1) {
        this.hold1 = hold1;
    }
    public String getTimeLength() {
        return timeLength;
    }

    public void setTimeLength(String timeLength) {
        this.timeLength = timeLength;
    }
    public String getFileList() {
        return fileList;
    }

    public void setFileList(String fileList) {
        this.fileList = fileList;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }
    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }
    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
    public String getDownloadDate() {
        return downloadDate;
    }

    public void setDownloadDate(String downloadDate) {
        this.downloadDate = downloadDate;
    }
    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    @Override
    public String toString() {
        return "VideoInfo{" +
            "videoId=" + videoId +
            ", url=" + url +
            ", videoName=" + videoName +
            ", savedLocalName=" + savedLocalName +
            ", dataTime=" + dataTime +
            ", website=" + website +
            ", length=" + length +
            ", command=" + command +
            ", hold1=" + hold1 +
            ", timeLength=" + timeLength +
            ", fileList=" + fileList +
            ", title=" + title +
            ", pageCount=" + pageCount +
            ", localPath=" + localPath +
            ", ownerName=" + ownerName +
            ", downloadDate=" + downloadDate +
            ", aid=" + aid +
        "}";
    }
}
