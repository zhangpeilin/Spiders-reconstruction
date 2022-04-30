package cn.zpl.commondaocenter.controller;

import cn.zpl.common.bean.Bika;
import cn.zpl.common.bean.RestResponse;
import cn.zpl.commondaocenter.service.IBikaService;
import cn.zpl.commondaocenter.utils.CommonUtils;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 哔咔漫画下载信息记录 前端控制器
 * </p>
 *
 * @author zpl
 * @since 2022-03-27
 */
@RestController
@RequestMapping("/commondaocenter/bika")
public class BikaController {


    @Resource
    IBikaService bikaService;

    @GetMapping("/download/{id}")
    public String downloadById(@PathVariable("id") String id) {
        Bika bika = bikaService.getById(id);
        System.out.println(bika);
        return bika.getTitle();
    }

    @PostMapping("/saveBika")
    public RestResponse saveBika(@RequestBody Bika bika) {
        boolean save = bikaService.save(bika);
        return CommonUtils.getRestResponse(save);
    }

    @PostMapping("/saveOrUpdateBika")
    public RestResponse saveOrUpdateBika(@RequestBody Bika bika) {
        boolean result = bikaService.saveOrUpdate(bika);
        return CommonUtils.getRestResponse(result);
    }

    @PostMapping("/updateBika")
    public Map<String, Object> updateBika(@RequestBody Bika bika) {
        UpdateWrapper<Bika> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", bika.getId());
        boolean update = bikaService.update(bika, updateWrapper);
        Map<String, Object> result = new HashMap<>();
        result.put("flag", update);
        result.put("msg", update ? "保存成功" : "保存失败");
        return result;
    }

}
