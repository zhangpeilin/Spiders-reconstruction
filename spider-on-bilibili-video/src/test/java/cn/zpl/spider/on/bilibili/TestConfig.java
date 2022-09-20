package cn.zpl.spider.on.bilibili;

import cn.zpl.common.bean.Bika;
import cn.zpl.common.bean.NasPage;
import cn.zpl.config.SpringContext;
import cn.zpl.config.UrlConfig;
import cn.zpl.spider.on.bilibili.common.BilibiliConfigParams;
import cn.zpl.util.CrudTools;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 *
 * @author zhangpl1
 * @date 2022/8/1
 */
@SpringBootTest
@ComponentScan("cn.zpl")
public class TestConfig {

    @Resource
    BilibiliConfigParams configParams;
    @Resource
    CrudTools tools;

    @Test
    public void contextLoad() {
        System.out.println(configParams.properties.cookies);
    }

    @Test
    public void test() {
        UrlConfig config = new UrlConfig();
        config.setCommonQueryUrl("http://localhost:8080/common/dao/api/query/%1$s?fetchProperties=[%2$s]&condition=[%3$s]&size=%4$s");
        config.setCommonSaveUrl("http://localhost:8080/common/dao/api/save");
        CrudTools crudTools = SpringContext.getBeanWithGenerics(CrudTools.class);
        List<NasPage> nasPages = crudTools.commonApiQueryBySql("offset=" + 500, NasPage.class);
        if (nasPages.isEmpty()) {
            return;
        }
        String string = new String(nasPages.get(0).getResult());
        System.out.println(string);
    }
}

class testClassPath{
    public static void main(String[] args) {
        UrlConfig config = new UrlConfig();
        config.setCommonQueryUrl("http://localhost:8080/common/dao/api/query/%1$s?fetchProperties=[%2$s]&condition=[%3$s]&size=%4$s");
        config.setCommonSaveUrl("http://localhost:8080/common/dao/api/save");
        CrudTools crudTools = SpringContext.getBeanWithGenerics(CrudTools.class);
        List<NasPage> nasPages = crudTools.commonApiQueryBySql("offset=" + 500, NasPage.class);
        if (nasPages.isEmpty()) {
            return;
        }
        String string = new String(nasPages.get(0).getResult());
        System.out.println(string);
    }
}
