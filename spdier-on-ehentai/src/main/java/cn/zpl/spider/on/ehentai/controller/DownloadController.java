package cn.zpl.spider.on.ehentai.controller;

import cn.zpl.spider.on.ehentai.config.EhentaiConfig;
import cn.zpl.spider.on.ehentai.thread.DownLoadArchiveThread;
import cn.zpl.spider.on.ehentai.thread.DownloadPageThread;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class DownloadController {

    @Resource
    EhentaiConfig config;
    @PostMapping("/download")
    public String downloadByUrl(@RequestParam("url") String url) {
        DownLoadArchiveThread downLoadArchiveThread = new DownLoadArchiveThread(url);
        downLoadArchiveThread.run();
        return "下载成功";
    }

    @PostMapping("/downloadPage")
    public String downloadPage(@RequestParam("url") String url) {
        DownloadPageThread downLoadArchiveThread = new DownloadPageThread();
        downLoadArchiveThread.setUrl(url);
        downLoadArchiveThread.run();
        return "下载成功";
    }
}
