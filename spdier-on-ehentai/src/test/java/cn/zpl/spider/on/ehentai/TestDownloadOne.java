package cn.zpl.spider.on.ehentai;

import cn.zpl.spider.on.ehentai.thread.DownLoadArchiveThread;
import cn.zpl.util.DownloadTools;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestDownloadOne {

    @Test
    public void doBusiness(){

        String url = "https://e-hentai.org/g/2015558/a362384eae/\n" +
                "VM23:3 https://e-hentai.org/g/1929882/0a00a6ef0f/\n" +
                "VM23:3 https://e-hentai.org/g/1872939/da292fed99/\n" +
                "VM23:3 https://e-hentai.org/g/1814338/0dc0aaf092/";
        url = url.replaceAll("VM.+ ", "");
        String[] urls = url.split("\n");
        DownloadTools tools = DownloadTools.getInstance(20);
        for (String s : urls) {
            tools.ThreadExecutorAdd(new DownLoadArchiveThread(s));
        }
        tools.shutdown();
    }
}
