package cn.zpl.config;

import cn.zpl.util.DownloadTools;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class MyThreadExecutorConfig implements AsyncConfigurer {

    @Override
    @Bean("MyAsync")
    public Executor getAsyncExecutor() {
        DownloadTools instance = DownloadTools.getInstance(5, "MyAsync");
        return instance.getExecutor();
    }
}



