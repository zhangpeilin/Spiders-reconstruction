package cn.zpl.spider.on.ehentai.controller;

import cn.zpl.spider.on.ehentai.config.EhentaiConfig;
import cn.zpl.spider.on.ehentai.util.EUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class TestConfigController {

    @Resource
    EhentaiConfig config;

    @GetMapping("/config")
    public String showConfig() {
        System.out.println(config.getSavePath());
        EUtil util = new EUtil();
        return util.getEh("1297889").getSavePath();
    }
}
