package cn.zpl.spider.on.ehentai.thread;

import cn.zpl.common.bean.Ehentai;
import cn.zpl.common.bean.RestResponse;
import cn.zpl.pojo.Data;
import cn.zpl.pojo.DownloadDTO;
import cn.zpl.spider.on.ehentai.config.Params;
import cn.zpl.thread.CommonThread;
import cn.zpl.thread.OneFileOneThread;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.CrudTools;
import cn.zpl.util.UnZipUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DownLoadArchiveThread extends CommonThread {

    public DownLoadArchiveThread(String url) {
        this.setUrl(url);
    }

    @Override
    public void domain() {
        Data data = new Data();
        Ehentai ehentai;
        data.setUrl(getUrl());
        data.setHeader(Params.exhentai_cookies);
        data.setProxy(true);
        data.setAlwaysRetry();
        CommonIOUtils.withTimer(data);
        Document document = Jsoup.parse(data.getResult());
        Element favcount = document.selectFirst("td#favcount");
        Elements tagList = document.select("div#taglist td");
        Map<String, Object> infomation = new HashMap<>();
        String key = null;
        for (int i = 0; i < tagList.size(); i++) {
            //偶数时是大类名，奇数是具体值
            if (i % 2 == 0) {
                key = tagList.get(i).text().replace(":", "");
            } else {
                Elements children = document.select("div#taglist td").get(i).children();
                StringBuffer values = new StringBuffer("[");
                for (Element child : children) {
                    values.append(child.text()).append(",");
                }
                values.deleteCharAt(values.length() - 1).append("]");
                infomation.put(key, values.toString());
            }
        }
        ehentai = JSON.toJavaObject(new JSONObject(infomation), Ehentai.class);
        Pattern pattern = Pattern.compile("[\\d,]+");
        assert favcount != null;
        Matcher favcountMatcher = pattern.matcher(favcount.text());
        if (favcountMatcher.find()) {
            try {
                ehentai.setFavcount(NumberFormat.getNumberInstance(Locale.US).parse(favcountMatcher.group()).toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        Elements view_gallery = document.getElementsMatchingText("View Gallery");
        if (view_gallery.size() != 0) {
            url = view_gallery.attr("href");
            run();
            return;
        }
        Elements elements = document.select("div#gd5 p.g2");

        for (Element element1 : elements) {
            Element a = element1.selectFirst("a");
            if (a.text().toLowerCase().contains("archive")) {
                String js = a.attr("onclick");
                Data archive = new Data();
                archive.setHeader(Params.exhentai_cookies);
                archive.setUrl(js.substring(js.indexOf("http"), js.lastIndexOf("'")));
                archive.setProxy(true);
                archive.setAlwaysRetry();
                CommonIOUtils.withTimer(archive);
                Document tmp = Jsoup.parse(archive.getResult());
                String title = tmp.selectFirst("div#db > h1").text();
                Element form = tmp.selectFirst("form");
                assert form != null;
                Elements freeMark = Objects.requireNonNull(form.previousElementSibling()).getElementsMatchingText("Free");
                if (freeMark.isEmpty()) {
                    log.error("下载消耗点数，链接地址记录：" + url);
                    log.error(tmp.select("   div#db h1").text());
                    Matcher matcher = pattern.matcher(form.previousElementSibling().text());
                    if (matcher.find()) {
                        String GPStr = matcher.group(0);
                        int GP = 0;
                        try {
                            GP = NumberFormat.getNumberInstance(Locale.US).parse(GPStr).intValue();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        ehentai.setTitle(title);
                        ehentai.setUrl(url);
                        ehentai.setCost(String.valueOf(GP));
                        ehentai.setCreate_time(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
//                        RestResponse restResponse = CrudTools.saveEhentai(ehentai);
                        RestResponse restResponse = CrudTools.commonApiSave(ehentai);
                        log.debug("保存是否成功：{}", restResponse.isSuccess());

                        if (GP > 20000) {
                            log.error("当前漫画未下载：{}", url);
                            return;
                        }
                        log.info("下载消耗点数：" + GP);
                    }
//                    return;
                }
                if (form.attr("action").startsWith("http")) {
                    Data d1 = new Data();
                    d1.setUrl(form.attr("action"));
                    d1.setHeader(Params.exhentai_cookies + "\nContent-Type: application/x-www-form-urlencoded; charset=UTF-8");
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
                    data1.setUrl(tmpUrl.selectFirst("a").attr("href"));
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
                    path.add(Params.save_path);
                    path.add("archive");
                    path.add(DateFormatUtils.format(new Date(), "yyyyMMdd"));
                    dto.setSavePath(CommonIOUtils.makeFilePath(path, dto.getFileName()));
                    dto.setAlwaysRetry();
                    OneFileOneThread thread2 = new OneFileOneThread(dto);
                    thread2.setFrame(Params.mainFrame);
                    thread2.run();
                    try {
                        if (!Params.unzip) {
                            return;
                        }
                        String dest = UnZipUtils.unZip(new File(dto.getSavePath()), "G:\\exhentai\\archive\\20201226\\丝袜\\" + dto.getFileName().replace(".zip", ""), "");
                        log.debug("解压成功，目录为：" + dest);
                        if (Params.mainFrame != null)
                            Desktop.getDesktop().open(new File(dest));
                    } catch (IOException e) {
                        log.error("解压失败");
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
