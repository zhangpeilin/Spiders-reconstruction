package cn.zpl.spider.on.bilibili.manga.thread;

import cn.zpl.config.SpringContext;
import cn.zpl.pojo.DownloadDTO;
import cn.zpl.pojo.SynchronizeLock;
import cn.zpl.spider.on.bilibili.manga.util.BilibiliCommonUtils;
import cn.zpl.spider.on.bilibili.manga.util.BilibiliMangaProperties;
import cn.zpl.thread.OneFileOneThread;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.DownloadTools;
import cn.zpl.util.SaveLog;
import cn.zpl.util.SaveLogForImages;
import cn.zpl.util.URLConnectionTool;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
public class ChapterThread implements Callable<Map<String, Object>> {


    private final JsonElement element;
    private final Vector<Future<DownloadDTO>> futureVector = new Vector<>();

    BilibiliMangaProperties bilibiliMangaProperties;


    public ChapterThread(JsonElement element) {
        this.element = element;
        this.bilibiliMangaProperties = SpringContext.getBeanWithGenerics(BilibiliMangaProperties.class);
    }

    @Override
    public Map<String, Object> call() {
        try {
            return domain();
        } catch (Exception e) {
            log.error("章节线程出错：\n",e);
            CommonIOUtils.waitSeconds(5);
            return call();
        }
    }

