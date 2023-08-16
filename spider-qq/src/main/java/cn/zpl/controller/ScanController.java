package cn.zpl.controller;

import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScanController {

    public String scanPath(String path) {

        return "扫描完成";
    }
}
