package cn.zpl.spider.on.ehentai;

import cn.zpl.spider.on.ehentai.thread.DownloadPageThread;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestPageDownd {
    @Test
    public void page() {
        DownloadPageThread downloadPageThread = new DownloadPageThread();
        downloadPageThread.setUrl("https://e-hentai.org/?f_search=artist%3Atake%24+chinese");
        downloadPageThread.run();
    }
}
