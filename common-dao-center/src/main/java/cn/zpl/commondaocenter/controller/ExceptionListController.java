package cn.zpl.commondaocenter.controller;

import cn.zpl.common.bean.ExceptionList;
import cn.zpl.common.bean.RestResponse;
import cn.zpl.commondaocenter.service.IExceptionListService;
import cn.zpl.commondaocenter.utils.CommonUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
 * @since 2022-05-02
 */
@RestController
@RequestMapping("/commondaocenter/exceptionList")
public class ExceptionListController {

    @Resource
    IExceptionListService exceptionListService;

    @GetMapping("/get/{id}")
    public RestResponse getById(@PathVariable("id") String id){
        QueryWrapper<ExceptionList> queryWrapper = new QueryWrapper<>();
        QueryWrapper<ExceptionList> video_id = queryWrapper.eq("video_id", id);
        ExceptionList one = exceptionListService.getOne(video_id);
        video_id.getEntityClass();
        System.out.println(one);
        return RestResponse.fail().item(null);
    }

    @PostMapping("/saveOrUpdateExceptionList")
    public RestResponse saveOrUpdateExceptionList(@RequestBody ExceptionList exceptionList) {
        boolean result = exceptionListService.saveOrUpdate(exceptionList);
        return CommonUtils.getRestResponse(result);
    }
}
