package cn.zpl.spider.on.pixiv.controller;

import cn.zpl.common.bean.PixivPictures;
import cn.zpl.pojo.DownloadDTO;
import cn.zpl.pojo.SynchronizeLock;
import cn.zpl.spider.on.pixiv.thread.GetImgUrlThread2;
import cn.zpl.spider.on.pixiv.util.CommonUtils;
import cn.zpl.thread.OneFileOneThread;
import cn.zpl.util.DownloadTools;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Vector;

@RestController
public class PixivController {

    @Resource
    CommonUtils commonUtils;

    @GetMapping("download/{pid}")
    public String download(@PathVariable("pid") String pid) {
        Vector<DownloadDTO> dtoList = new Vector<>();
        PixivPictures pic = new PixivPictures();
        pic.setId(pid);
        new GetImgUrlThread2(pic, dtoList).run();
        //所有的下载共用一个锁
        SynchronizeLock lock = new SynchronizeLock();
        DownloadTools tools = DownloadTools.getInstance(20, "");
        dtoList.forEach(downloadDTO -> {
            downloadDTO.setSynchronizeLock(lock);
            tools.ThreadExecutorAdd(new OneFileOneThread(downloadDTO));
        });
        tools.shutdown();
        return "success";
    }


    @GetMapping("downloadByUid/{uid}")
    public String downloadByUid(@PathVariable("uid") String uid) {
        commonUtils.downloadByUid(uid);
        return "success";
    }
}
