package cn.zpl.spider.on.bilibili.manga;

import cn.zpl.spider.on.bilibili.manga.bs.MagaDownloadCore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class MangaCoreTest {

    @Resource
    MagaDownloadCore magaDownloadCore;
    @Test
    public void test() {
        magaDownloadCore.test();
    }
}
