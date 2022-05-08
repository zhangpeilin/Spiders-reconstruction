package cn.zpl.spider.on.ehentai;

import cn.zpl.spider.on.ehentai.thread.DownLoadArchiveThread;
import cn.zpl.util.DownloadTools;

public class Download {

    public static void main(String[] args) {
        String url = "https://e-hentai.org/g/143/e5fee59d04/";
        String[] urls = url.split("\n");
        DownloadTools tools = DownloadTools.getInstance(20);
        for (String s : urls) {
            tools.ThreadExecutorAdd(new DownLoadArchiveThread(s));
        }
        tools.shutdown();
    }
}

