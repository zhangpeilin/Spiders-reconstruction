package cn.zpl.spider.on.bika.config;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitQueueListenerv2 {

    public void process(String msg) {
        System.out.println("当前消费队列v2:" + msg);
    }


}
