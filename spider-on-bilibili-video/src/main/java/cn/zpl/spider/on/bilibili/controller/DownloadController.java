package cn.zpl.spider.on.bilibili.controller;

import cn.zpl.common.bean.RestResponse;
import cn.zpl.spider.on.bilibili.BilibiliDownloadCoreForMusic;
import cn.zpl.spider.on.bilibili.BilibiliDownloadCorev2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
public class DownloadController {

    @Resource
    BilibiliDownloadCorev2 corev2;

    @Resource
    BilibiliDownloadCoreForMusic music;

    @GetMapping("/download/ep/{epId}")
    public RestResponse downloadByEpId(@PathVariable("epId") String epId) {
        try {
            corev2.getEpList(epId);
        } catch (Exception e) {
            log.error("下载错误：", e);
            RestResponse.fail(e.getMessage());
        }
        return RestResponse.ok();
    }

    @GetMapping("/download/{bid}")
    public RestResponse downloadById(@PathVariable("bid") String bid) {
        try {
            corev2.mainBusiness(bid);
        } catch (Exception e) {
            log.error("下载错误：", e);
            RestResponse.fail(e.getMessage());
        }
        return RestResponse.ok();
    }

    @GetMapping("/downloadAudioOnly/{bid}")
    public RestResponse downloadAudioOnly(@PathVariable("bid") String bid) {
        try {
            music.mainBusiness(bid);
        } catch (Exception e) {
            log.error("下载错误：", e);
            RestResponse.fail(e.getMessage());
        }
        return RestResponse.ok();
    }

    @PostMapping("/download/uid/{uid}")
    public RestResponse downloadByUid(@PathVariable("uid") String uid, @RequestBody String json) {
        try {
            corev2.getVideoList(uid, json);
        } catch (Exception e) {
            log.error("下载错误：", e);
            RestResponse.fail(e.getMessage());
        }
        return RestResponse.ok();
    }

    @GetMapping("/download/collection/{bid}")
    public RestResponse collection(@PathVariable("bid") String bid) {
        try {
            corev2.getCollections(bid);
        } catch (Exception e) {
            log.error("下载错误：", e);
            RestResponse.fail(e.getMessage());
        }
        return RestResponse.ok();
    }
}
