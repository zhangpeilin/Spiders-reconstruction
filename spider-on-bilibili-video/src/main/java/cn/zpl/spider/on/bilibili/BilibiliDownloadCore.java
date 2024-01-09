package cn.zpl.spider.on.bilibili;

import cn.zpl.common.bean.ExceptionList;
import cn.zpl.common.bean.RestResponse;
import cn.zpl.common.bean.VideoInfo;
import cn.zpl.config.CommonParams;
import cn.zpl.pojo.Data;
import cn.zpl.pojo.DownloadDTO;
import cn.zpl.pojo.VideoData;
import cn.zpl.spider.on.bilibili.common.BilibiliCommonUtils;
import cn.zpl.spider.on.bilibili.common.BilibiliProperties;
import cn.zpl.spider.on.bilibili.common.TransformVideId;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.CrudTools;
import cn.zpl.util.DownloadTools;
import cn.zpl.util.FFMEPGToolsPatch;
import cn.zpl.util.SaveLog;
import cn.zpl.util.URLConnectionTool;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.regex.Pattern;

@Slf4j
@Component
public class BilibiliDownloadCore {

    private String owner_name = "";
    private List<String> exception = new ArrayList<>();
    private boolean filter = false;
    private ThreadLocal<Integer> already = new ThreadLocal<>();
    private String avid;

    @Resource
    BilibiliProperties properties;

    @Resource
    CrudTools crudTools;
    @Resource
    FFMEPGToolsPatch ffmepgToolsPatch;

    @Resource
    BilibiliCommonUtils bilibiliCommonUtils;

    public ThreadLocal<String> getNewPath() {
        return newPath;
    }

    private ThreadLocal<String> newPath = new ThreadLocal<>();

    //下载指定用户
    public void test() {
        getVideoList("4653374");
    }

    public static void main(String[] args) {
        BilibiliDownloadCore bilibiliDownloadCore2 = new BilibiliDownloadCore();
        bilibiliDownloadCore2.downloadTheVideo();
    }

    public void downloadTheVideo() {
        filter = false;
        FFMEPGToolsPatch.checkExist = false;
        String video_id = "BV1tA4y197wd";
        mainBusiness(video_id);
    }

