package cn.zpl.spider.on.bilibili;
import cn.zpl.common.bean.Bika;
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
    CrudTools<Bika> tools;
    @Test
    public void contextLoad() {
        System.out.println(configParams.properties.cookies);
    }

    @Test
    public void testCurlTools() {
        List<Bika> bikaList = tools.queryAll(Bika.class);
        System.out.println(bikaList);
    }
}
