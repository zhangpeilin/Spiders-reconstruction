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
            byte[] bytes = new byte[0];
            for (int i = 0; i < 10; i++) {
                bytes = (message + i).getBytes(StandardCharsets.UTF_8);
                channel.basicPublish("myExchange", "test1", null, bytes);
            }
            System.out.println("Sent message: " + new String(bytes));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}