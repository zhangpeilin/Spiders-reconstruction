package cn.zpl.spider.on.ehentai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Data
@RefreshScope
@ConfigurationProperties(prefix = "spider.ehentai")
public class EhentaiConfig {
    String ehentaiCookies;
    String savePath;
    boolean unzip;
    boolean saveDb;
    String manhuaguiCookies;
}
