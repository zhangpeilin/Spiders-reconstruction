package cn.zpl.controller;

import cn.zpl.common.bean.RestResponse;
import cn.zpl.util.CrudTools;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UploadIPController {

    CrudTools tools;

    public RestResponse saveIP(){
        return RestResponse.ok();
    }
}
