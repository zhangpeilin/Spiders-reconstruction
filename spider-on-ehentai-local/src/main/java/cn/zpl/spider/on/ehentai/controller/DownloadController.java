package cn.zpl.spider.on.ehentai.controller;

import cn.zpl.common.bean.Ehentai;
import cn.zpl.common.bean.RestResponse;
import cn.zpl.spider.on.ehentai.bs.DownloadService;
import cn.zpl.spider.on.ehentai.thread.DownloadPageThread;
import cn.zpl.spider.on.ehentai.util.EUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Slf4j
public class DownloadController {

    @Resource
    DownloadService service;

    @Resource
    EUtil utils;

    @PostMapping("/download")
    public RestResponse downloadByUrl(@RequestParam("url") String url, @RequestParam("isDownload") boolean isDownload) {
        if (!StringUtils.isEmpty(url)) {
            if (url.contains("\n")) {
                String[] urls = url.split("\n");
                for (String s : urls) {
                    service.downTheOne(s, -1, isDownload);
                }
            } else {
                service.downTheOne(url, -1, isDownload);
            }
        }
        return RestResponse.ok("提交成功");
    }

    @GetMapping("/download/id/{id}")
    public String downloadById(@PathVariable("id") String id) {
        Ehentai eh = utils.getEh(id);
        if (eh == null) {
            return "未找到id为" + id + "的记录";
        }
        service.downTheOne(eh.getUrl(), -1, true);
        return "下载成功";
    }

    /**
     * 查询本地元数据记录（供 UI 展示已下载/未完成的画廊）
     */
    @GetMapping("/local/list")
    public RestResponse localList(@RequestParam(value = "keyword", required = false) String keyword,
                                  @RequestParam(value = "size", required = false, defaultValue = "100") int size) {
        return RestResponse.ok(utils.listAll(keyword, size));
    }

    @PostMapping("/downloadPage")
    public String downloadPage(@RequestParam("url") String url, @RequestParam("flag") Boolean flag,  @RequestParam("pageCount") int pageCount) {
        DownloadPageThread downLoadArchiveThread = new DownloadPageThread();
        downLoadArchiveThread.setDownload(flag);
        downLoadArchiveThread.setUrl(url);
        downLoadArchiveThread.setPageCount(pageCount);
        downLoadArchiveThread.run();
        return "下载成功";
    }
}
