package cn.zpl.spider.on.bilibili.controller;

import cn.zpl.config.SpringContext;
import cn.zpl.spider.on.bilibili.BilibiliDownloadCore2;
import cn.zpl.spider.on.bilibili.common.BilibiliCommonUtils;
import cn.zpl.spider.on.bilibili.common.BilibiliConfigParams;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.URLConnectionTool;
import cn.zpl.util.UrlContainer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
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

//    private static final String uid = "404412";
//    private static final String url = "https://api.bilibili.com/medialist/gateway/base/created?pn=1&ps=100&up_mid=" +
//            uid + "&is_space=0&jsonp=jsonp";

    @GetMapping("/downloadFav/{uid}")
    public String downloadFav(@PathVariable("uid") String uid) {
        doTheOne(uid);
        return "success";
    }

    public void doTheOne(String uid) {
        String owner_name;
//
        UrlContainer container = new UrlContainer(String.format("https://api.bilibili.com/x/v3/fav/folder/created/list-all?up_mid=%1$s&jsonp=jsonp", uid));
        container.setHeaders(BilibiliCommonUtils.getConfigParams().properties.cookies);
        HttpsURLConnection conn = URLConnectionTool.getHttpsURLConnection(container);
        try {
            owner_name = BilibiliCommonUtils.getUserInfo(uid);
            conn.setRequestProperty("Referer", "https://space.bilibili.com/" +
                    uid + "/favlist");
            InputStream is = conn.getInputStream();
            JsonObject json = (JsonObject) JsonParser.parseReader(new InputStreamReader(is));
            String path = "data-list";
            JsonArray favourFolders = CommonIOUtils.getFromJson2(json, path).getAsJsonArray();
            BilibiliDownloadCore2 bdc = SpringContext.getBeanWithGenerics(BilibiliDownloadCore2.class);
            for (JsonElement jsonElement : favourFolders) {
                Map<String, List<String>> result = new HashMap<>();
                String media_id = jsonElement.getAsJsonObject().get("id").getAsString();
                String title = jsonElement.getAsJsonObject().get("title").getAsString();
//                if (!title.equals("原神2")) {
//                    continue;
//                }
                int media_count = jsonElement.getAsJsonObject().get("media_count").getAsInt();
                int page = media_count / 20 + 1;
                for (int i = 1; i <= page; i++) {
                    spaceDetail(result, media_id, i, uid);
                }
                if (result.size() == 1) {
                    Map.Entry<String, List<String>> entry = result.entrySet().iterator().next();
                    bdc.getNewPath().set(BilibiliCommonUtils.getConfigParams().properties.favourite_save_path + File.separator + owner_name + "\\" + entry.getKey() + "\\");
                    bdc.downloadList(entry.getValue());
                }
                result.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void spaceDetail(Map<String, List<String>> result, String media_id, int pn, String uid) {
        String url = "https://api.bilibili.com/medialist/gateway/base/spaceDetail?media_id=" +
                media_id + "&pn=" +
                pn + "&ps=20&keyword=&order=mtime&type=0&tid=0&jsonp=jsonp";
        UrlContainer container = new UrlContainer(url);
        container.setHeaders(BilibiliCommonUtils.getConfigParams().properties.cookies);
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
