package cn.zpl.spider.on.bilibili.manga.config;

import cn.zpl.spider.on.bilibili.manga.util.BilibiliMangaProperties;
import cn.zpl.spider.on.bilibili.manga.util.BilibiliProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({BilibiliMangaProperties.class, BilibiliProperties.class})
public class BilibiliMangaConfig {
}
