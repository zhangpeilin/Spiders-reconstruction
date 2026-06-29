package cn.zpl.local;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spider.bilibili")
public class LocalBilibiliProperties {

    public static final String playInfoUrl = "https://api.bilibili.com/x/player/wbi/playurl?avid=%1$s&bvid=%2$s&cid=%3$s&qn=0&fnver=0&fnval=4048&fourk=1&gaia_source=&from_client=BROWSER";
    public static final String getEpListUrl = "https://api.bilibili.com/pgc/view/web/ep/list?season_id=%1$s";
    public static final String commonHeaders = "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36\nContent-Type:application/json\n";

    private String cookies = "";
    private String tmpSavePath = "d:\\视频爬虫\\temp";
    private String videoSavePath = "g:\\视频爬虫";
    private String ffmpeg = "ffmpeg";
}
