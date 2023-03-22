package cn.zpl.spider.on.bilibili.controller;

import cn.zpl.pojo.DownloadDTO;
import cn.zpl.pojo.SynchronizeLock;
import cn.zpl.spider.on.bilibili.common.BilibiliConfigParams;
import cn.zpl.thread.OneFileOneThread;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.DownloadTools;
import cn.zpl.util.URLConnectionTool;
import com.google.gson.JsonElement;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

public class EmojiDownload {

    @Test
    public void domain() {
        List<DownloadDTO> dtoList = new ArrayList<>();
        String emojiJson = URLConnectionTool.getMethod2Str("https://api.bilibili.com/x/emote/user/panel/web?business=reply", "Cookie:SESSDATA=4bd4ab82%2C1694352930%2Cedf05%2A31;");
        JsonElement packages = CommonIOUtils.getFromJson2(emojiJson, "data-packages");
        if (packages.isJsonArray()) {
            for (JsonElement jsonElement : packages.getAsJsonArray()) {
                List<String> path = new ArrayList<>();
                path.add("c:");
                path.add("users");
                path.add("zpl");
                path.add("pictures");
                path.add("B站表情包11");
                path.add(CommonIOUtils.getFromJson2Str(jsonElement, "text"));
                if (CommonIOUtils.getFromJson2Str(jsonElement, "text").contains("颜文字")) {
                    continue;
                }
                JsonElement emote = CommonIOUtils.getFromJson2(jsonElement, "emote");
                if (emote.isJsonArray()) {
                    for (JsonElement element : emote.getAsJsonArray()) {
                        String title = CommonIOUtils.getFromJson2Str(element, "text");
                        String url = CommonIOUtils.getFromJson2Str(element, "url");
                        if (!url.contains(".")) {
                            continue;
                        }
                        String type = null;
                        try {
                            type = url.substring(url.lastIndexOf("."));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        DownloadDTO dto = new DownloadDTO();
                        dto.setUrl(url);
                        dto.setSavePath(CommonIOUtils.makeFilePath(path, title + type));
                        dtoList.add(dto);
                    }
                }
            }
        }
//        Elements elements = CommonIOUtils.getElementsFromStr(CommonIOUtils.readTxt("C:\\Users\\zpl\\Documents\\xhr.html", "utf-8").toString(), "img");
//        List<String> path = new ArrayList<>();
//        for (Element element : elements) {
//            String url = element.attr("src");
//            url = url.substring(0, url.indexOf("@"));
//            String type = url.substring(url.lastIndexOf("."));
//            String title = element.attr("title");
//            DownloadDTO dto = new DownloadDTO();
//            dto.setUrl(url);
//            path.clear();
//            path.add("c:");
//            path.add("users");
//            path.add("zpl");
//            path.add("pictures");
//            path.add("xhr");
//            dto.setSavePath(CommonIOUtils.makeFilePath(path, title + type));
//            dtoList.add(dto);
//        }
        SynchronizeLock lock = new SynchronizeLock();
        DownloadTools tools = DownloadTools.getInstance(10);
        dtoList.forEach(downloadDTO -> {
            downloadDTO.setSynchronizeLock(lock);
            downloadDTO.setProxy(true);
            tools.ThreadExecutorAdd(new OneFileOneThread(downloadDTO));
        });
        tools.shutdown();
    }

}
