package cn.zpl.spider.on.pixiv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("cn.zpl")
@EnableEurekaClient
public class PixivApplication {

    public static void main(String[] args) {
        SpringApplication.run(PixivApplication.class, args);
    }
}
