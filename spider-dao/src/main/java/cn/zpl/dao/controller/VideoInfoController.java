package cn.zpl.dao.controller;

import cn.zpl.common.bean.RestResponse;
import cn.zpl.dao.bean.VideoInfo;
import cn.zpl.dao.service.IVideoInfoService;
import cn.zpl.dao.utils.CommonUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
 *  前端控制器
 * </p>
 *
 * @author zpl
 * @since 2022-04-02
 */
@RestController
@RequestMapping("/commondaocenter/videoInfo")
public class VideoInfoController {

    @Resource
    IVideoInfoService iVideoInfoService;

    @GetMapping("/download/{id}")
    public String downloadById(@PathVariable("id") String id){
        QueryWrapper<VideoInfo> queryWrapper = new QueryWrapper<>();
        QueryWrapper<VideoInfo> video_id = queryWrapper.eq("video_id", id);
        VideoInfo one = iVideoInfoService.getOne(video_id);
        System.out.println(one);
        return one.getTitle();
    }

    @GetMapping("/get/{id}")
    public VideoInfo getById(@PathVariable("id") String id){
        QueryWrapper<VideoInfo> queryWrapper = new QueryWrapper<>();
        QueryWrapper<VideoInfo> video_id = queryWrapper.eq("video_id", id);
        VideoInfo one = iVideoInfoService.getOne(video_id);
        System.out.println(one);
        return one;
    }

    @PostMapping("/saveVideoInfo")
    public RestResponse saveVideoInfo(@RequestBody VideoInfo videoInfo) {

        boolean save = iVideoInfoService.save(videoInfo);
        if (save) {
            return RestResponse.ok();
        } else {
            return RestResponse.fail("保存失败");
        }
    }

    @PostMapping("/saveOrUpdateVideoInfo")
    public RestResponse saveOrUpdateVideoInfo(@RequestBody VideoInfo videoInfo) {
        boolean result = iVideoInfoService.saveOrUpdate(videoInfo);
        return CommonUtils.getRestResponse(result);
    }

    @PostMapping("/updateVideoInfo")
    public Map<String, Object> updateVideoInfo(@RequestBody VideoInfo videoInfo) {
        UpdateWrapper<VideoInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("video_id", videoInfo.getVideoId());
        boolean update = iVideoInfoService.update(videoInfo, updateWrapper);
        Map<String, Object> result = new HashMap<>();
        result.put("flag", update);
        result.put("msg", update ? "保存成功" : "保存失败");
        return result;
    }

}
