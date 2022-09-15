package cn.zpl.spider.on.bilibili.manga.bs;

import cn.zpl.pojo.OrigionalDTO;
import cn.zpl.spider.on.bilibili.manga.thread.ChapterThread;
import cn.zpl.spider.on.bilibili.manga.util.BilibiliStaticParams;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.CrudTools;
import cn.zpl.util.DownloadTools;
import cn.zpl.util.URLConnectionTool;
import com.google.gson.JsonElement;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
public class MagaDownloadCore {

    private static MagaDownloadCore core;

    static MagaDownloadCore getInstance() {
        if (core == null) {
            core = new MagaDownloadCore();
        }
        return core;
    }

    public void test() {
        getComicDetail("mc27306".replace("mc", "").replaceAll("[()]", ""), true);
//        getComicDetailForFree("mc26787");
    }

    void getComicDetailForFree(String comic_id) {
        getComicDetail(comic_id, false);
    }

    String getComicDetail(String comic_id, boolean needLogin) {
        try {
            Vector<Future<Map<String, Object>>> futureVector = new Vector<>();
            String detailStr = URLConnectionTool.postUrl(BilibiliStaticParams.getComicDetailUrl,
                    "{\"comic_id\":" + comic_id +
                            "}", needLogin ? BilibiliStaticParams.commonHeaders + BilibiliStaticParams.bilibiliCookies :
                            BilibiliStaticParams.commonHeaders);
            JsonElement detailJson = CommonIOUtils.paraseJsonFromStr(detailStr);
            if (CommonIOUtils.getFromJson2Integer(detailJson, "code") != 0) {
                log.error("返回结果不符合预期，请检查" + detailStr);
                OrigionalDTO data2Save = new OrigionalDTO();
                data2Save.put("comic_id", comic_id);
                data2Save.put("allow_wait_free", 2);
                data2Save.put("mark", detailStr);
//                DBManager.getInstance().saveOrUpdateByTableName("bilibili_manga", data2Save);
                CrudTools.commonApiSave(data2Save);
                return detailStr;
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
            OrigionalDTO data2Save = new OrigionalDTO();
            futureVector.forEach(mapFuture -> {
                try {
                    if (data2Save.get("save_path") == null) {
                        data2Save.put("save_path", mapFuture.get().get("save_path"));
                    }
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
            data2Save.put("comic_id", comic_id);
            data2Save.put("title", comic_name);
            data2Save.put("chapter_wait_buy", min[1]);
            data2Save.put("wait_free_at", wait_free_at);
            data2Save.put("allow_wait_free", allow_wait_free);
//            DBManager.getInstance().saveOrUpdateByTableName("bilibili_manga", data2Save);
            CrudTools.commonApiSave(data2Save);
            return data2Save.get("save_path") == null ? "" : data2Save.get("save_path").toString();
        } catch (Exception e) {
            log.error("漫画第一层解析失败：\n", e);
            return getComicDetail(comic_id, needLogin);
        }
    }
}
