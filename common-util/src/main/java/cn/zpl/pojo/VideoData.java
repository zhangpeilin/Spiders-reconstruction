package cn.zpl.pojo;

import lombok.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Data
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
    private final List<String> partList = new ArrayList<>();
    private DownloadDTO video;
    private DownloadDTO audio;

    public File GetTmpSaveDirectory(){
        if (!partList.isEmpty()) {
            return new File(partList.get(0)).getParentFile();
        }
        return tmpSavePath;
    }
}
