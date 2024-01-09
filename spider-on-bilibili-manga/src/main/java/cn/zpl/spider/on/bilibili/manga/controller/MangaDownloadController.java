package cn.zpl.spider.on.bilibili.manga.controller;


import cn.zpl.common.bean.BilibiliManga;
import cn.zpl.config.SpringContext;
import cn.zpl.spider.on.bilibili.manga.bs.MangaDownloadCore;
import cn.zpl.spider.on.bilibili.manga.config.MyEventListener;
import cn.zpl.spider.on.bilibili.manga.thread.BatchBuyEpisodeThread;
import cn.zpl.spider.on.bilibili.manga.thread.BuyWaitFreeEpisodeThread;
import cn.zpl.spider.on.bilibili.manga.util.BilibiliCommonUtils;
import cn.zpl.spider.on.bilibili.manga.util.BilibiliMangaProperties;
import cn.zpl.spider.on.bilibili.manga.util.EpEntity;
import cn.zpl.util.DownloadTools;
import com.alibaba.fastjson.JSON;
import com.google.gson.JsonArray;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class MangaDownloadController {


    @Resource
    MangaDownloadCore mangaDownloadCore;
    @Resource
    BuyWaitFreeEpisodeThread buyWaitFreeEpisodeThread;

    @Resource
    BilibiliCommonUtils utils;
    @Resource
    BilibiliMangaProperties properties;

    @GetMapping("/downloadManga/{comicId}")
    public String testEureka(@PathVariable("comicId") String comicId){
        mangaDownloadCore.getComicDetail(comicId, true);
        BilibiliManga manga = utils.getComicById(comicId);
        MyEventListener.chapterTime.put(manga.getChapterWaitBuy(), manga.getWaitFreeAt());
        return "success";
    }
    @GetMapping("/downloadMangas")
    public String downloadMangas(@RequestParam("comicIds") List<String> comicIds){
        comicIds.forEach(comicId -> mangaDownloadCore.getComicDetail(comicId, true));
        buyWaitFreeEpisodeThread.waitStart();
        return "success";
    }

    @GetMapping("/downloadAllBoughtManga")
    public String downloadAllBoughtManga() {
        mangaDownloadCore.downloadAllBoughtManga(1);
        return "更新完成";
    }

    @GetMapping("/getMangaSavePath")
    public String test(){
        return properties.getMangaSavePath();
    }

    @PostMapping("/batchBuy")
    public String batchBuyEp(@RequestParam("comicIds") List<String> comicList, @RequestParam("buyTime") Integer buyTime, @RequestParam(value = "fromEpId", required = false) Integer fromEpId) {
        fromEpId = fromEpId == null ? 0 : fromEpId;
        DownloadTools tools = DownloadTools.getInstance(2);
        for (String comicId : comicList) {
            JsonArray epIds = mangaDownloadCore.getEpIds(comicId);
            List<EpEntity> objects = JSON.parseArray(epIds.toString(), EpEntity.class);
            Integer finalFromEpId = fromEpId;
            List<EpEntity> sortedList = objects.stream().filter(EpEntity::is_locked).filter(eps -> Integer.parseInt(eps.getId()) > finalFromEpId).sorted(Comparator.comparingInt(EpEntity::getOrd)).collect(Collectors.toList());
            for (EpEntity ep : sortedList) {
                if (buyTime-- > 0) {
                    System.out.println("当前下载章节：" + ep.getOrd());
                    BatchBuyEpisodeThread episodeThread = SpringContext.getBeanWithGenerics(BatchBuyEpisodeThread.class);
                    episodeThread.setEpId(ep.getId());
                    tools.getExecutor().submit(episodeThread);
                } else {
                    break;
                }
            }
            tools.shutdown();
            new Thread(() -> mangaDownloadCore.getComicDetail(comicId, true)).start();
        }
        return "下载成功";
    }
}

