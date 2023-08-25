package cn.zpl.config;

import cn.zpl.util.CommonProperties;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@EnableConfigurationProperties(RedisProperties.class)
public class RedissonConfig {

    @Resource
    RedisProperties redisProperties;

    @Bean
    public RedissonClient getRedissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress(redisProperties.getRedisAddress()).setPassword(redisProperties.getRedisPassword());
        return Redisson.create(config);
    }
}
