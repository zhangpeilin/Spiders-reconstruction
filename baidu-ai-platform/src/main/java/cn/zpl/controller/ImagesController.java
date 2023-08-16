package cn.zpl.controller;

import cn.zpl.util.BaiduAITool;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;

@RestController
public class ImagesController {

    @Resource
    BaiduAITool baiduAITool;

    @PostMapping("/checkImages")
    public String checkImages(@RequestParam("path") String imagePath) {
        return baiduAITool.ApiCensor(imagePath);
    }
}
