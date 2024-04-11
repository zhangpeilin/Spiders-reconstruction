package cn.zpl.spider.on.bilibili;

import cn.zpl.common.bean.ExceptionList;
import cn.zpl.common.bean.RestResponse;
import cn.zpl.common.bean.VideoInfo;
import cn.zpl.pojo.Data;
import cn.zpl.pojo.DownloadDTO;
import cn.zpl.pojo.VideoData;
import cn.zpl.spider.on.bilibili.common.BilibiliCommonUtils;
import cn.zpl.spider.on.bilibili.common.BilibiliProperties;
import cn.zpl.thread.OneFileOneThread;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.CrudTools;
import cn.zpl.util.DownloadTools;
import cn.zpl.util.FFMEPGToolsPatch;
import cn.zpl.util.JsonUtil;
import cn.zpl.util.SaveLog;
import cn.zpl.util.URLConnectionTool;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
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
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
@Component
public class BilibiliDownloadCoreForMusic {

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
        BilibiliDownloadCoreForMusic bilibiliDownloadCore2 = new BilibiliDownloadCoreForMusic();
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

    public void getVideoList(String uid) {
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
            data.setUrl(String.format("https://api.bilibili.com/x/web-interface/view?bvid=%1$s", videoId));
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
                result = JsonParser.parseString(Objects.requireNonNull(data.getString()));
                code = CommonIOUtils.getFromJson2Integer(result, "code");
                if (code != 0) {
                    log.error(CommonIOUtils.getFromJson2Str(result, "message"));
                    return;
                }
            }
            JsonElement pages = JsonUtil.getFromJson(jsonData, "pages");
            if (pages.isJsonArray()) {
                int count = 0;
                for (JsonElement part : pages.getAsJsonArray()) {
                    Data download = new Data();
                    if (count > 25) {
                        count = 0;
                        TimeUnit.MINUTES.sleep(20);
                    }
                    boolean success = downLoadByAPI(download, jsonData, part);
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
            exception.clear();
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
        if (isDownloaded(cid, aid)) {
            return false;
        }
        data.setUrl(String.format(BilibiliProperties.playInfoUrl, aid, bvid, cid));
        data.setHeader(BilibiliProperties.commonHeaders + properties.getCookies());
        CommonIOUtils.withTimer(data);
        JsonElement playInfo = JsonUtil.parseFromStr(data.getResult());
        int code = JsonUtil.getFromJson2Integer(playInfo, "code");
        if (code == -404) {
            log.error("视频不存在！");
            return false;
        }
        title = CommonIOUtils.filterFileName2(title);
        VideoData videoData = new VideoData();
        VideoInfo videoInfo = new VideoInfo();
        videoInfo.setOwnerName(owner_name == null || "".equalsIgnoreCase(owner_name) ? JsonUtil.getFromJson2Str(jsonData, "owner-name") : owner_name);
        videoInfo.setVideoId(cid);
        videoInfo.setAid(aid);
        videoInfo.setPageCount(videos);
        videoInfo.setTimeLength(String.valueOf(JsonUtil.getFromJson2Integer(pageJson, "duration") * 1000L));
        videoInfo.setSavedLocalName(CommonIOUtils.filterFileName2(StringEscapeUtils.escapeHtml4(pageNumber + "." + partName).replaceAll("(&[a-z]{3};)+", "")));
        videoInfo.setVideoName(title);
        videoInfo.setTitle(title);
        videoInfo.setWebsite("bilibili");
        videoInfo.setDownloadDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        videoInfo.setBid(bvid);

        List<String> path = new ArrayList<>();
        path.add(newPath.get() != null && !"".equals(newPath.get()) ? newPath.get() : properties.getVideoSavePath());
        path.add("音乐文件夹");
        String videoName = title + "(av" + avid + ")[" + bvid + "]" + ".mp3";
        if (videos > 1) {
            path.add(title + "(av" + avid + ")");
            videoName = videoInfo.getSavedLocalName() + ".mp3";
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
            return false;
        }
        if (filter && isDownloaded(cid, avid)) {
            already.set(already.get() == null ? 0 : already.get() + 1);
            VideoInfo exist = getVideoInfoByCid(cid);
            assert exist != null;
            if (exist.getLocalPath().equalsIgnoreCase(videoInfo.getLocalPath())) {
                return false;
            }
            try {
                FileUtils.moveFile(new File(exist.getLocalPath()), new File(videoInfo.getLocalPath()));
            } catch (IOException e) {
                log.error("移动失败，有可能文件重复");
                log.error("错误原因：", e);
            }
            log.debug(title);
            return false;
        }
        if (FFMEPGToolsPatch.isExists(videoData)) {
            RestResponse restResponse = crudTools.commonApiSave(videoInfo);
            if (!restResponse.isSuccess()) {
                throw new RuntimeException("保存记录失败");
            }
            return false;
        }
        JsonArray acceptQuality =
                CommonIOUtils.getFromJson2(playInfo, "data-accept_quality").getAsJsonArray();
        List<Integer> quality = new ArrayList<>();
        acceptQuality.forEach(jsonElement -> quality.add(jsonElement.getAsInt()));
        Collections.sort(quality);
        Collections.reverse(quality);
        JsonElement audioList = JsonUtil.getFromJson(playInfo, "data-dash-audio");
        doM4s(audioList, avid, bvid, videoInfo, videoData);
        if (!ffmepgToolsPatch.transferM4s2MP3(videoData)) {
            log.error("不应该出现在这，video_id：" + avid);
        }
        RestResponse restResponse = crudTools.commonApiSave(videoInfo);
        if (!restResponse.isSuccess()) {
            throw new RuntimeException("保存下载记录失败");
        }
        return restResponse.isSuccess();
    }

    private void doM4s(JsonElement audioList, String avid, String bvid, VideoInfo videoInfo, VideoData videoData) {

        List<DownloadDTO> audioDTO = new ArrayList<>();
        if (audioList.isJsonArray()) {
            audioDTO.add(buildTemplate(audioList.getAsJsonArray().get(0), bvid, avid));
        }
        //如果有指定保存路径，则使用指定的路径，否则从config.properties中读取
        videoData.setAudio(audioDTO.get(0));
        DownloadTools tools = DownloadTools.getInstance(30);
        tools.setName(videoInfo.getTitle());
        if (!audioDTO.isEmpty()) {
            addDownload(tools, audioDTO);
        }
        tools.shutdown();
        for (DownloadDTO audio : audioDTO) {
            if (audio == null || new File(audio.getSavePath()).length() == audio.getFileLength()) {
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

    private void addDownload(DownloadTools tools, List<DownloadDTO> dtoList){
        for (DownloadDTO dto : dtoList) {
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
}
