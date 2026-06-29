package cn.zpl.local;

import cn.zpl.common.bean.RestResponse;
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
    BilibiliLocalDownloadCore downloadCore;

    @GetMapping("/download/ep/{epId}")
    public RestResponse downloadByEpId(@PathVariable("epId") String epId) {
        try {
            downloadCore.getEpList(epId);
        } catch (Exception e) {
            log.error("下载错误：", e);
            RestResponse.fail(e.getMessage());
        }
        return RestResponse.ok();
    }

    @GetMapping("/download/ssId/{ssid}")
    public RestResponse downloadBySsId(@PathVariable("ssid") String ssid) {
        try {
            downloadCore.getEpList(ssid);
        } catch (Exception e) {
            log.error("下载错误：", e);
            RestResponse.fail(e.getMessage());
        }
        return RestResponse.ok();
    }

    @GetMapping("/download/{bid}")
    public RestResponse downloadById(@PathVariable("bid") String bid) {
        try {
            downloadCore.mainBusiness(bid);
        } catch (Exception e) {
            log.error("下载错误：", e);
            RestResponse.fail(e.getMessage());
        }
        return RestResponse.ok();
    }

    @PostMapping("/download/uid/{uid}")
    public RestResponse downloadByUid(@PathVariable("uid") String uid, @RequestBody String json) {
        try {
            downloadCore.getVideoList(uid, json);
        } catch (Exception e) {
            log.error("下载错误：", e);
            RestResponse.fail(e.getMessage());
        }
        return RestResponse.ok();
    }

    @GetMapping("/download/collection/{bid}")
    public RestResponse collection(@PathVariable("bid") String bid) {
        try {
            downloadCore.getCollections(bid);
        } catch (Exception e) {
            log.error("下载错误：", e);
            RestResponse.fail(e.getMessage());
        }
        return RestResponse.ok();
    }
}
