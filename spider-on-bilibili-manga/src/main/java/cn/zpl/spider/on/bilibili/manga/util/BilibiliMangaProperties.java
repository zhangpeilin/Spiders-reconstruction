package cn.zpl.spider.on.bilibili.manga.util;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spider.bilibili.manga")
public class BilibiliMangaProperties {

    @Value("${spider.bilibili.cookies}")
    public String bilibiliCookies;
    public String commonHeaders;
    public String getComicDetailUrl;

    public String mangaSavePath;

    public String getImageIndexUrl;

    public String ImageTokenUrl;

}
