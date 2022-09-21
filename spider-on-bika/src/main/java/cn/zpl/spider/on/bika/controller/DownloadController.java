package cn.zpl.spider.on.bika.controller;

import cn.zpl.common.bean.RestResponse;
import cn.zpl.spider.on.bika.utils.BikaUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class DownloadController {

    @Resource
    BikaUtils bikaUtils;

    @GetMapping("/download/{key}")
    public RestResponse downloadByKey(@PathVariable("key") String key) {
        bikaUtils.search(key);
        return RestResponse.ok().msg("下载提交成功");
    }
}
