package cn.zpl.util.m3u8;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class M3U8 {
        private String basepath;
        private String fpath;
        private List<M3U8Ts> tsList = new ArrayList<>();
        private List<M3U8> m3u8List = new ArrayList<>();

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

        public String getBasepath() {
            return basepath;
        }

        public void setBasepath(String basepath) {
            this.basepath = basepath;
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

        @Override
        public String toString() {
            return JSON.toJSONString(this);
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