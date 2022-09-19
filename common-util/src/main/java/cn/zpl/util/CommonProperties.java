package cn.zpl.util;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "spider.common")
@Component
@PropertySource(value = "classpath:util.properties",encoding="UTF-8")
public class CommonProperties {

    @Value("${spider.common.ffmpeg}")
    public String ffmpeg;
    @Value("${spider.common.m3u8SavePath}")
    public String m3u8SavePath;
}
