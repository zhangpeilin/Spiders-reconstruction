package cn.zpl.spider.on.bika;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 *
 * @author zhangpl1
 * @date 2022/8/4
 */
@SpringBootApplication
@ComponentScan("cn.zpl")
public class BikaApplication {
    public static void main(String[] args) {
        SpringApplication.run(BikaApplication.class, args);
    }
}
