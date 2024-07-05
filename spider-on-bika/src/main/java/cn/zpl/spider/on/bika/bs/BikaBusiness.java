package cn.zpl.spider.on.bika.bs;

import cn.zpl.common.bean.BikaList;
import cn.zpl.common.bean.RestResponse;
import cn.zpl.spider.on.bika.thread.BikaComicThread;
import cn.zpl.util.CrudTools;
import cn.zpl.util.DownloadTools;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class BikaBusiness {
    @Resource
    CrudTools tools;


    @Async("MyAsync")
    public void updateAllExistBika(){

        long lastDate = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.of(0, 0)).toEpochSecond(ZoneOffset.UTC) * 1000;
        List<BikaList> list = tools.commonApiQueryBySql("SELECT * FROM bika WHERE is_deleted <> 1 AND local_path IS NOT NULL AND downloaded_at < " + lastDate +
                " AND (categories NOT LIKE '%CG雜圖%' OR (categories LIKE '%CG雜圖%' AND pages_count <= 300)) ORDER BY likes_count DESC LIMIT 3000;", BikaList.class);
        DownloadTools tool = DownloadTools.getInstance(5);
        tool.setName("漫画");
        tool.setSleepTimes(10000);
        list.forEach(bikaList -> tool.ThreadExecutorAdd(new BikaComicThread(bikaList.getId())));
        tool.shutdown();
    }
}
