package cn.zpl.spider.on.ehentai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Data
@RefreshScope
@ConfigurationProperties(prefix = "spider.ehentai.rabbitmq")
public class RabbitMqConfig {
//    private final static String QUEUE_NAME = "bika";
//    private final static String HOST = "192.168.139.130";
//    private final static int PORT = 30762;
//    private final static String USERNAME = "admin";
//    private final static String PASSWORD = "admin";
    String queueName;
    String host;
    int port;
    String userName;
    String password;
}
