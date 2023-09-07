package cn.zpl.spider.on.bilibili.manga.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Data
@ConfigurationProperties(prefix = "spider.bilibili")
@RefreshScope
public class BilibiliProperties {

    String cookies;
}
