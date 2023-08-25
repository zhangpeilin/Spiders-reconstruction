package cn.zpl.spider.on.ehentai;

import cn.zpl.spider.on.ehentai.config.EhentaiConfig;
import cn.zpl.spider.on.ehentai.config.RabbitMqConfig;
import cn.zpl.spider.on.ehentai.thread.DownLoadArchiveThread;
import cn.zpl.util.DownloadTools;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableConfigurationProperties({EhentaiConfig.class, RabbitMqConfig.class})
@ComponentScan("cn.zpl")
public class EhentaiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EhentaiApplication.class);
    }
}
