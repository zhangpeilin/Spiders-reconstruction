package cn.zpl.spider.on.bilibili.manga.bs;

import cn.zpl.annotation.DistributeLock;
import cn.zpl.annotation.DistributedLockKey;
import cn.zpl.common.bean.BilibiliManga;
import cn.zpl.config.SpringContext;
import cn.zpl.spider.on.bilibili.manga.thread.ChapterThread;
import cn.zpl.spider.on.bilibili.manga.util.BilibiliMangaProperties;
import cn.zpl.spider.on.bilibili.manga.util.BilibiliProperties;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.CrudTools;
import cn.zpl.util.DownloadTools;
import cn.zpl.util.URLConnectionTool;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
@Component
public class MangaDownloadCore {

    @Resource
    BilibiliMangaProperties mangaProperties;

    @Resource
    BilibiliProperties bilibiliProperties;
    @Resource
    CrudTools crudTools;
    public void test() {
        getComicDetail("25501".replace("mc", "").replaceAll("[()]", ""), true);
//        getComicDetailForFree("mc26787");
    }

    void getComicDetailForFree(String comic_id) {
        getComicDetail(comic_id, false);
    }

    @Async("MyAsync")
    public void downloadAllBoughtManga(int page) {
        String boughtMangaJson = URLConnectionTool.postUrl(mangaProperties.getGetAutoBuyComics(),
                "{\"page_num\": " + page + ", \"page_size\": 50}", mangaProperties.getCommonHeaders() + bilibiliProperties.getCookies());
        JsonElement json = CommonIOUtils.paraseJsonFromStr(boughtMangaJson);
        JsonElement data = CommonIOUtils.getFromJson2(json, "data");
        MangaDownloadCore downloadCore = SpringContext.getBeanWithGenerics(MangaDownloadCore.class);
        if (data.isJsonArray() && data.getAsJsonArray().size() > 1) {
            for (JsonElement jsonElement : data.getAsJsonArray()) {
                downloadCore.getComicDetail(CommonIOUtils.getFromJson2Str(jsonElement, "comic_id"), true);
            }
            downloadAllBoughtManga(++page);
        }
    }

    @DistributeLock(value = "redissionLock:comic2Buy", waitTime = 500, holdTime = 500)
    public Map<String, String> getComicDetail(@DistributedLockKey String comic_id, boolean needLogin) {
        try {
            Vector<Future<Map<String, Object>>> futureVector = new Vector<>();
            String detailStr = URLConnectionTool.postUrl(mangaProperties.getGetComicDetailUrl(),
                    "{\"comic_id\":" + comic_id +
                            "}", needLogin ? mangaProperties.getCommonHeaders() + bilibiliProperties.getCookies() :
                            mangaProperties.getCommonHeaders());
            JsonElement detailJson = CommonIOUtils.paraseJsonFromStr(detailStr);
            if (CommonIOUtils.getFromJson2Integer(detailJson, "code") != 0) {
                log.error("返回结果不符合预期，请检查" + detailStr);
                BilibiliManga manga = new BilibiliManga();
                manga.setComicId(comic_id);
                manga.setAllowWaitFree(2);
                manga.setMark(detailStr);
                crudTools.commonApiSave(manga);
                return Collections.emptyMap();
            }
            JsonElement ep_list = CommonIOUtils.getFromJson2(detailJson, "data-ep_list");
            String comic_name = CommonIOUtils.filterFileName(CommonIOUtils.getFromJson2Str(detailJson, "data-title"));
            String wait_free_at = CommonIOUtils.getFromJson2Str(detailJson, "data-wait_free_at");
            String allow_wait_free = CommonIOUtils.getFromJson2Str(detailJson, "data-allow_wait_free");
            DownloadTools tools = DownloadTools.getInstance(5);
            tools.setName(comic_name);
            //改造，有未购买章节但是allow_wait_free和wait_free_at页签表明可以限免时，请求最小的未购买章节，那么需要线程池有返回结果
            if (ep_list.isJsonArray()) {
                ep_list.getAsJsonArray().forEach(jsonElement -> {
                    jsonElement.getAsJsonObject().addProperty("comic_id", comic_id);
                    jsonElement.getAsJsonObject().addProperty("comic_name", comic_name);
                    futureVector.add(tools.getExecutor().submit(new ChapterThread((jsonElement))));
                });
            }
            tools.shutdown();
            final double[] min = {0, 0};
            BilibiliManga manga = new BilibiliManga();
            futureVector.forEach(mapFuture -> {
                try {
                    manga.setSavePath(manga.getSavePath() == null ? String.valueOf(mapFuture.get().get("save_path")) : manga.getSavePath());
                    //过滤完成并且buy_status状态是1的结果
                    if (mapFuture.isDone() && mapFuture.get().get("buy_status").equals(0)) {
                        if (min[0] == 0) {
                            min[0] = Double.parseDouble((String) mapFuture.get().get("order"));
                            min[1] = (int) mapFuture.get().get("chapter_id");
                        } else if (min[0] > Double.parseDouble((String) mapFuture.get().get("order"))) {
                            min[0] = Double.parseDouble((String) mapFuture.get().get("order"));
                            min[1] = (int) mapFuture.get().get("chapter_id");
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
            log.debug("最小的未购买章节是：" + min[1]);
            manga.setComicId(comic_id);
            manga.setTitle(comic_name);
            manga.setChapterWaitBuy(String.valueOf(min[1]));
            manga.setWaitFreeAt(wait_free_at);
            manga.setAllowWaitFree("false".equalsIgnoreCase(allow_wait_free) ? 0 : 1);
            crudTools.commonApiSave(manga);
            return Collections.singletonMap(String.valueOf(min[1]), wait_free_at);
        } catch (Exception e) {

            log.error("漫画第一层解析失败：\n", e);
            return getComicDetail(comic_id, needLogin);
        }
    }

    public JsonArray getEpIds(String comicId) {
        try {
            String detailStr = URLConnectionTool.postUrl(mangaProperties.getGetComicDetailUrl(),
                    "{\"comic_id\":" + comicId +
                            "}", mangaProperties.getCommonHeaders() + bilibiliProperties.getCookies());
            JsonElement detailJson = CommonIOUtils.paraseJsonFromStr(detailStr);
            if (CommonIOUtils.getFromJson2Integer(detailJson, "code") != 0) {
                log.error("返回结果不符合预期，请检查" + detailStr);
                BilibiliManga manga = new BilibiliManga();
                manga.setComicId(comicId);
                manga.setAllowWaitFree(2);
                manga.setMark(detailStr);
                crudTools.commonApiSave(manga);
            }
            JsonElement list = CommonIOUtils.getFromJson2(detailJson, "data-ep_list");
            return list.isJsonArray() ? list.getAsJsonArray() : null;
        } catch (Exception e) {
            log.error("获取章节列表失败", e);
            return null;
        }
    }
}
