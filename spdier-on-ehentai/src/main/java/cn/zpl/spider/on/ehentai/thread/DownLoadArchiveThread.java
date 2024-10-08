package cn.zpl.spider.on.ehentai.thread;

import cn.zpl.common.bean.Ehentai;
import cn.zpl.common.bean.RestResponse;
import cn.zpl.config.SpringContext;
import cn.zpl.pojo.Data;
import cn.zpl.pojo.DownloadDTO;
import cn.zpl.spider.on.ehentai.config.EhentaiConfig;
import cn.zpl.spider.on.ehentai.util.EUtil;
import cn.zpl.thread.CommonThread;
import cn.zpl.thread.OneFileOneThread;
import cn.zpl.util.CheckCallBack;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.CrudTools;
import cn.zpl.util.ProxyUtil;
import cn.zpl.util.UnZipUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zpl
 */
@Slf4j
@Scope("prototype")
@Component
public class DownLoadArchiveThread extends CommonThread {

    @Resource
    EhentaiConfig ehentaiConfig;
    Pattern pattern = Pattern.compile("[\\d,]+");

    int cost = 10000;

    boolean isDownload = true;

    @Resource
    CrudTools tools;

    public void setDownload(boolean flag) {
        this.isDownload = flag;
    }

    public DownLoadArchiveThread setCost(int cost) {
        this.cost = cost;
        return this;
    }
    EUtil util;

    public DownLoadArchiveThread() {
        util = new EUtil();
        getDoRetry().setRetryMaxCount(3);
    }
    public DownLoadArchiveThread(String url) {
        this.setUrl(url);
        ehentaiConfig = SpringContext.getBeanWithGenerics(EhentaiConfig.class);
        util = new EUtil();
        tools = SpringContext.getBeanWithGenerics(CrudTools.class);
        getDoRetry().setRetryMaxCount(3);
    }

