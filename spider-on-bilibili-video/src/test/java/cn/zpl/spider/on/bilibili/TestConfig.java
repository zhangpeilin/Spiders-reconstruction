package cn.zpl.spider.on.bilibili;
import cn.zpl.spider.on.bilibili.common.BilibiliConfigParams;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 *
 * @author zhangpl1
 * @date 2022/8/1
 */
@SpringBootTest
public class TestConfig {

    @Resource
    BilibiliConfigParams configParams;
    @Test
    public void contextLoad() {
        System.out.println(configParams.properties.cookies);
    }
}
