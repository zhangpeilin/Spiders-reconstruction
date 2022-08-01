package cn.zpl.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spider.common")
public class CommonProperties {

    public String ffmpeg;
}
