package cn.zpl.spider.on.bilibili.manga;

import cn.zpl.common.bean.BilibiliManga;
import cn.zpl.spider.on.bilibili.manga.bs.MagaDownloadCore;
import cn.zpl.spider.on.bilibili.manga.thread.BuyWaitFreeEpisodeThread;
import cn.zpl.spider.on.bilibili.manga.util.BilibiliCommonUtils;
import cn.zpl.util.CrudTools;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
public class WaitForFreeTest {

    @Resource
    BuyWaitFreeEpisodeThread buyWaitFreeEpisodeThread;
    @Resource
    BilibiliCommonUtils utils;
    @Resource
    CrudTools tools;
    @Test
    public void test() {
//        BilibiliManga manga = utils.getComicById("29318");
//        buyWaitFreeEpisodeThread.test();
        List<BilibiliManga> bilibiliMangas = tools.commonApiQuery(String.format(" comic_id = %1$s", "29318"), BilibiliManga.class);
        System.out.println(bilibiliMangas);
    }
}
