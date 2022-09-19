package cn.zpl.controller;

import cn.zpl.service.RabbitMQService;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.Resource;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 *
 * @author zhangpl1
 * @date 2022/9/9
 */
public class RabbitmqController {

    @Resource
    RabbitMQService rabbitMQService;

    @PostMapping("/sendMsg")
    public String sendMsg(){
        return rabbitMQService.sendMsg("");
    }
}
