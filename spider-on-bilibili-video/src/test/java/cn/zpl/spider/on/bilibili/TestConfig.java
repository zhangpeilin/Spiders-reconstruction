package cn.zpl.spider.on.bilibili;

import cn.zpl.common.bean.Bika;
import cn.zpl.common.bean.ExceptionList;
import cn.zpl.common.bean.NasPage;
import cn.zpl.common.bean.VideoInfo;
import cn.zpl.config.SpringContext;
import cn.zpl.config.UrlConfig;
import cn.zpl.spider.on.bilibili.common.BilibiliCommonUtils;
import cn.zpl.spider.on.bilibili.common.BilibiliConfigParams;
import cn.zpl.util.CrudTools;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.Resource;
import java.io.File;
import java.util.Collections;
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

    @Resource
    BilibiliCommonUtils bilibiliCommonUtils;

    @Test
    public void contextLoad() {
        List<VideoInfo> bv1hR4y1B7zH = tools.commonApiQuery(String.format("bid = %1$s", "BV1WM4y167Tx"), VideoInfo.class);
        System.out.println(bv1hR4y1B7zH);
        if (new File(bv1hR4y1B7zH.get(0).getLocalPath()).exists()) {
            System.out.println(bv1hR4y1B7zH.get(0).getLocalPath() + "存在");
            BilibiliDownloadCore2 bilibiliDownloadCore2 = new BilibiliDownloadCore2();
            bilibiliDownloadCore2.downloadList(Collections.singletonList("BV1WM4y167Tx"));
        }
    }

    @Test
    public void test2() {
        System.out.println(bilibiliCommonUtils.getExists("900735459"));
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
