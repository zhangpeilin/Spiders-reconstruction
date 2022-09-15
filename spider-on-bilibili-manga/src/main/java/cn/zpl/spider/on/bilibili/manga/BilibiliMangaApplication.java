package cn.zpl.spider.on.bilibili.manga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("cn.zpl")
public class BilibiliMangaApplication {
    public static void main(String[] args) {
        SpringApplication.run(BilibiliMangaApplication.class);
    }
}
