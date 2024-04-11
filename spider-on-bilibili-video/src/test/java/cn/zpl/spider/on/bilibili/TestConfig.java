package cn.zpl.spider.on.bilibili;

import cn.zpl.common.bean.NasPage;
import cn.zpl.common.bean.VideoInfo;
import cn.zpl.config.SpringContext;
import cn.zpl.config.UrlConfig;
import cn.zpl.spider.on.bilibili.common.BilibiliCommonUtils;
import cn.zpl.util.CrudTools;
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
@ComponentScan("cn.zpl")
public class TestConfig {

    @Resource
    CrudTools tools;

    @Resource
    BilibiliCommonUtils bilibiliCommonUtils;

    public void contextLoad() {
        List<VideoInfo> bv1hR4y1B7zH = tools.commonApiQuery(String.format("bid = %1$s", "BV1WM4y167Tx"), VideoInfo.class);
        System.out.println(bv1hR4y1B7zH);
        if (new File(bv1hR4y1B7zH.get(0).getLocalPath()).exists()) {
            System.out.println(bv1hR4y1B7zH.get(0).getLocalPath() + "存在");
            BilibiliDownloadCore bilibiliDownloadCore2 = new BilibiliDownloadCore();
            bilibiliDownloadCore2.downloadList(Collections.singletonList("BV1WM4y167Tx"));
        }
    }

    public void test2() {
        System.out.println(bilibiliCommonUtils.getVideoInfo("900735459"));
    }

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
