package cn.zpl.util.m3u8;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class M3U8 {
    private String basePath;
    private String fpath;
    /**
     * 最后文件保存位置
     */
    private String fileSavePath;
    private List<M3U8Ts> tsList = new ArrayList<>();
    private List<M3U8> m3u8List = new ArrayList<>();

    /**
     * 加密key路径，可以是网络地址，也可以是本地地址
     */
    private String keyUrl;

    private String filePath;
    private String fileName;

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    private int quality;


    public String getFpath() {
        return fpath;
    }

    public void setFpath(String fpath) {
        this.fpath = fpath;
    }

    public List<M3U8> getM3u8List() {
        return m3u8List;
    }

    public void addM3u8(M3U8 m3u8) {
        if (this.m3u8List.size() == 1) {
            M3U8 already = m3u8List.remove(0);
            this.m3u8List.add(already.getQuality() > m3u8.getQuality() ? already : m3u8);
        } else {
            this.m3u8List.add(m3u8);
        }
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public List<M3U8Ts> getTsList() {
        return tsList;
    }

    public void setTsList(List<M3U8Ts> tsList) {
        this.tsList = tsList;
    }

    public void addTs(M3U8Ts ts) {
        this.tsList.add(ts);
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}