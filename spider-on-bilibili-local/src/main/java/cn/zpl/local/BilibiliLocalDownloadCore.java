package cn.zpl.local;

import cn.zpl.pojo.Data;
import cn.zpl.pojo.DownloadDTO;
import cn.zpl.pojo.VideoData;
import cn.zpl.thread.OneFileOneThread;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.DownloadTools;
import cn.zpl.util.FFMEPGToolsPatch;
import cn.zpl.util.JsonUtil;
import cn.zpl.util.SaveLog;
import cn.zpl.util.URLConnectionTool;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
@Component
public class BilibiliLocalDownloadCore {

    private String owner_name = "";
    private ThreadLocal<Integer> already = new ThreadLocal<>();
    private String avid;

    @Resource
    LocalBilibiliProperties properties;

    private ThreadLocal<String> newPath = new ThreadLocal<>();

    public void getVideoList(String uid, String json) {
        int page = 1;
        while (page > 0) {
            try {
                if (getPlayListByWeb(uid, String.valueOf(page), json) > 0) {
                    page++;
                    continue;
                }
                break;
            } catch (Exception e) {
                log.error("获取视频列表异常：", e);
                break;
            }
        }
    }

    public void getCollections(String bid) {
        Data data = new Data();
        data.setUrl("https://api.bilibili.com/x/web-interface/view?bvid=" + bid);
        data.setHeader(properties.getCookies());
        data.setReferer("https://www.bilibili.com/video/" + bid);
        CommonIOUtils.withTimer(data);
        JsonElement json = CommonIOUtils.paraseJsonFromStr(data.getResult());
        int code = JsonUtil.getFromJson2Integer(json, "code");
        if (code != 0) {
            log.error("读取合集失败");
            return;
        }
        JsonElement sections = JsonUtil.getFromJson(json, "data-ugc_season-sections");
        if (sections.isJsonArray()) {
            JsonElement jsonElement = sections.getAsJsonArray().get(0);
            JsonElement episodes = JsonUtil.getFromJson(jsonElement, "episodes");
            if (episodes.isJsonArray()) {
                for (JsonElement episode : episodes.getAsJsonArray()) {
                    String bvid = JsonUtil.getFromJson2Str(episode, "bvid");
                    mainBusiness(bvid);
                }
            }
        }
    }

    private int getPlayListByWeb(String uid, String page, String jsonStr) throws Exception {
        owner_name = getUserInfo(uid);
        Data data = new Data();
        data.setUrl("https://api.bilibili.com/x/space/arc/search?mid=" + uid + "&ps=100&tid=0&pn=" + page + "&keyword=&order=pubdate&jsonp=jsonp");
        data.setHeader(properties.getCookies());
        CommonIOUtils.withTimer(data);
        JsonElement json = (jsonStr != null && !jsonStr.isEmpty()) ? CommonIOUtils.paraseJsonFromStr(jsonStr) : CommonIOUtils.paraseJsonFromStr(data.getResult());
        int videoSize = CommonIOUtils.getFromJson2(json, "data-list-vlist").isJsonArray() ? CommonIOUtils.getFromJson2(json, "data-list-vlist").getAsJsonArray().size() : 0;
        JsonElement vlist = CommonIOUtils.getFromJson2(json, "data-list-vlist");
        List<String> list = new ArrayList<>();
        if (vlist.isJsonArray()) {
            vlist.getAsJsonArray().forEach(jsonElement -> {
                String bvid = CommonIOUtils.getFromJson2Str(jsonElement, "bvid");
                list.add(bvid);
            });
        }
        for (String videoId : list) {
            mainBusiness(videoId);
        }
        if (jsonStr != null && !jsonStr.isEmpty()) {
            return 0;
        }
        return videoSize;
    }

