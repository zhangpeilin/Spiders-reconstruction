package cn.zpl.spider.on.bilibili.manga;

import cn.zpl.common.bean.BilibiliManga;
import cn.zpl.config.SpringContext;
import cn.zpl.util.CrudTools;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.loadbalancer.RestTemplateCustomizer;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
public class MangaBeanTest {

    @Resource
    CrudTools<BilibiliManga> tools;

    @Test
    public void test() {
        Object bean = SpringContext.getBeanWithGenerics(RestTemplateCustomizer.class);
        tools.getConfig().setCommonQueryUrl("http://common-gateway/common-dao-center/common/dao/api/query/%1$s?fetchProperties=[%2$s]&condition=[%3$s]&page=%4$s");
        List<BilibiliManga> bilibiliMangas = tools.commonApiQueryBySql("select * from bilibili_manga where comic_id = '27491'", BilibiliManga.class);
        System.out.println(bilibiliMangas);
    }
}
