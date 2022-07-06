package cn.zpl.spider.on.ehentai;

import cn.zpl.spider.on.ehentai.thread.DownLoadArchiveThread;
import cn.zpl.util.DownloadTools;

public class Download {

    public static void main(String[] args) {
        String url = "https://e-hentai.org/g/2208421/a651b5d31c/";
        url = url.replaceAll("VM.+ ", "");
        String[] urls = url.split("\n");
        DownloadTools tools = DownloadTools.getInstance(20);
        for (String s : urls) {
            tools.ThreadExecutorAdd(new DownLoadArchiveThread(s));
        }
        tools.shutdown();
    }
}

