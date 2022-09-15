package cn.zpl.spider.on.bilibili.manga.config;

import cn.zpl.spider.on.bilibili.manga.util.BilibiliMangaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(BilibiliMangaProperties.class)
public class BilibiliMangaConfig {
}