    private String getUserInfo(String uid) {
        try {
            Data data = new Data();
            data.setUrl("https://api.bilibili.com/x/web-interface/nav");
            data.setHeader(properties.getCookies());
            CommonIOUtils.withTimer(data);
            return CommonIOUtils.getFromJson2Str(CommonIOUtils.paraseJsonFromStr(data.getResult()), "data-uname");
        } catch (Exception e) {
            log.warn("获取用户信息失败：{}", e.getMessage());
            return "unknown";
        }
    }

    public void getEpList(String epId) {
        Data data = new Data();
        data.setUrl(String.format(LocalBilibiliProperties.getEpListUrl, epId));
        data.setHeader(LocalBilibiliProperties.commonHeaders + properties.getCookies());
        CommonIOUtils.withTimer(data);
        JsonElement[] eps = CommonIOUtils.getFromJson3(data.getResult(), "result-episodes-bvid");
        for (JsonElement ep : eps) {
            mainBusiness(ep.getAsString());
        }
    }

    public void mainBusiness(@NotNull String videoId) {
        if (videoId.startsWith("http")) {
            videoId = videoId.substring(videoId.lastIndexOf("/") + 1);
        }
        if (videoId.startsWith("av")) {
            videoId = videoId.replace("av", "");
        }
        boolean match = Pattern.matches("^\\d+$", videoId);

        Data data = new Data();
        try {
            data.setUrl(String.format("https://api.bilibili.com/x/web-interface/view?bvid=%1$s", videoId));
            if (match) {
                log.error("不支持纯数字av号，请使用BV号");
                return;
            }
            CommonIOUtils.withTimer(data);
            if (data.getResult() == null && data.getStatusCode() == 403) {
                Thread.sleep(3000);
                CommonIOUtils.withTimer(data);
                if (data.getStatusCode() == 403) {
                    log.error(videoId + "拒绝访问403，请查证后重试");
                    return;
                }
            }
            JsonElement result = JsonUtil.parseFromStr(data.getResult());
            int code = JsonUtil.getFromJson2Integer(result, "code");

            JsonElement jsonData = CommonIOUtils.getFromJson2(result, "data");

            if (code != 0) {
                log.error(CommonIOUtils.getFromJson2Str(result, "message"));
            }
            if (code == 62002) {
                log.error(CommonIOUtils.getFromJson2Str(result, "message"));
                return;
            }
            if (code == -404) {
                log.error("视频不存在！");
                return;
            }
            if (code == -403) {
                data.setHeader(properties.getCookies());
                CommonIOUtils.withTimer(data);
                result = JsonUtil.parseFromStr(data.getResult());
                code = JsonUtil.getFromJson2Integer(result, "code");
                if (code != 0) {
                    log.error(CommonIOUtils.getFromJson2Str(result, "message"));
                    return;
                }
            }
            String title = JsonUtil.getFromJson2Str(jsonData, "title");
            String owner = JsonUtil.getFromJson2Str(jsonData, "owner-name");
            log.info("开始下载：{} - UP主：{}", title, owner);
            JsonElement pages = JsonUtil.getFromJson(jsonData, "pages");
            if (pages.isJsonArray()) {
                int count = 0;
                for (JsonElement part : pages.getAsJsonArray()) {
                    if (count > 25) {
                        count = 0;
                        TimeUnit.MINUTES.sleep(20);
                    }
                    boolean success = downLoadByAPI(data, jsonData, part);
                    if (success) {
                        count++;
                    }
                }
            }
            avid = CommonIOUtils.getFromJson2Str(result, "aid");
        } catch (Exception e) {
            if (!data.doRetry()) {
                log.error("重试次数已用完，返回");
                return;
            }
            log.error(videoId + "下载异常，重新解析\n", e);
            data.sleep();
            mainBusiness(videoId);
        }
    }

