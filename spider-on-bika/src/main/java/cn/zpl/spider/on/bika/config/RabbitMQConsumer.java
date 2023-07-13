package cn.zpl.spider.on.bika.config;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQConsumer {
    private static final String QUEUE_NAME = "test_queue";
    private static final String ERROR_QUEUE_NAME = "error_queue";
    private static final int MAX_RETRY_COUNT = 3;
    private static final int RETRY_DELAY = 1000; // 1秒

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.139.130");
        factory.setPort(30762);
        factory.setUsername("admin");
        factory.setPassword("admin");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // 创建错误处理队列
        channel.queueDeclare(ERROR_QUEUE_NAME, false, false, false, null);

        // 设置消息确认模式
        channel.basicQos(1);

        // 创建消费者
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received message: " + message);

                try {
                    // 消费消息的代码
                    processMessage(message);

                    // 消息确认
                    channel.basicAck(envelope.getDeliveryTag(), false);
                } catch (Exception e) {
                    System.err.println("Failed to process message: " + message);

                    // 消费失败时，进行重试
                    int retryCount = 0;
                    while (retryCount < MAX_RETRY_COUNT) {
                        try {
                            Thread.sleep(RETRY_DELAY);
                            processMessage(message);
                            channel.basicAck(envelope.getDeliveryTag(), false);
                            break;
                        } catch (Exception ex) {
                            retryCount++;
                            System.err.println("Retry " + retryCount + " failed for message: " + message);
                        }
                    }

                    // 重试失败后，将消息发送到错误处理队列
                    channel.basicPublish("", ERROR_QUEUE_NAME, null, message.getBytes("UTF-8"));
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            }
        };

        // 开始消费消息
        channel.basicConsume(QUEUE_NAME, false, consumer);
    }

    private static void processMessage(String message) throws InterruptedException {
        // 模拟消费消息的处理过程
        if (Integer.parseInt(message) < 5) {
            throw new RuntimeException("Failed to process message");
        }

        System.out.println("Processed message: " + message);
    }
}