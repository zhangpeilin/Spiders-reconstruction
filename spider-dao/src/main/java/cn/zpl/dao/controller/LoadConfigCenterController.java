package cn.zpl.dao.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoadConfigCenterController {

    @Value("${spring.datasource.url}")
    String info;

    @GetMapping("/loadConfig")
    public String loadConfig() {
        return info;
    }
}