    private boolean downLoadByAPI(Data data, JsonElement jsonData, JsonElement pageJson) {
        String aid = JsonUtil.getFromJson2Str(jsonData, "aid");
        avid = aid;
        String bvid = JsonUtil.getFromJson2Str(jsonData, "bvid");
        int videos = CommonIOUtils.getFromJson2Integer(jsonData, "videos");
        String title = JsonUtil.getFromJson2Str(jsonData, "title");
        String cid = JsonUtil.getFromJson2Str(pageJson, "cid");
        String partName = JsonUtil.getFromJson2Str(pageJson, "part");
        int pageNumber = JsonUtil.getFromJson2Integer(pageJson, "page");

        data.setUrl("https://www.bilibili.com/video/" + bvid);
        data.setHeader(LocalBilibiliProperties.commonHeaders + properties.getCookies());
        CommonIOUtils.withTimer(data);
        Document document = Jsoup.parse(data.getResult());
        String playurlSSRData = CommonIOUtils.getScriptContainTheStr(document, "playurlSSRData");
        JsonElement playInfo;
        if (playurlSSRData == null) {
            data.setUrl(String.format(LocalBilibiliProperties.playInfoUrl, aid, bvid, cid));
            CommonIOUtils.withTimer(data);
            playInfo = JsonUtil.parseFromStr(data.getResult());
        } else {
            playInfo = CommonIOUtils.paraseJsonFromStr(CommonIOUtils.getJsonStrWitchBegin(playurlSSRData, "playurlSSRData"));
        }
        int code = JsonUtil.getFromJson2Integer(playInfo, "code");
        if (code == -404) {
            log.error("视频不存在！");
            return false;
        }
        title = CommonIOUtils.filterFileName2(title);

        VideoData videoData = new VideoData();
        String savedLocalName = CommonIOUtils.filterFileName2(StringEscapeUtils.escapeHtml4(pageNumber + "." + partName).replaceAll("(&[a-z]{3};)+", ""));
        String ownerName = (owner_name == null || "".equals(owner_name)) ? JsonUtil.getFromJson2Str(jsonData, "owner-name") : owner_name;
        String timeLength = String.valueOf(JsonUtil.getFromJson2Integer(pageJson, "duration") * 1000L);

        List<String> path = new ArrayList<>();
        path.add(newPath.get() != null && !"".equals(newPath.get()) ? newPath.get() : properties.getVideoSavePath());
        if (newPath.get() == null) {
            path.add(ownerName);
        }
        String videoName = title + "(av" + avid + ")[" + bvid + "]" + ".mp4";
        if (videos > 1) {
            path.add(title + "(av" + avid + ")");
            videoName = savedLocalName + ".mp4";
        }
        videoData.setWebSite("bilibili");
        videoData.setDesSaveName(videoName);
        videoData.setTmpSavePath(new File(properties.getTmpSavePath(), avid));
        videoData.setDesSavePath(CommonIOUtils.makeFilePath(path, videoName));
        videoData.setTimeLength(timeLength);
        videoData.setVideoId(avid);

        if (new File(videoData.getDesSavePath()).exists()) {
            log.info("文件已存在，跳过：{}", videoData.getDesSavePath());
            return false;
        }

        JsonArray acceptQuality = CommonIOUtils.getFromJson2(playInfo, playurlSSRData == null ? "data-accept_quality" : "result-video_info-accept_quality").getAsJsonArray();
        List<Integer> quality = new ArrayList<>();
        acceptQuality.forEach(jsonElement -> quality.add(jsonElement.getAsInt()));
        Collections.sort(quality);
        Collections.reverse(quality);

        JsonElement videoList = JsonUtil.getFromJson(playInfo, playurlSSRData == null ? "data-dash-video" : "result-video_info-dash-video");
        JsonElement videoCurrent = JsonNull.INSTANCE;
        if (videoList.isJsonArray()) {
            for (JsonElement jsonElement : videoList.getAsJsonArray()) {
                int id = JsonUtil.getFromJson2Integer(jsonElement, "id");
                if (id == quality.get(0)) {
                    videoCurrent = jsonElement;
                }
            }
        }
        JsonElement tmp = videoList.getAsJsonArray().get(0);
        if (videoCurrent.isJsonNull() && JsonUtil.getFromJson2Integer(tmp, "width") >= 1920) {
            videoCurrent = tmp;
        }
        JsonElement audioList = JsonUtil.getFromJson(playInfo, playurlSSRData == null ? "data-dash-audio" : "result-video_info-dash-audio");
        JsonElement audioCurrent = JsonNull.INSTANCE;
        if (audioList.isJsonArray()) {
            audioCurrent = audioList.getAsJsonArray().get(0);
        }

        doM4s(videoCurrent, audioCurrent, avid, bvid, title, videoData);
        if (!new FFMEPGToolsPatch().mergeBilibiliVideo2(videoData)) {
            log.error("合并失败，video_id：{}", avid);
        } else {
            log.info("下载完成：{}", videoData.getDesSavePath());
        }
        return true;
    }

