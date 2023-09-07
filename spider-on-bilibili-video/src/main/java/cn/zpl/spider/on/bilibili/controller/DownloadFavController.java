package cn.zpl.spider.on.bilibili.controller;

import cn.zpl.config.SpringContext;
import cn.zpl.spider.on.bilibili.BilibiliDownloadCore;
import cn.zpl.spider.on.bilibili.common.BilibiliCommonUtils;
import cn.zpl.spider.on.bilibili.common.BilibiliProperties;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.URLConnectionTool;
import cn.zpl.util.UrlContainer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class DownloadFavController {

    @Resource
    BilibiliCommonUtils utils;

    @Resource
    BilibiliProperties properties;

//    private static final String uid = "404412";
//    private static final String url = "https://api.bilibili.com/medialist/gateway/base/created?pn=1&ps=100&up_mid=" +
//            uid + "&is_space=0&jsonp=jsonp";

    @GetMapping("/downloadFav/{uid}/{gallery}")
    public String downloadFav(@PathVariable("uid") String uid, @PathVariable(value = "gallery", required = false) String gallery) {
        doTheOne(uid, gallery);
        return "success";
    }

    @GetMapping("/downloadFav/{uid}")
    public String downloadFav(@PathVariable("uid") String uid) {
        doTheOne(uid, null);
        return "success";
    }

    public void doTheOne(String uid, String gallery) {
        String owner_name;
//
        UrlContainer container = new UrlContainer(String.format("https://api.bilibili.com/x/v3/fav/folder/created/list-all?up_mid=%1$s&jsonp=jsonp", uid));
        container.setHeaders(properties.getCookies());
        HttpsURLConnection conn = URLConnectionTool.getHttpsURLConnection(container);
        try {
            owner_name = BilibiliCommonUtils.getUserInfo(uid);
            conn.setRequestProperty("Referer", "https://space.bilibili.com/" +
                    uid + "/favlist");
            InputStream is = conn.getInputStream();
            JsonObject json = (JsonObject) JsonParser.parseReader(new InputStreamReader(is));
            String path = "data-list";
            JsonArray favourFolders = CommonIOUtils.getFromJson2(json, path).getAsJsonArray();
            BilibiliDownloadCore bdc = SpringContext.getBeanWithGenerics(BilibiliDownloadCore.class);
            for (JsonElement jsonElement : favourFolders) {
                Map<String, List<String>> result = new HashMap<>();
                CommonIOUtils.getFromJson2Str(jsonElement, "title");
                String media_id = CommonIOUtils.getFromJson2Str(jsonElement, "id");
                String title = CommonIOUtils.getFromJson2Str(jsonElement, "title");
                if (gallery != null && !title.equals(gallery)) {
                    continue;
                }
                int media_count = CommonIOUtils.getFromJson2Integer(jsonElement, "media_count");
                int page = media_count / 20 + 1;
                for (int i = 1; i <= page; i++) {
                    spaceDetail(result, media_id, i, uid);
                }
                if (result.size() == 1) {
                    Map.Entry<String, List<String>> entry = result.entrySet().iterator().next();
                    bdc.getNewPath().set(properties.getFavouriteSavePath() + File.separator + owner_name + "\\" + entry.getKey() + "\\");
                    bdc.downloadList(entry.getValue());
                }
                result.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void spaceDetail(Map<String, List<String>> result, String media_id, int pn, String uid) {
        String url = "https://api.bilibili.com/medialist/gateway/base/spaceDetail?media_id=" +
                media_id + "&pn=" +
                pn + "&ps=20&keyword=&order=mtime&type=0&tid=0&jsonp=jsonp";
        UrlContainer container = new UrlContainer(url);
        container.setHeaders(properties.getCookies());
        HttpsURLConnection conn = URLConnectionTool.getHttpsURLConnection(container);
        try {
            conn.setRequestProperty("Referer", "https://space.bilibili.com/" +
                    uid + "/favlist");
            InputStream is = conn.getInputStream();
            JsonElement json = JsonParser.parseReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            JsonElement element = CommonIOUtils.getFromJson2(json, "data-medias");
            if (element != null && !element.isJsonNull()) {
                JsonArray medias = element.getAsJsonArray();
                String folder = CommonIOUtils.getFromJson2Str(json, "data-info-title");
                if (result.get(folder) == null || result.get(folder).isEmpty()) {
                    result.put(folder, new ArrayList<>());
                }
                for (JsonElement jsonElement : medias) {
                    result.get(folder).add(CommonIOUtils.getFromJson2Str(jsonElement, "bvid"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
