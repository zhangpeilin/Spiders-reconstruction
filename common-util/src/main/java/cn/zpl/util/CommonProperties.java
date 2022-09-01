package cn.zpl.util;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "spider.common")
@Component
public class CommonProperties {

    @Value("${spider.common.ffmpeg}")
    public String ffmpeg;
}
