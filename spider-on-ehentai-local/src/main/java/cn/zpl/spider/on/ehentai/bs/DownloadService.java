package cn.zpl.spider.on.ehentai.bs;


import cn.zpl.config.SpringContext;
import cn.zpl.spider.on.ehentai.thread.DownLoadArchiveThread;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DownloadService {


    @Async("MyAsync")
    public void downTheOne(String url, int cost, boolean isDownload){
        DownLoadArchiveThread downLoadArchiveThread = SpringContext.getBeanWithGenerics(DownLoadArchiveThread.class);
        downLoadArchiveThread.setUrl(url);
        downLoadArchiveThread.setDownload(isDownload);
        downLoadArchiveThread.setCost(cost);
        downLoadArchiveThread.run();
    }
}
