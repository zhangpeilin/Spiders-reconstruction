package cn.zpl.spider.on.ehentai.util;

import cn.zpl.spider.on.ehentai.config.RabbitMqConfig;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

import static org.reflections.Reflections.log;

@Component
@Slf4j
public class RabbitMQSender {
    @Resource
    RabbitMqConfig mqConfig;
    public void sendMsg(String msg, String queue) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(mqConfig.getHost());
        factory.setPort(mqConfig.getPort());
        factory.setUsername(mqConfig.getUserName());
        factory.setPassword(mqConfig.getPassword());

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.basicPublish("", queue, null, (msg).getBytes(StandardCharsets.UTF_8));
            log.debug("Sent message: " + msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}