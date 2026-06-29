package cn.zpl;

import cn.zpl.config.LockAspect;
import cn.zpl.config.RedissonConfig;
import cn.zpl.config.UtilSpringConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(basePackages = "cn.zpl", excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {RedissonConfig.class, LockAspect.class, UtilSpringConfig.class})
})
public class BilibiliLocalDownloadApplication {

    public static void main(String[] args) {
        SpringApplication.run(BilibiliLocalDownloadApplication.class, args);
    }
}