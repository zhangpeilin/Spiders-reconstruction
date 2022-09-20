package cn.zpl.spider.on.bika;

import cn.zpl.spider.on.bika.common.BikaParams;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 *
 * @author zhangpl1
 * @date 2022/8/4
 */
@SpringBootTest
public class ApplicationConfigTest {

    @Resource
    BikaParams params;
    @Test
    public void loadConfig() {
        System.out.println(params.getEmail());
    }

    @Test
    public void domain(){
        BikaUtils bikaUtils = SpringContext.getBeanWithGenerics(BikaUtils.class);
        bikaUtils.search("めりちゃんどり");
    }
}
