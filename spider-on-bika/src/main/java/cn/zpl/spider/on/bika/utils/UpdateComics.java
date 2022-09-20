package cn.zpl.spider.on.bika.utils;

import cn.zpl.common.bean.Bika;
import cn.zpl.common.bean.BikaList;
import cn.zpl.spider.on.bika.thread.BikaComicThread;
import cn.zpl.util.CrudTools;
import cn.zpl.util.DownloadTools;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;
@SpringBootTest
public class UpdateComics {

    @Resource
    CrudTools tools;

    @Resource
    BikaUtils bikaUtils;

    @Test
    public void main() {

        DownloadTools tool = DownloadTools.getInstance(10);
        tool.setName("漫画");
        tool.setSleepTimes(10000);
//        List<BikaList> list = tools.commonApiQueryBySql("select a.epsCount, a.realPagesCount, a.* from v_bika_list a where not exists(select 1 from bika b where b.id = a.id and b.isDeleted = 1) and realPagesCount <> epsCount", BikaList.class);
        List<BikaList> list = tools.commonApiQueryBySql("select * from bika_list where title like '%女主陷阱%';", BikaList.class);
        list.forEach(bikaList -> tool.ThreadExecutorAdd(new BikaComicThread(bikaList.getId(), true)));
        tool.shutdown();
    }
}
