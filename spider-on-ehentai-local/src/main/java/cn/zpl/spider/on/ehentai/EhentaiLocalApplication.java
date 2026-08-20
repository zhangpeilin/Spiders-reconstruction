package cn.zpl.spider.on.ehentai;

import cn.zpl.config.LockAspect;
import cn.zpl.config.RedissonConfig;
import cn.zpl.config.UtilSpringConfig;
import cn.zpl.spider.on.ehentai.config.EhentaiConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@EnableConfigurationProperties({EhentaiConfig.class})
@ComponentScan(basePackages = "cn.zpl", excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {RedissonConfig.class, LockAspect.class, UtilSpringConfig.class})
})
public class EhentaiLocalApplication {

    public static void main(String[] args) {
        SpringApplication.run(EhentaiLocalApplication.class, args);
    }
}
