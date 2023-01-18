package cn.zpl.dao.controller;

import cn.zpl.common.bean.RestResponse;
import cn.zpl.dao.bean.Ehentai;
import cn.zpl.dao.service.IEhentaiService;
import cn.zpl.dao.utils.CommonUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zpl
 * @since 2022-04-20
 */
@RestController
@RequestMapping("/commondaocenter/ehentai")
public class EhentaiController {


    @Resource
    IEhentaiService ehentaiService;

    @GetMapping("/get/{id}")
    public String downloadById(@PathVariable("id") String id) {
        Ehentai ehentai = ehentaiService.getById(id);
        System.out.println(ehentai);
        return ehentai.getTitle();
    }

    @PostMapping("/saveOrUpdateEhentai")
    public RestResponse saveOrUpdateBika(@RequestBody Ehentai ehentai) {
        boolean result = ehentaiService.saveOrUpdate(ehentai);
        return CommonUtils.getRestResponse(result);
    }
}
