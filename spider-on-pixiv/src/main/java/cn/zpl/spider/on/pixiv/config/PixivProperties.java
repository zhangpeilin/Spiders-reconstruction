package cn.zpl.spider.on.pixiv.config;

import cn.zpl.annotation.Value;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "spider.pixiv")
@Component
@Data
public class PixivProperties {

    @Value("cookiesValue")
    String cookies;

    @Value("pxiv_save_path")
    String pixivSavePath;

    String pageUrl;

    boolean dateSort;
}
