package cn.zpl.spider.on.bika.config;

import cn.zpl.util.DownloadTools;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class ThreadExecutorConfig implements AsyncConfigurer {

    @Override
    @Bean("BikaAsync")
    public Executor getAsyncExecutor() {
        DownloadTools instance = DownloadTools.getInstance(5);
        return instance.getExecutor();
    }


}
