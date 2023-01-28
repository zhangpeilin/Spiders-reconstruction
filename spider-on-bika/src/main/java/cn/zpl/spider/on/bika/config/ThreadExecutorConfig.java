package cn.zpl.spider.on.bika.config;

import cn.zpl.util.DownloadTools;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class ThreadExecutorConfig implements AsyncConfigurer {

    @Override
    @Bean("BikaAsync")
    public Executor getAsyncExecutor() {
        DownloadTools instance = DownloadTools.getInstance(50);
        return instance.getExecutor();
    }
    @Bean // WebConfigurer
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void configurePathMatch(PathMatchConfigurer configurer) {
                UrlPathHelper urlPathHelper = new UrlPathHelper();
                urlPathHelper.setRemoveSemicolonContent(false);
                configurer.setUrlPathHelper(urlPathHelper);
            }
        };
    }


}
