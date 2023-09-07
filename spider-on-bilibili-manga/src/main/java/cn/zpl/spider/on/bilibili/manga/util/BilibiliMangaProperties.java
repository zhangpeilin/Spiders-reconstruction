package cn.zpl.spider.on.bilibili.manga.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Data
@ConfigurationProperties(prefix = "spider.bilibili.manga")
@RefreshScope
public class BilibiliMangaProperties {

    String commonHeaders;

    String getComicDetailUrl;

    String mangaSavePath;

    public String getImageIndexUrl;

    public String ImageTokenUrl;

    String GetEpisodeBuyInfoUrl;
    String BuyEpisodeUrl;

    int queueSize;

}
