package cn.zpl.spider.on.bilibili.manga.controller;


import cn.zpl.common.bean.BilibiliManga;
import cn.zpl.common.bean.Page;
import cn.zpl.spider.on.bilibili.manga.bs.MagaDownloadCore;
import cn.zpl.spider.on.bilibili.manga.config.BilibiliMangaConfig;
import cn.zpl.spider.on.bilibili.manga.config.MyEventListener;
import cn.zpl.spider.on.bilibili.manga.thread.BuyWaitFreeEpisodeThread;
import cn.zpl.spider.on.bilibili.manga.util.BilibiliCommonUtils;
import cn.zpl.spider.on.bilibili.manga.util.BilibiliMangaProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class TestController {


    @Resource
    MagaDownloadCore magaDownloadCore;
    @Resource
    BuyWaitFreeEpisodeThread buyWaitFreeEpisodeThread;

    @Resource
    BilibiliCommonUtils utils;
    @Resource
    BilibiliMangaProperties properties;

    @GetMapping("/downloadManga/{comicId}")
    public String testEureka(@PathVariable("comicId") String comicId){
        magaDownloadCore.getComicDetail(comicId, true);
        BilibiliManga manga = utils.getComicById(comicId);
        MyEventListener.chapterTime.put(manga.getChapterWaitBuy(), manga.getWaitFreeAt());
        return "success";
    }
    @GetMapping("/downloadMangas")
    public String downloadMangas(@RequestParam("comicIds") List<String> comicIds){
        comicIds.forEach(comicId -> magaDownloadCore.getComicDetail(comicId, true));
        buyWaitFreeEpisodeThread.waitStart();
        return "success";
    }

    @GetMapping("/getMangaSavePath")
    public String test(){
        return properties.getMangaSavePath();
    }
}