    private void doM4s(JsonElement videoElement, JsonElement audioElement, String avid, String bvid, String title, VideoData videoData) {
        List<String> pathMake = new ArrayList<>();
        DownloadDTO video = buildTemplate(videoElement, bvid, avid);
        DownloadDTO audio = buildTemplate(audioElement, bvid, avid);

        videoData.setVideo(video);
        videoData.setAudio(audio);
        DownloadTools tools = DownloadTools.getInstance(30);
        tools.setName(title);
        if (video != null) {
            addDownload(tools, video);
        }
        if (audio != null) {
            addDownload(tools, audio);
        }
        tools.shutdown();
        assert video != null;
        if (new File(video.getSavePath()).length() == video.getFileLength() && (audio == null || new File(audio.getSavePath()).length() == audio.getFileLength())) {
            SaveLog.saveLog(video.getSavePath());
            log.debug(video.getSavePath() + "下载完毕");
            if (audio != null) {
                SaveLog.saveLog(audio.getSavePath());
                log.debug(audio.getSavePath() + "下载完毕");
            }
        } else {
            CommonIOUtils.waitSeconds(5);
            log.error("下载大小与返回头的大小不一致，重新下载");
            throw new RuntimeException("下载大小与返回头的大小不一致，重新下载");
        }
    }

    private DownloadDTO buildTemplate(JsonElement m4s, String bvid, String avid) {
        if (m4s == null || m4s.isJsonNull()) {
            return null;
        }
        String baseUrl = CommonIOUtils.getFromJson2Str(m4s, "baseUrl");
        List<String> pathMake = new ArrayList<>();
        DownloadDTO downloadDTO = new DownloadDTO();
        downloadDTO.setUrl(baseUrl);
        downloadDTO.setWebSite("bilibili");
        downloadDTO.setReferer("https://www.bilibili.com/video/" + bvid);
        downloadDTO.setHeader(
                "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36 Edg/85.0.564.63\n");
        downloadDTO.setFileLength(URLConnectionTool.getDataLength(downloadDTO));
        pathMake.add(properties.getTmpSavePath());
        pathMake.add(avid);
        downloadDTO.setSavePath(CommonIOUtils.makeFilePath(pathMake, downloadDTO.getUrl().substring(downloadDTO.getUrl().lastIndexOf("/") + 1, downloadDTO.getUrl().indexOf("?"))));
        return downloadDTO;
    }

    private void addDownload(DownloadTools tools, DownloadDTO dto) {
        if (dto.getFileLength() == 0) {
            tools.ThreadExecutorAdd(new OneFileOneThread(dto));
            return;
        }
        if (!new File(dto.getSavePath()).exists() || !SaveLog.isCompeleteMultiple(dto)) {
            tools.MultipleThreadWithLog(dto);
        } else {
            log.debug(dto.getSavePath() + "已下载，跳过");
        }
    }
}
