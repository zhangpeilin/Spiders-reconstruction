package cn.zpl.tencent.controller;

import cn.zpl.common.bean.RestResponse;
import cn.zpl.tencent.bs.TencentBusiness;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class WeiyunController {

    @Resource
    TencentBusiness tencentBusiness;

    @GetMapping("/saveShare/{share_key}")
    public RestResponse save(@PathVariable("share_key") String share_key) {
        tencentBusiness.saveShareFile(share_key);
        return RestResponse.ok("提交保存成功");
    }
}
