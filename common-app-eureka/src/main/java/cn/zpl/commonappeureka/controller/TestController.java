package cn.zpl.commonappeureka.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 *
 * @author zhangpl1
 * @date 2022/9/14
 */

@RestController
public class TestController {

    @GetMapping("/get")
    public String hello(){
        return "hello";
    }
}
