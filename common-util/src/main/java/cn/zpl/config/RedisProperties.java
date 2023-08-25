package cn.zpl.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spider.common.redis")
public class RedisProperties {

    private String redisAddress;
    private String redisPassword;
}
