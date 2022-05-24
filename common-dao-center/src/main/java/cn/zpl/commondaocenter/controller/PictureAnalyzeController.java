package cn.zpl.commondaocenter.controller;

import cn.zpl.common.bean.PictureAnalyze;
import cn.zpl.common.bean.RestResponse;
import cn.zpl.commondaocenter.service.IPictureAnalyzeService;
import cn.zpl.commondaocenter.utils.CommonUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zpl
 * @since 2022-05-18
 */
@RestController
@RequestMapping("/commondaocenter/pictureAnalyze")
public class PictureAnalyzeController {

    @Resource
    IPictureAnalyzeService pictureAnalyzeService;

    @PostMapping("/saveOrUpdatePictureAnalyze")
    public RestResponse saveOrUpdatePictureAnalyze(@RequestBody PictureAnalyze pictureAnalyze) {
        boolean result = pictureAnalyzeService.saveOrUpdate(pictureAnalyze);
        return CommonUtils.getRestResponse(result);
    }

    @GetMapping("/download/{id}")
    public RestResponse getPA(@PathVariable String  id) {
        PictureAnalyze byId = pictureAnalyzeService.getById(id);
        assert byId != null;
        return RestResponse.ok().item(byId);
    }

    @GetMapping("/queryPAList/{key}")
    public RestResponse queryPAList(@PathVariable String key) {
        QueryWrapper<PictureAnalyze> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.like("tencent_json_result", key).or().like("baidu_json_result", key);
        List<PictureAnalyze> list = pictureAnalyzeService.list(objectQueryWrapper);
        return RestResponse.ok().list(list);
    }

    @SneakyThrows
    @GetMapping("/queryPAListByCondition/{condition}")
    public RestResponse queryPAListByCondition(@PathVariable String condition) {
        QueryWrapper<PictureAnalyze> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.apply(URLDecoder.decode(condition, "utf-8"));
        List<PictureAnalyze> list = pictureAnalyzeService.list(objectQueryWrapper);
        return RestResponse.ok().list(list);
    }

    @SneakyThrows
    public static void main(String[] args) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<RestResponse> responseEntity = restTemplate.getForEntity("http://localhost:8080/commondaocenter/pictureAnalyze/queryPAListByCondition/" + URLEncoder.encode("id='1527733848444903425'", "utf-8"), RestResponse.class);
        RestResponse item = responseEntity.getBody();
        System.out.println(Objects.requireNonNull(item).getItem());
    }
}
