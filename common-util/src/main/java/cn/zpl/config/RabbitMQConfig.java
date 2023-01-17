//package cn.zpl.config;
//
//import org.springframework.amqp.core.Binding;
//import org.springframework.amqp.core.BindingBuilder;
//import org.springframework.amqp.core.DirectExchange;
//import org.springframework.amqp.core.Queue;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * <p>Title: </p>
// * <p>Description: </p>
// *
// * @author zhangpl1
// * @date 2022/9/9
// */
//
//@Configuration
//public class RabbitMQConfig {
//
//    public static final String RABBITMQ_TOPIC = "rabbitmqTopic";
//
//    public static final String RABBITMQ_DIRECT_EXCHANGE = "rabbitmqDirectExchange";
//
//    public static final String RABBITMQ_DIRECT_ROUTING = "rabbitmqDirectRouting";
//
//    @Bean
//    public Queue rabbitmqDirectQueue() {
//        return new Queue(RabbitMQConfig.RABBITMQ_TOPIC, true, false, false);
//    }
//
//    @Bean
//    public DirectExchange rabbitmqDirectExchange() {
//        return new DirectExchange(RabbitMQConfig.RABBITMQ_DIRECT_EXCHANGE, true, false);
//    }
//
//    @Bean
//    public Binding bindDirect() {
//        return BindingBuilder.bind(rabbitmqDirectQueue()).to(rabbitmqDirectExchange()).with(RabbitMQConfig.RABBITMQ_DIRECT_ROUTING);
//    }
//
//}
