package cn.zpl.spider.on.bilibili.manga.util;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spider.bilibili.manga")
public class BilibiliMangaProperties {

    @Value("${spider.bilibili.cookies}")
    String bilibiliCookies;

    String commonHeaders;

    String getComicDetailUrl;

    String mangaSavePath;

    public String getImageIndexUrl;

    public String ImageTokenUrl;

    String GetEpisodeBuyInfoUrl;
    String BuyEpisodeUrl;

}
