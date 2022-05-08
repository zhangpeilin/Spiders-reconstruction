package cn.zpl.pojo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideoData {

    private String videoId;
    private String desSavePath;
    private String desSaveName;
    private String tmpSaveName;
    private File tmpSavePath;
    private String webSite;
    private String timeLength;
    private long length;
    private String hold1;
    private Integer pageCount;
    private List<String> partList = new ArrayList<>();
    private DownloadDTO video;
    private DownloadDTO audio;

    public String getDesSavePath() {
        return desSavePath;
    }

    public void setDesSavePath(String desSavePath) {
        this.desSavePath = desSavePath;
    }

    public String getDesSaveName() {
        return desSaveName;
    }

    public void setDesSaveName(String desSaveName) {
        this.desSaveName = desSaveName;
    }

    public String getTmpSaveName() {
        return tmpSaveName;
    }

    public void setTmpSaveName(String tmpSaveName) {
        this.tmpSaveName = tmpSaveName;
    }

    public List<String> getPartList() {
        return partList;
    }

    public File GetTmpSaveDirectory(){
        if (!partList.isEmpty()) {
            return new File(partList.get(0)).getParentFile();
        }
        return tmpSavePath;
    }

    public File getTmpSavePath() {
        return tmpSavePath;
    }

    public void setTmpSavePath(File tmpSavePath) {
        this.tmpSavePath = tmpSavePath;
    }

    public String getWebSite() {
        return webSite;
    }

    public void setWebSite(String webSite) {
        this.webSite = webSite;
    }

    public String getTimeLength() {
        return timeLength;
    }

    public void setTimeLength(String timeLength) {
        this.timeLength = timeLength;
    }

    public String getHold1() {
        return hold1;
    }

    public void setHold1(String hold1) {
        this.hold1 = hold1;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public DownloadDTO getVideo() {
        return video;
    }

    public void setVideo(DownloadDTO video) {
        this.video = video;
    }

    public DownloadDTO getAudio() {
        return audio;
    }

    public void setAudio(DownloadDTO audio) {
        this.audio = audio;
    }
}
