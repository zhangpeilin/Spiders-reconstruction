package cn.zpl.spider.on.ehentai.util;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

public class MessageSender {

    private final RabbitTemplate rabbitTemplate;
    private final Exchange myExchange;

    public DirectExchange myExchange() {
        return new DirectExchange("myExchange");
    }

    public MessageSender(RabbitTemplate rabbitTemplate, Exchange myExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.myExchange = myExchange;
    }

    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend(myExchange.getName(), "myRoutingKey", message);
        System.out.println("Sent message: " + message);
    }
}