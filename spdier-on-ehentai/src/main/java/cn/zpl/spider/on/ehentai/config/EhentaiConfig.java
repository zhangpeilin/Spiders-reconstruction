package cn.zpl.spider.on.ehentai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spider.ehentai")
public class EhentaiConfig {
    String ehentaiCookies;
    String savePath;
    boolean unzip;
}
