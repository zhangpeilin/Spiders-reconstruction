package cn.zpl.commondaocenter.controller;

import cn.zpl.common.bean.PictureAnalyze;
import cn.zpl.common.bean.RestResponse;
import cn.zpl.commondaocenter.bean.Ehentai;
import cn.zpl.commondaocenter.service.IPictureAnalyzeService;
import cn.zpl.commondaocenter.utils.CommonUtils;
import cn.zpl.commondaocenter.utils.UrlConfig;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
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
    public PictureAnalyze getPA(@PathVariable String  id) {
        return pictureAnalyzeService.getById(id);
    }

    @SneakyThrows
    public static void main(String[] args) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<PictureAnalyze> responseEntity = restTemplate.getForEntity("http://localhost:8080/commondaocenter/pictureAnalyze/download/1526642337481396226", PictureAnalyze.class);
        PictureAnalyze item = responseEntity.getBody();
        assert item != null;
        ByteArrayInputStream bis = new ByteArrayInputStream(Objects.requireNonNull(item.getBaiduResult()));
            ObjectInputStream ois = new ObjectInputStream(bis);
        Object o = ois.readObject();
        System.out.println(o);
    }
}
