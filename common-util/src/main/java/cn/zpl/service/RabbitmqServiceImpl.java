//package cn.zpl.service;
//
//import cn.zpl.config.RabbitMQConfig;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//
//import javax.annotation.Resource;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
///**
// * <p>Title: </p>
// * <p>Description: </p>
// *
// * @author zhangpl1
// * @date 2022/9/9
// */
//public class RabbitmqServiceImpl implements RabbitMQService {
//    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//    @Resource
//    RabbitTemplate rabbitTemplate;
//
//
//    @Override
//    public String sendMsg(String msg) {
//        String msgId = UUID.randomUUID().toString().replace("-", "").substring(0, 32);
//        String sendTime = sdf.format(new Date());
//        Map<String, Object> map = new HashMap<>();
//        map.put("msgId", msgId);
//        map.put("sendTime", sendTime);
//        map.put("msg", msg);
//        rabbitTemplate.convertAndSend(RabbitMQConfig.RABBITMQ_DIRECT_EXCHANGE, RabbitMQConfig.RABBITMQ_DIRECT_ROUTING, map);
//        return "ok";
//    }
//}
