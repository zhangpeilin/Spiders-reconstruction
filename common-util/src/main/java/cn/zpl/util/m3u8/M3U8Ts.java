package cn.zpl.util.m3u8;


import cn.zpl.util.CommonIOUtils;

public class M3U8Ts {
        private String file;
        private float seconds;

        public String getSubUrl() {
            return subUrl;
        }

        public void setSubUrl(String subUrl) {
            this.subUrl = subUrl;
        }

        private String subUrl;

        public M3U8Ts(String file, float seconds) {
            this.file = CommonIOUtils.filterFileName(file);
            this.subUrl = file;
            this.seconds = seconds;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public float getSeconds() {
            return seconds;
        }

        public void setSeconds(float seconds) {
            this.seconds = seconds;
        }

        @Override
        public String toString() {
            return file + " (" + seconds + "sec)";
        }
    }