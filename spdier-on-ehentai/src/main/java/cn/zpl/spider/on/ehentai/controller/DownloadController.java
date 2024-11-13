package cn.zpl.spider.on.ehentai.controller;

import cn.zpl.annotation.DistributeLock;
import cn.zpl.annotation.DistributedLockKey;
import cn.zpl.common.bean.Ehentai;
import cn.zpl.common.bean.RestResponse;
import cn.zpl.config.SpringContext;
import cn.zpl.spider.on.ehentai.bs.DownloadService;
import cn.zpl.spider.on.ehentai.thread.DownLoadArchiveThread;
import cn.zpl.spider.on.ehentai.thread.DownloadPageThread;
import cn.zpl.spider.on.ehentai.util.EUtil;
import cn.zpl.util.CommonStringUtil;
import cn.zpl.util.CrudTools;
import cn.zpl.util.DownloadTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.File;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@Slf4j
public class DownloadController {

    @Resource
    CrudTools tools;

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
        service.downTheOne(eh.getUrl(), -1, true);
        return "下载成功";
    }

    @PostMapping("/downloadBySql")
    public String downloadBySql(@RequestParam("sql") String sql, @RequestParam(value = "isDownload") boolean isDownload, @RequestParam(value = "cost", required = false) int cost) {
        List<Ehentai> ehentaiList = tools.commonApiQueryBySql(sql, Ehentai.class);
        DownloadTools tools = DownloadTools.getInstance(3, "downloadBySql");
        for (Ehentai ehentai : ehentaiList) {
            DownLoadArchiveThread downLoadArchiveThread = SpringContext.getBeanWithGenerics(DownLoadArchiveThread.class);
            downLoadArchiveThread.setUrl(ehentai.getUrl());
            downLoadArchiveThread.setCost(cost);
            downLoadArchiveThread.setDownload(isDownload);
            tools.ThreadExecutorAdd(downLoadArchiveThread);
        }
        tools.shutdown();
        return "下载成功";
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

    @PostMapping("/updateExistsFile")
    public String updateExistsFile(@RequestParam("path") String path) {
        Collection<File> files = FileUtils.listFiles(new File(path), new String[]{"zip"}, true);

        ConcurrentHashMap<String, Ehentai> temp = new ConcurrentHashMap<>();
        EUtil eUtil = new EUtil();
        eUtil.getEh("1");
        EUtil.cacheLoaded = false;
        EUtil.exists.asMap().forEach((s, o) -> {
            Ehentai ehentai = (Ehentai) o;
            if (StringUtils.isEmpty(ehentai.getTitle())) {
                return;
            }
            temp.put(ehentai.getTitle().toLowerCase(), ehentai);
        });
        for (File file : files) {
            String lowerCase = file.getName().replace(".zip", "").toLowerCase();
            Ehentai ehentai = temp.get(lowerCase);
            if (ehentai != null) {
                ehentai.setSavePath(file.getPath());
                tools.commonApiSave(ehentai);
            }
        }
        return path + "目录更新完成";
    }

    @PostMapping("/updateFile")
    public String updateFile(@RequestParam("path") String path) {
        Collection<File> files = FileUtils.listFiles(new File(path), new String[]{"zip"}, true);
        ConcurrentHashMap<String, Ehentai> temp = new ConcurrentHashMap<>();
        EUtil eUtil = new EUtil();
        EUtil.cacheLoaded = false;
        eUtil.getEh("1");
        EUtil.exists.asMap().forEach((s, o) -> {
            Ehentai ehentai = (Ehentai) o;
            if (StringUtils.isEmpty(ehentai.getTitle()) || !StringUtils.isEmpty(ehentai.getSavePath())) {
                return;
            }
            temp.put(ehentai.getTitle(), ehentai);
        });
        files.parallelStream().forEach(file -> temp.entrySet().parallelStream().forEach(entry -> {
            if (CommonStringUtil.stickCheck(entry.getKey(), file.getName().replace(".zip", "")) > 0.9) {
                Ehentai value = entry.getValue();
                log.debug("{}在数据库中存在近似项，数据库记录id为：{}", file, value.getId());
                value.setSavePath(file.getPath());
                tools.commonApiSave(value);
            }
        }));
        return "不存在数据库中文件输出完毕";
    }

    @PostMapping("/echoNoInDB")
    public String echoNoInDB(@RequestParam("path") String path) {
        Collection<File> files = FileUtils.listFiles(new File(path), new String[]{"zip"}, true);
        ConcurrentHashMap<String, Ehentai> temp = new ConcurrentHashMap<>();
        EUtil eUtil = new EUtil();
        EUtil.cacheLoaded = false;
        eUtil.getEh("1");
        EUtil.exists.asMap().forEach((s, o) -> {
            Ehentai ehentai = (Ehentai) o;
            if (StringUtils.isEmpty(ehentai.getTitle()) || StringUtils.isEmpty(ehentai.getSavePath())) {
                return;
            }
            temp.put(ehentai.getSavePath().toLowerCase(), ehentai);
        });
        DownloadTools downloadTools = DownloadTools.getInstance(1);
        files.parallelStream().forEach(file -> {
            if (temp.get(file.getPath().toLowerCase()) == null) {
                log.debug("{}文件在数据库中不存在", file);
                downloadTools.ThreadExecutorAdd(() ->{
                    Pattern pattern = Pattern.compile("\\[(.*?)\\]");
                    Matcher matcher = pattern.matcher(file.getName());
                    if (matcher.find()) {
                        HttpHeaders headers = new HttpHeaders();
                        String content = matcher.group(1);
                        LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
                        try {
                            params.add("url", "https://e-hentai.org/?f_search=" + URLEncoder.encode(content, "utf-8"));
                        } catch (Exception ignored) {
                        }
                        HttpEntity<MultiValueMap<String, Object>>  request = new HttpEntity<>(params, headers);
                        RestTemplate restTemplate = new RestTemplate();
                        restTemplate.postForEntity("http://localhost:8081/downloadPage", request, String.class);
                    }
                });
            }
        });
        downloadTools.shutdown();
        return "不存在数据库中文件输出完毕";
    }

    @GetMapping("/test1/{value}")
    @DistributeLock("redissonTest:test1")
    public void test1(@DistributedLockKey @PathVariable("value") String value1) {
        try {
            TimeUnit.SECONDS.sleep(2);
            System.out.println(value1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/test2")
    @DistributeLock(value = "redissonTest:test2", waitTime = 500, holdTime = 5000)
    public void test2(String value1) {
        try {
            TimeUnit.SECONDS.sleep(2);
            System.out.println("执行完毕");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