    public void downloadList(@NotNull List<String> list) {
        for (String string : list) {
            if (already.get() == null) {
                already.set(0);
            }
            if (already.get() > 5) {
                already.set(0);
                return;
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mainBusiness(string);
        }
    }

    void getVideoList(String uid) {
        int page = 1;
        while (page > 0) {
            try {
                if (getPlayListByWeb(uid, String.valueOf(page)) > 0) {
                    page++;
                    continue;
                }
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int getPlayListByWeb(String uid, String page) throws IOException {
        owner_name = BilibiliCommonUtils.getUserInfo(uid);
        Data data = new Data();
        data.setUrl("https://api.bilibili.com/x/space/arc/search?mid=" + uid + "&ps=100&tid=0&pn=" + page + "&keyword=&order=pubdate&jsonp=jsonp");
        data.setHeader(properties.getCookies());
        CommonIOUtils.withTimer(data);
        JsonElement json = CommonIOUtils.paraseJsonFromStr(data.getResult());
        int videoSize = CommonIOUtils.getFromJson2(json, "data-list-vlist").isJsonArray() ? CommonIOUtils.getFromJson2(json, "data-list-vlist").getAsJsonArray().size() : 0;
        JsonElement vlist = CommonIOUtils.getFromJson2(json, "data-list-vlist");
        Vector<HashMap<String, String>> list = new Vector<>();
        if (vlist.isJsonArray()) {
            vlist.getAsJsonArray().forEach(jsonElement -> {
                HashMap<String, String> map = new HashMap<>();
                map.put("video_id", CommonIOUtils.getFromJson2Str(jsonElement, "bvid"));
                list.add(map);
            });
        }
        for (HashMap<String, String> element : list) {
            mainBusiness(element.get("video_id"));
        }
        return videoSize;
    }

    private boolean isDownloaded(final String cid, final String avid) {
//        if (isException(avid)) {
//            return true;
//        }
        VideoInfo exist = bilibiliCommonUtils.getVideoInfo(cid);
        if (exist == null) {
            return false;
        }
        return new File(exist.getLocalPath()).exists();
    }

    private VideoInfo getVideoInfoByCid(String cid) {
        List<VideoInfo> videoInfos = crudTools.commonApiQuery(String.format("video_id = %1$s", cid), VideoInfo.class);
        if (!videoInfos.isEmpty()) {
            return videoInfos.get(0);
        }
        return null;
    }

    private boolean isException(final String cid) {
        if (exception.contains(cid)) {
            log.debug(cid + "已排除");
            return true;
        } else {
            ExceptionList exist = bilibiliCommonUtils.getExceptionList(cid);
            boolean isException = exist != null;
            if (isException) {
                exception.add(cid);
            }
            return isException;
        }
    }

    public void mainBusiness(@NotNull String videoId) {

        //^\d+$
        if (videoId.startsWith("http")) {
            videoId = videoId.substring(videoId.lastIndexOf("/") + 1);
        }
        if (videoId.startsWith("av")) {
            videoId = videoId.replace("av", "");
        }
        if (isException(videoId)) {
            return;
        }
        //如果是纯数字，true
        boolean match = Pattern.matches("^\\d+$", videoId);

        Data data = new Data();
        try {
            data.setUrl("https://api.bilibili.com/x/player/pagelist?bvid=" + videoId + "&jsonp=jsonp");
            if (match) {
                log.error("出大问题");
                System.exit(0);
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
            JsonElement result = JsonParser.parseString(Objects.requireNonNull(data.getString()));
            int code = CommonIOUtils.getFromJson2Integer(result, "code");

            JsonElement temp = CommonIOUtils.getFromJson2(result, "data");
//            boolean isDownloaded = false;
//            if (temp.isJsonArray()) {
//                for (JsonElement part : temp.getAsJsonArray()) {
//                    isDownloaded = isDownloaded(CommonIOUtils.getFromJson2Str(part, "cid"), avid);
//                }
//            }
//            if (isDownloaded) {
//                return;
//            }
            if (code != 0) {
                log.error(CommonIOUtils.getFromJson2Str(result, "message"));
            }
            if (code == -404) {
                log.error("视频不存在！");
                return;
            }
            if (code == -403) {
                data.setHeader(properties.getCookies());
                CommonIOUtils.withTimer(data);
                result = JsonParser.parseString(Objects.requireNonNull(data.getString()));
                code = CommonIOUtils.getFromJson2Integer(result, "code");
                if (code != 0) {
                    log.error(CommonIOUtils.getFromJson2Str(result, "message"));
                    return;
                }
            }
            result.getAsJsonObject().addProperty("aid", TransformVideId.b2a(videoId));
            result.getAsJsonObject().addProperty("bvid", videoId);
            Document document = CommonIOUtils.getDocumentFromUrl("https://www.bilibili.com/video/" + videoId);
            Element titleEle = document.selectFirst("title");
            Element upInfo = document.selectFirst("head > meta:nth-child(18)");
            if (titleEle != null) {
                result.getAsJsonObject().addProperty("title", titleEle.text());
            } else {
                log.error("获取视频标题失败，使用aid代替");
                result.getAsJsonObject().addProperty("title", videoId);
            }
            if (upInfo != null) {
                result.getAsJsonObject().addProperty("userName", upInfo.attr("content"));
            }
            JsonElement parts = CommonIOUtils.getFromJson2(result, "data");
            if (parts.isJsonArray()) {
                result.getAsJsonObject().addProperty("videos", parts.getAsJsonArray().size());
                for (JsonElement part : parts.getAsJsonArray()) {
                    Data download = new Data();
                    downLoadByAPI(download, "", CommonIOUtils.getFromJson2Str(part, "cid"), result, part);
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
            exception.clear();
            mainBusiness(videoId);
        }

    }

    private void downLoadByAPI(Data data, @NotNull String qualityLevel, String cid, JsonElement mainJson, JsonElement partJson) {
        if (isDownloaded(cid, avid)) {
            return;
        }
        String avid = CommonIOUtils.getFromJson2Str(mainJson, "aid");
        String bvid = CommonIOUtils.getFromJson2Str(mainJson, "bvid");
        int videos = CommonIOUtils.getFromJson2Integer(mainJson, "videos");
//        owner_name = CommonIOUtils.getFromJson2Str(mainJson, "data-owner-name");
        String title = CommonIOUtils.getFromJson2Str(mainJson, "title");
        String part = CommonIOUtils.getFromJson2Str(partJson, "part");
        int page = CommonIOUtils.getFromJson2Integer(partJson, "page");
        String url = "https://api.bilibili.com/x/player/playurl?avid=" + avid + "&cid=" + cid + "&bvid=&qn=" + ("".equals(qualityLevel) ? "112" : qualityLevel) + "&type=&otype=json&fnver=0&fnval=16&fourk=1";
        data.setUrl(url);
        data.setHeader(properties.getCookies());
        CommonIOUtils.withTimer(data);
        JsonElement json = JsonParser.parseString(Objects.requireNonNull(data.getString()));
        int code = CommonIOUtils.getFromJson2Integer(json, "code");
        if (code == -404) {
            log.error("视频不存在！");
            return;
        }
        title = CommonIOUtils.filterFileName2(title);
        VideoData videoData = new VideoData();
        VideoInfo videoInfo = new VideoInfo();
        videoInfo.setOwnerName(owner_name == null || "".equalsIgnoreCase(owner_name) ? CommonIOUtils.getFromJson2Str(mainJson, "userName") : owner_name);
        videoInfo.setVideoId(cid);
        videoInfo.setAid(avid);
        videoInfo.setPageCount(videos);
        videoInfo.setTimeLength(CommonIOUtils.getFromJson2Str(json, "data-timelength"));
        videoInfo.setSavedLocalName(CommonIOUtils.filterFileName2(StringEscapeUtils.escapeHtml4(page + "." + part).replaceAll("(&[a-z]{3};)+", "")));
        videoInfo.setVideoName(title);
        videoInfo.setTitle(title);
        videoInfo.setWebsite("bilibili");
        videoInfo.setDownloadDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        videoInfo.setBid(bvid);

        List<String> path = new ArrayList<>();
        path.add(newPath.get() != null && !"".equals(newPath.get()) ? newPath.get() : properties.getVideoSavePath());
        path.add(owner_name);
        String videoName = title + "(av" + avid + ")" + ".mp4";
        if (videos > 1) {
            path.add(title + "(av" + avid + ")");
            videoName = videoInfo.getSavedLocalName() + ".mp4";
        }
        videoData.setWebSite("bilibili");
        videoData.setDesSaveName(videoName);
        videoData.setTmpSavePath(new File(properties.getTmpSavePath(), avid));
        videoData.setDesSavePath(CommonIOUtils.makeFilePath(path, videoName));
        videoInfo.setLocalPath(videoData.getDesSavePath());
        videoData.setTimeLength(videoInfo.getTimeLength());
        videoData.setVideoId(avid);
        if (FFMEPGToolsPatch.isExists(videoData)) {
            RestResponse restResponse = crudTools.commonApiSave(videoInfo);
            if (!restResponse.isSuccess()) {
                throw new RuntimeException("保存记录失败");
            }
            return;
        }
        if (filter && isDownloaded(cid, avid)) {
            already.set(already.get() == null ? 0 : already.get() + 1);
            VideoInfo exist = getVideoInfoByCid(cid);
            assert exist != null;
            if (exist.getLocalPath().equalsIgnoreCase(videoInfo.getLocalPath())) {
                return;
            }
            try {
                FileUtils.moveFile(new File(exist.getLocalPath()), new File(videoInfo.getLocalPath()));
            } catch (IOException e) {
                log.error("移动失败，有可能文件重复");
                log.error("错误原因：", e);
            }
            log.debug(title);
            return;
        }
        if (FFMEPGToolsPatch.isExists(videoData)) {
            RestResponse restResponse = crudTools.commonApiSave(videoInfo);
            if (!restResponse.isSuccess()) {
                throw new RuntimeException("保存记录失败");
            }
            return;
        }
        JsonArray acceptQuality =
                CommonIOUtils.getFromJson2(json, "data-accept_quality").getAsJsonArray();
        List<Integer> quality = new ArrayList<>();
        acceptQuality.forEach(jsonElement -> quality.add(jsonElement.getAsInt()));
        Collections.sort(quality);
        Collections.reverse(quality);
        String currentQuality = CommonIOUtils.getFromJson2Str(json, "data-quality");
        // 如果有高画质，则重新执行主方法，执行完后直接return
        if (!currentQuality.equals(String.valueOf(quality.get(0)))) {
            log.error("画质重定位");
            if (data.doRetry()) {
                downLoadByAPI(data, String.valueOf(quality.get(0)), cid, mainJson, partJson);
            }
            return;
        }
        //判断是否为1p多段，如果是，那么json中是flv的下载地址，否则是m4s的地址
        if (CommonIOUtils.getFromJson2(json, "data-dash-video").isJsonNull()) {
            //1p多段下载
            dealMultiplePart(json, avid, videoData, page, videoInfo);
            if (!properties.merge) {
                return;
            }
            if (!FFMEPGToolsPatch.mergeBilibiliVideo(videoData)) {
                log.error("不应该出现在这，video_id：" + avid);
                System.exit(1);
            }
        }
        if (!CommonIOUtils.getFromJson2(json, "data-dash-video").isJsonNull()) {
            doM4s(json, avid, bvid, currentQuality, videoInfo, videoData);
            if (!properties.merge) {
                return;
            }
            if (!ffmepgToolsPatch.mergeBilibiliVideo2(videoData)) {
                log.error("不应该出现在这，video_id：" + avid);
                System.exit(1);
            }
        }
        RestResponse restResponse = crudTools.commonApiSave(videoInfo);
        if (!restResponse.isSuccess()) {
            throw new RuntimeException("保存下载记录失败");
        }
    }

    private void dealMultiplePart(JsonElement json, String video_id, VideoData videoData, int page, VideoInfo video) {
        JsonElement durl = CommonIOUtils.getFromJson2(json, "data-durl");
        String play_list;
        String order;
        if (durl.isJsonArray()) {
            for (JsonElement jsonElement : durl.getAsJsonArray()) {
                List<String> pathMake = new ArrayList<>();
                // 下载每个分p的分段，最后合并
                JsonObject obj = jsonElement.getAsJsonObject();
                play_list = obj.get("url").getAsString();
                order = obj.get("order").getAsString();

                DownloadDTO dto = new DownloadDTO();
                dto.setUrl(play_list);
                dto.setWebSite("bilibili");
                dto.setReferer("https://www.bilibili.com/video/av" + video_id);
                video.setUrl(dto.getReferer());

                dto.setHeader(
                        "User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36\n");
                long length = URLConnectionTool.getDataLength(dto);
                // 存储的是视频大小（字节）
                dto.setFileLength(length);
                //如果有指定保存路径，则使用指定的路径，否则从config.properties中读取
                pathMake.add(properties.getTmpSavePath());
                pathMake.add(video_id);
                dto.setSavePath(CommonIOUtils.makeFilePath(pathMake, "p" + page + " order" + order + ".flv"));
                videoData.getPartList().add(dto.getSavePath());
                if (new File(dto.getSavePath()).exists() && SaveLog.isCompeleteMultiple(dto)) {
                    log.error("已下载，跳过");
                    continue;
                }
                DownloadTools tools = DownloadTools.getInstance(30);
                tools.setName((String) video.getTitle());
                tools.MultipleThread(dto);
                tools.shutdown();
                if (new File(dto.getSavePath()).length() == length) {
                    SaveLog.saveLog(dto.getSavePath());
                    log.debug(dto.getSavePath() + "下载完毕");
                } else {
                    log.error("下载大小与返回头的大小不一致，重新下载");
                    throw new RuntimeException("下载大小与返回头的大小不一致，重新下载");
                }
            }
        }
    }

    private void doM4s(JsonElement json, String avid, String bvid, String current_quality, VideoInfo videoInfo, VideoData videoData) {

        JsonElement videoElement = CommonIOUtils.getFromJson2(json, "data-dash-video");
        JsonElement audioElement = CommonIOUtils.getFromJson2(json, "data-dash-audio");
//        if (!videoElement.isJsonArray() || !audioElement.isJsonArray()) {
//            log.error(avid + "解析出错");
//            System.exit(1);
//        }
        JsonElement videoM4s = null;
        JsonElement audioM4s = null;
        if (!videoElement.isJsonNull()) {
            videoM4s= videoElement.getAsJsonArray().get(0);
        }
        if (!audioElement.isJsonNull()) {
            audioM4s = audioElement.getAsJsonArray().get(0);
        }
        if (Integer.parseInt(CommonIOUtils.getFromJson2Str(videoM4s, "id")) < Integer.parseInt(current_quality)) {
            log.error(avid + "解析出错");
            System.exit(1);
        }
        List<String> pathMake = new ArrayList<>();
        // 下载每个分p的分段，最后合并
        String baseUrl = CommonIOUtils.getFromJson2Str(videoM4s, "baseUrl");

        DownloadDTO video = buildTemplate(videoM4s, bvid, avid);
        DownloadDTO audio = buildTemplate(audioM4s, bvid, avid);

        //如果有指定保存路径，则使用指定的路径，否则从config.properties中读取

        videoData.setVideo(video);
        videoData.setAudio(audio);
        DownloadTools tools = DownloadTools.getInstance(30);
        tools.setName(videoInfo.getTitle());
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

    private DownloadDTO buildTemplate(JsonElement m4s, String bvid, String avid){
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
        // 存储的是视频大小（字节）
        downloadDTO.setFileLength(URLConnectionTool.getDataLength(downloadDTO));
        //如果有指定保存路径，则使用指定的路径，否则从config.properties中读取
        pathMake.add(properties.getTmpSavePath());
        pathMake.add(avid);
        downloadDTO.setSavePath(CommonIOUtils.makeFilePath(pathMake, downloadDTO.getUrl().substring(downloadDTO.getUrl().lastIndexOf("/") + 1, downloadDTO.getUrl().indexOf("?"))));
        return downloadDTO;
    }

    private void addDownload(DownloadTools tools, DownloadDTO dto){
        if (!new File(dto.getSavePath()).exists() || !SaveLog.isCompeleteMultiple(dto)) {
            tools.MultipleThreadWithLog(dto);
        } else {
            log.debug(dto.getSavePath() + "已下载，跳过");
        }
    }

    String getAvid() {
        return avid;
    }

    public void setAvid(String avid) {
        this.avid = avid;
    }
}
