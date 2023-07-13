package cn.zpl.spider.on.ehentai;

import cn.zpl.spider.on.ehentai.thread.DownLoadArchiveThread;
import cn.zpl.util.DownloadTools;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestDownloadOne {

    @Test
    public void doBusiness(){

        String url = "https://e-hentai.org/g/2588345/44031c0bb3/";
        url = url.replaceAll("VM.+ ", "");
        String[] urls = url.split("\n");
        DownloadTools tools = DownloadTools.getInstance(3);
        for (String s : urls) {
            tools.ThreadExecutorAdd(new DownLoadArchiveThread(s));
        }
        tools.shutdown();
    }
}
