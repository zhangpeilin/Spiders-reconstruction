package cn.zpl.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "spider.common.url")
public class UrlConfig {
    String commonSaveUrl;
    String commonQueryUrl;
}