    @Override
    public void domain() {
        Random random = new Random();
        int waitTime = random.nextInt(5) + 1;
        try {
            TimeUnit.SECONDS.sleep(waitTime);
        } catch (InterruptedException ignored) {
        }
        Ehentai eh = util.getEh(EUtil.getGalleryId(getUrl()));
        if (isDownload && eh != null && eh.getFinish() == 1) {
            log.debug("{}-->{}已下载，跳过", getUrl(), eh.getTitle());
            //已经下载的，复制到当前目录一份，文件名中添加（拷贝）字样
            File path = new File(makePath(eh.getTitle()));
            String name = new File(eh.getSavePath()).getName();
            try {
                FileUtils.copyFile(new File(eh.getSavePath()), new File(path.getParent(), "(拷贝)" + name));
            } catch (IOException e) {
                log.error("{}-->{}复制失败", getUrl(), eh.getTitle());
            }
            return;
        }
        Data data = new Data();
        Ehentai ehentai;
        data.setUrl(getUrl());
        data.setHeader(ehentaiConfig.getEhentaiCookies());
        data.setProxy(true);
        data.setAlwaysRetry();
        CommonIOUtils.withTimer(data);
        CheckCallBack checkCallBack = new CheckCallBack() {
            @Override
            public boolean call(String data) {
                Document parse = Jsoup.parse(data);
                Elements bannedForExcessive = parse.getElementsContainingOwnText("banned for excessive pageloads");
                return !bannedForExcessive.isEmpty();
            }
        };
        if (checkCallBack.call(data.getResult())) {
            ProxyUtil.checkProxy(url, "ehentai", ehentaiConfig.getEhentaiCookies(), checkCallBack);
            CommonIOUtils.withTimer(data);
        }
        Document document = Jsoup.parse(data.getResult());
        Element fileSize = document.selectFirst(":containsOwn(File Size)");
        if (data.getStatusCode() == 404) {
            if (eh == null) {
                eh = new Ehentai();
            }
            eh.setSavePath("画廊已删除");
            tools.commonApiSave(eh);
            log.debug("404画廊不存在");
            return;
        }
        Element favcount = document.selectFirst("td#favcount");
        Elements tagList = document.select("div#taglist td");
        Element rating = document.selectFirst("td#rating_label");
        Map<String, Object> information = new HashMap<>();
        String key = null;
        for (int i = 0; i < tagList.size(); i++) {
            //偶数时是大类名，奇数是具体值
            if (i % 2 == 0) {
                key = tagList.get(i).text().replace(":", "");
            } else {
                Elements children = document.select("div#taglist td").get(i).children();
                StringBuilder values = new StringBuilder("[");
                for (Element child : children) {
                    values.append(child.text()).append(",");
                }
                values.deleteCharAt(values.length() - 1).append("]");
                information.put(key, values.toString());
            }
        }
        ehentai = JSON.toJavaObject(new JSONObject(information), Ehentai.class);
        if (fileSize != null) {
            Element size = fileSize.nextElementSibling();
            if (size != null) {
                ehentai.setSize(CommonIOUtils.convertSizeToBytes(size.text()));
            }
        }
        assert favcount != null;
        Matcher faviconMatcher = pattern.matcher(favcount.text());
        if (faviconMatcher.find()) {
            try {
                ehentai.setFavcount(NumberFormat.getNumberInstance(Locale.US).parse(faviconMatcher.group()).toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (rating != null && !StringUtils.isEmpty(rating.text())) {
            String number = rating.text().replaceAll("[^\\d.]", "");
            ehentai.setRating(number);
        }
        Elements viewGallery = document.getElementsMatchingText("View Gallery");
        if (viewGallery.size() != 0) {
            url = viewGallery.attr("href");
            run();
            return;
        }
        Elements elements = document.select("div#gd5 p.g2");

        for (Element element1 : elements) {
            Element a = element1.selectFirst("a");
            if (a.text().toLowerCase().contains("archive")) {
                String js = a.attr("onclick");
                Data archive = new Data();
                archive.setHeader(ehentaiConfig.getEhentaiCookies());
                archive.setUrl(js.substring(js.indexOf("http"), js.lastIndexOf("'")));
                archive.setProxy(true);
                archive.setAlwaysRetry();
                CommonIOUtils.withTimer(archive);
                Document tmp = Jsoup.parse(archive.getResult());
                String title = Objects.requireNonNull(tmp.selectFirst("div#db > h1")).text();
                Element form = tmp.selectFirst("form");
                assert form != null;
                Elements freeMark = Objects.requireNonNull(form.previousElementSibling()).getElementsMatchingText("Free");
                ehentai.setTitle(title);
                ehentai.setUrl(url);
                ehentai.setId(EUtil.getGalleryId(url));
                ehentai.setFinish(0);
                ehentai.setCreate_time(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                if (freeMark.isEmpty()) {
                    log.error("下载消耗点数，链接地址记录：" + url);
                    log.error(tmp.select("   div#db h1").text());
                    Matcher matcher = pattern.matcher(form.previousElementSibling().text());
                    if (matcher.find()) {
                        String gpStr = matcher.group(0);
                        int gp = 0;
                        try {
                            gp = NumberFormat.getNumberInstance(Locale.US).parse(gpStr).intValue();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        ehentai.setCost(String.valueOf(gp));
                        if (cost != -1 && gp > cost) {
                            if (ehentaiConfig.isSaveDb()) {
                                RestResponse restResponse = tools.commonApiSave(ehentai);
                                log.debug("保存是否成功：{}", restResponse.isSuccess());
                            }
                            log.error("当前漫画未下载：{}", url);
                            return;
                        }
                        log.info("下载消耗点数：" + gp);
                    }
                }
                if (ehentaiConfig.isSaveDb()) {
                    RestResponse restResponse = tools.commonApiSave(ehentai);
                    log.debug("保存是否成功：{}", restResponse.isSuccess());
                }
                if (!isDownload) {
                    return;
                }
                if (form.attr("action").startsWith("http")) {
                    Data d1 = new Data();
                    d1.setUrl(form.attr("action"));
                    d1.setHeader(ehentaiConfig.getEhentaiCookies() + "\nContent-Type: application/x-www-form-urlencoded; charset=UTF-8");
                    d1.setProxy(true);
                    d1.setParams("dltype=org&dlcheck=Download+Original+Archive");
                    Map<String, String> vp = new HashMap<>();
                    vp.put("dltype", "org");
                    vp.put("dlcheck", "Download Original Archive");
                    d1.setValuePairs(vp);

                    String result = CommonIOUtils.postUrl(d1);
                    Element tmpUrl = CommonIOUtils.getElementFromStr(result, "p#continue");
                    Data data1 = new Data();
                    data1.setAlwaysRetry();
                    data1.setProxy(true);
                    data1.setUrl(Objects.requireNonNull(tmpUrl.selectFirst("a")).attr("href"));
                    CommonIOUtils.withTimer(data1);
                    Document doc = Jsoup.parse(data1.getResult());
                    doc.setBaseUri(data1.getBaseUrl());
                    Element downUrl = CommonIOUtils.getElementFromStr(doc, "div#db a");
                    Element fileName = CommonIOUtils.getElementFromStr(doc, "div#db strong");
                    DownloadDTO dto = new DownloadDTO();
                    dto.setProxy(true);
                    dto.setUrl(downUrl.absUrl("href"));
                    dto.setFileName(CommonIOUtils.filterFileName2(fileName.text()));
                    List<String> path = new ArrayList<>();
                    path.add(ehentaiConfig.getSavePath());
                    path.add("archive");
                    path.add(DateFormatUtils.format(new Date(), "yyyyMMdd"));
                    dto.setSavePath(CommonIOUtils.makeFilePath(path, dto.getFileName()));
                    OneFileOneThread thread2 = new OneFileOneThread(dto);
                    if (isDownload) {
                        thread2.run();
                    } else {
                        ehentai.setFinish(0);
                        tools.commonApiSave(ehentai);
                    }
                    if (thread2.getData().isComplete()) {
                        ehentai.setSavePath(dto.getSavePath());
                        ehentai.setFinish(1);
                        ehentai.setSize(thread2.getData().getFileLength());
                        tools.commonApiSave(ehentai);
                    }
                    try {
                        if (!ehentaiConfig.isUnzip()) {
                            return;
                        }
                        String dest = UnZipUtils.unZip(new File(dto.getSavePath()), "G:\\exhentai\\archive\\20201226\\sw\\" + dto.getFileName().replace(".zip", ""), "");
                        log.debug("解压成功，目录为：" + dest);
                    } catch (IOException e) {
                        log.error("解压失败");
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public String makePath(String fileName) {
        List<String> path = new ArrayList<>();
        path.add(ehentaiConfig.getSavePath());
        path.add("archive");
        path.add(DateFormatUtils.format(new Date(), "yyyyMMdd"));
        return CommonIOUtils.makeFilePath(path, fileName);
    }
}
