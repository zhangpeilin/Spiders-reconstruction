package cn.zpl;

import cn.zpl.util.CommonIOUtils;

public class ExJson {
    public static void main(String[] args) {
        String jsCode = "<script>\n" +
                "            window.__playinfo__ = {\n" +
                "                \"code\": 0,\n" +
                "                \"message\": \"0\",\n" +
                "                \"ttl\": 1,\n" +
                "                \"data\": {\n" +
                "                    \"from\": \"local\",\n" +
                "                    \"result\": \"suee\",\n" +
                "                    \"message\": \"\",\n" +
                "                    \"quality\": 80,\n" +
                "                    \"format\": \"flv\",\n" +
                "                    \"timelength\": 1329806,\n" +
                "                    \"accept_format\": \"flv,flv720,flv480,mp4\",\n" +
                "                    \"accept_description\": [\"高清 1080P\", \"高清 720P\", \"清晰 480P\", \"流畅 360P\"],\n" +
                "                    \"accept_quality\": [80, 64, 32, 16],\n" +
                "                    \"video_codecid\": 7,\n" +
                "                    \"seek_param\": \"start\"\n" +
                "            }\n" +
                "            },\n" +
                "            window.sadfasfasdf = {\n" +
                "                \"code\": 0,\n" +
                "                \"message\": \"0\",\n" +
                "                \"ttl\": 1,\n" +
                "                \"data\": {\n" +
                "                    \"from\": \"local\",\n" +
                "                    \"result\": \"suee\",\n" +
                "                    \"message\": \"\",\n" +
                "                    \"quality\": 80,\n" +
                "                    \"format\": \"flv\",\n" +
                "                    \"timelength\": 1329806,\n" +
                "                    \"accept_format\": \"flv,flv720,flv480,mp4\",\n" +
                "                    \"accept_description\": [\"高清 1080P\", \"高清 720P\", \"清晰 480P\", \"流畅 360P\"],\n" +
                "                    \"accept_quality\": [80, 64, 32, 16],\n" +
                "                    \"video_codecid\": 7,\n" +
                "                    \"seek_param\": \"start\"\n" +
                "            }\n" +
                "            }"+
                "        </script>";
        System.out.println(CommonIOUtils.getValue2(jsCode, "window.__playinfo__"));
    }
}