    public Map<String, Object> domain() {
        Map<String, Object> result = new HashMap<>();
        int chapter_id = CommonIOUtils.getIntegerFromJson(element, "id");
        String order = CommonIOUtils.getFromJson2Str(element, "ord");
        String title = CommonIOUtils.getFromJson2Str(element, "title");
        boolean is_locked = CommonIOUtils.getFromJson2Boolean(element, "is_locked");
        boolean is_in_free = CommonIOUtils.getFromJson2Boolean(element, "is_in_free");
        title = CommonIOUtils.filterFileName(title);
        element.getAsJsonObject().addProperty("title", title);
        int comic_id = CommonIOUtils.getIntegerFromJson(element, "comic_id");
        String comic_name = CommonIOUtils.getFromJson2Str(element, "comic_name");
        List<String> pathMake = new ArrayList<>();
        pathMake.add(bilibiliMangaProperties.getMangaSavePath());
        pathMake.add(CommonIOUtils.generateComicFolderName(comic_name, comic_id));
        result.put("save_path", CommonIOUtils.makeFilePath(pathMake, null));
        pathMake.add("".equalsIgnoreCase(title) ? order : CommonIOUtils.generateChapterName(title, order));
        result.put("chapter_id", chapter_id);
        result.put("order", order);
        result.put("download_status", 0);
        result.put("buy_status", 0);
        if (SaveLogForImages.isChapterCompelete(CommonIOUtils.makeFilePath(pathMake, ""))) {
            result.put("download_status", 1);
            result.put("buy_status", 1);
            log.debug("已下载，跳过");
            return result;
        }
        //没有解锁并且没有限免的章节跳过，不用请求了
        if (!is_in_free && is_locked) {
            result.put("buy_status", 0);
            return result;
        }

        //获取加密的index.dat文件请求路径
        String IndexInfo = URLConnectionTool.postUrl(bilibiliMangaProperties.getImageIndexUrl,
                "{\"ep_id\":" + chapter_id +
                        "}", bilibiliMangaProperties.getCommonHeaders() + bilibiliMangaProperties.getBilibiliCookies());
        //https://manga.hdslb.com/bfs/manga/26484/309850/data.index?token=529914acc997e3166f4504a4adac4130&ts=5e425614
        //https://manga.hdslb.com
        ///bfs/manga/26484/309850/data.index?token=9b57200fa0112d3685feaecca338559a&ts=5e425676
        if (CommonIOUtils.getFromJson2Integer(CommonIOUtils.paraseJsonFromStr(IndexInfo), "code") == 1 || ("need buy " +
                "episode").equalsIgnoreCase(CommonIOUtils.getFromJson2Str(CommonIOUtils.paraseJsonFromStr(IndexInfo), "msg"))) {
            //需要购买，这样的章节跳过并记录数据库comic_id,chapter_id,title,comic_name
            result.put("buy_status", 0);
            return result;
        }
        String host = CommonIOUtils.getFromJson2(Objects.requireNonNull(CommonIOUtils.paraseJsonFromStr(IndexInfo))
                , "data-host").getAsString();
        String path = CommonIOUtils.getFromJson2(Objects.requireNonNull(CommonIOUtils.paraseJsonFromStr(IndexInfo))
                , "data-path").getAsString();
        byte[] encryption = URLConnectionTool.getMethod(host + path, bilibiliMangaProperties.getCommonHeaders());
        JsonElement picsJson = BilibiliCommonUtils.decryptIndexFile(encryption, comic_id, chapter_id);
        JsonElement clips = CommonIOUtils.getFromJson2(picsJson, "clips");
        JsonElement pics = CommonIOUtils.getFromJson2(picsJson, "pics");
        DownloadTools tools = DownloadTools.getInstance(10);
        tools.setName(title + "：" + chapter_id);
        if (clips.isJsonArray() && pics.isJsonArray() && clips.getAsJsonArray().size() == pics.getAsJsonArray().size()) {
            JsonArray clipsArray = clips.getAsJsonArray();
            JsonArray picsArray = pics.getAsJsonArray();
            int i = 0;
            for (JsonElement jsonElement : clipsArray) {
                futureVector.add(tools.getExecutor().submit(new ImageThread(element, jsonElement, picsArray.get(i))));
                i++;
            }
        } else {
            //数量不匹配会有大问题
            log.error("解析出错，必须检查");
            throw new RuntimeException("图片和切片数量不匹配，解析出错，必须检查");
        }
        tools.shutdown();
        List<Future<DownloadDTO>> done =
                futureVector.stream().filter(Future::isDone).collect(Collectors.toList());
        if (done.size() == clips.getAsJsonArray().size()) {
            tools.restart(10);
            SynchronizeLock lock = new SynchronizeLock();
            done.forEach(downloadDTOFuture -> {
                try {
                    downloadDTOFuture.get().setSynchronizeLock(lock);
                    downloadDTOFuture.get().setImage(true);
                    //表明下载报错时要一直重试
                    downloadDTOFuture.get().setAlwaysRetry();
                    tools.ThreadExecutorAdd(new OneFileOneThread(downloadDTOFuture.get()));
                } catch (InterruptedException | ExecutionException e) {
                    log.error("获取线程池执行结果失败：\n", e);
                }
            });
        } else {
            log.error("图片和切片数量不匹配，解析出错，必须检查");
            System.exit(1);
        }
        tools.shutdown();
        //图片文件如果数量跟线程池执行过的数量不匹配说明没有完全下载完成，不能记录日志
        long completeCount = done.stream().map(downloadDTOFuture -> {
            try {
                return downloadDTOFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            return new DownloadDTO();
        }).filter(SaveLogForImages::isCompelete).count();
        //增加条件，如果完成数跟index中的图片数不匹配不记录日志，并且购买标记为0
        if (completeCount != clips.getAsJsonArray().size()) {
            result.put("buy_status", 0);
            return result;
        }
        if (!done.isEmpty()) {
            try {
                File log = new File(done.get(0).get().getSavePath());
                SaveLog.saveLog(log.getParentFile());
        //如果章节下载完成并且数量匹配，那么删除章节内的list.txt文件方便阅读
                File innerLog = new File(log.getParentFile(), "list.txt");
                if (innerLog.exists()) {
                    FileUtils.forceDelete(innerLog);
                }
                result.put("save_path", log.getParentFile().getParent());
            } catch (InterruptedException | ExecutionException | IOException e) {
                log.error("保存日志失败\n", e);
            }
        }
        result.put("buy_status", 1);
        result.put("download_status", 1);
        return result;
    }
}
