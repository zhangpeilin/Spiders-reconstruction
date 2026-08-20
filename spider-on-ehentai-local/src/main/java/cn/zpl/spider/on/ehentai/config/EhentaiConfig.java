package cn.zpl.spider.on.ehentai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spider.ehentai")
public class EhentaiConfig {
    String ehentaiCookies;
    String savePath;
    boolean unzip;
    String manhuaguiCookies;
    /**
     * 请求代理（e-hentai 需要代理访问），默认本机 Clash
     */
    String proxyHost = "127.0.0.1";
    int proxyPort = 7890;
}
