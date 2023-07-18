package cn.zpl.spider.on.bilibili.manga.thread;

import cn.zpl.common.bean.BilibiliManga;
import cn.zpl.common.bean.Page;
import cn.zpl.spider.on.bilibili.manga.bs.MangaDownloadCore;
import cn.zpl.spider.on.bilibili.manga.util.BilibiliCommonUtils;
import cn.zpl.spider.on.bilibili.manga.util.BilibiliMangaProperties;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.CrudTools;
import com.google.gson.JsonElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Slf4j
@Scope("prototype")
@Component
public class BuyWaitFreeEpisodeThread implements Callable<Map<String, Map<String, String>>> {

    @Resource
    BilibiliCommonUtils utils;

    @Resource
    BilibiliMangaProperties properties;
    @Resource
    CrudTools tools;

    @Resource
    MangaDownloadCore magaDownloadCore;

    String epId;


    public void setEpId(String epId) {
        this.epId = epId;
    }

    public void waitStart() {
        List<BilibiliManga> bilibiliMangas = tools.commonApiQuery(String.format("allow_wait_free = %1$s and wait_free_at < now() and chapter_wait_buy <> %2$s", 1, 0), null, BilibiliManga.class, new Page(1, -1));
        bilibiliMangas.forEach(o -> doBusiness(o.getChapterWaitBuy()));
    }

    private Map<String, String> doBusiness(String ep_id) {
        if (StringUtils.isEmpty(ep_id)) {
            log.error("未传入漫画信息，方法返回");
            return Collections.emptyMap();
        }
        //获取漫画信息，根据wait_free_at获取下次解锁时间，与本地时间比较如果不到解锁时间，那么下载已解锁章节
        String param = "{\"ep_id\":" + ep_id + "}";
        String result = utils.postUrl(properties.getGetEpisodeBuyInfoUrl(), param, properties.getCommonHeaders() + properties.getBilibiliCookies());
        JsonElement resultJson = CommonIOUtils.paraseJsonFromStr(result);
        if (CommonIOUtils.getFromJson2Str(resultJson, "code").equalsIgnoreCase("unauthenticated")) {
            //需要登录，那么直接退出系统
            log.error("需要重新登录");
            return Collections.emptyMap();
        }
        boolean allow_wait_free = CommonIOUtils.getFromJson2Boolean(resultJson, "data-allow_wait_free");
        boolean is_locked = CommonIOUtils.getFromJson2Boolean(resultJson, "data-is_locked");
        String wait_free_at = CommonIOUtils.getFromJson2Str(resultJson, "data-wait_free_at");
        String comic_id = CommonIOUtils.getFromJson2Str(resultJson, "data-comic_id");
        //需要解锁的解锁，已经解锁的跳过
        if (allow_wait_free && "0000-00-00 00:00:00".equalsIgnoreCase(wait_free_at)) {
            //满足条件，调用解锁方法BuyEpisode
            param = "{\"buy_method\":4,\"ep_id\":" + ep_id + ",\"comic_id\":" + comic_id + "}";
            String buyResult = utils.postUrl(properties.getBuyEpisodeUrl(), param,
                    properties.getCommonHeaders() + properties.getBilibiliCookies());
            log.debug(buyResult);
            if (CommonIOUtils.getIntegerFromJson(CommonIOUtils.paraseJsonFromStr(buyResult), "code") == 0) {
                //购买完成，调用漫画下载进程
                return magaDownloadCore.getComicDetail(comic_id, true);
            }
        }
        if (!is_locked) {
            return magaDownloadCore.getComicDetail(comic_id, true);
        }
        log.warn("章节" + ep_id + "需要" + wait_free_at + "后才能解锁");
        BilibiliManga manga = utils.getComicById(comic_id);
        manga.setChapterWaitBuy(ep_id);
        //如果allow_wait_free = false，is_locked = true并且wait_free_at = 0000-00-00
        // 00:00:00，说明等免章节已经解锁完，剩下的是收费章节，暂时将此类漫画标志位记为2
        if (!allow_wait_free && "0000-00-00 00:00:00".equalsIgnoreCase(wait_free_at)) {
            manga.setAllowWaitFree(2);
            log.warn("章节" + ep_id + "需要购买，暂时标记为完成");
        }
        manga.setWaitFreeAt(wait_free_at);
        tools.commonApiSave(manga);
        return Collections.singletonMap(ep_id, wait_free_at);
    }

    @Override
    public Map<String, Map<String, String>> call() {
        return Collections.singletonMap(epId, doBusiness(epId));
    }
}
