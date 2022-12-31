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
        bikaUtils.search("女子写真部", true);
//        bikaUtils.downloadById("627ca33a75ab703dacb84cc1");
//        bikaUtils.showH24();
//        bikaUtils.favourite();
    }

    @Test
    public void testUrl() {
        CrudTools crudTools = SpringContext.getBeanWithGenerics(CrudTools.class);
        List<Bika> bikas = crudTools.commonApiQueryBySql("select * from bika where id = '5821859d5f6b9a4f93dbf719'", Bika.class);
        System.out.println(bikas);
    }
}
