package cn.zpl.jsweb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 *
 * @author zhangpl1
 * @date 2023/4/6
 */
@Controller
public class JsController {

    @GetMapping("/decode")
    public String decode(){
        return "2";
    }
}
