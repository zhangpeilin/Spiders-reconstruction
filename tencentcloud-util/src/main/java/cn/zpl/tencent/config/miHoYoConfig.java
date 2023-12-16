package cn.zpl.tencent.config;

import cn.zpl.config.SpringContext;
import cn.zpl.tencent.TestCorpWx;
import cn.zpl.tencent.common.TencentParams;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(TencentParams.class)
@EnableScheduling
public class miHoYoConfig {

    public static LoadingCache<String, Boolean> cache = CacheBuilder.newBuilder().maximumSize(1).expireAfterWrite(24, TimeUnit.HOURS).build(new CacheLoader<String, Boolean>() {
        @Override
        public @NotNull Boolean load(@NotNull String key) {
            return false;
        }
    });

    //每天8:30开始每5分钟执行一次
    @SneakyThrows
    @Scheduled(cron = "0 * 14 * * *")
    private void configureTask(){
        if (cache.get("isNotice")) {
            return;
        }
        TestCorpWx TestCorpWx = SpringContext.getBeanWithGenerics(TestCorpWx.class);
        TestCorpWx.doBusiness();
        //判断当天是否已经通知过，如果通知过则跳过执行
        System.out.println("自动任务执行时间：" + LocalDateTime.now());
    }

    @Bean
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }
}
