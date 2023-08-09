package cn.zpl.spider.on.bika.config;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

public class RabbitMQSender {
    private final static String QUEUE_NAME = "myqueue";
    private final static String HOST = "192.168.139.130";
    private final static int PORT = 30762;
    private final static String USERNAME = "admin";
    private final static String PASSWORD = "admin";

    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
//            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            String message = "批量消息，序号:";
            for (int i = 0; i < 1; i++) {
                channel.basicPublish("myExchange", "test1", null, (message + i).getBytes(StandardCharsets.UTF_8));
            }
            System.out.println("Sent message: " + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}