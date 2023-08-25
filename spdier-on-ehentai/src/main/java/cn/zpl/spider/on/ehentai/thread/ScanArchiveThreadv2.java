package cn.zpl.spider.on.ehentai.thread;

import cn.zpl.common.bean.Ehentai;
import cn.zpl.common.bean.RestResponse;
import cn.zpl.config.SpringContext;
import cn.zpl.pojo.Data;
import cn.zpl.spider.on.ehentai.config.EhentaiConfig;
import cn.zpl.spider.on.ehentai.util.EUtil;
import cn.zpl.spider.on.ehentai.util.RabbitMQSender;
import cn.zpl.thread.CommonThreadv2;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.CrudTools;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
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
public class ScanArchiveThreadv2 extends CommonThreadv2<Boolean> {

    private boolean result = false;
    EhentaiConfig ehentaiConfig;
    Pattern pattern = Pattern.compile("[\\d,]+");

    EUtil util;
    public ScanArchiveThreadv2(String url) {
        this.setUrl(url);
        ehentaiConfig = SpringContext.getBeanWithGenerics(EhentaiConfig.class);
        util = new EUtil();
    }

    @Override
    public void retryMaxFailed() {
        log.error("重试超过最大次数，将错误url发送到错误队列");
        RabbitMQSender msgSender = SpringContext.getBeanWithGenerics(RabbitMQSender.class);
        msgSender.sendMsg(getUrl(), "ehentai");
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
        if (eh != null) {
            log.debug("{}-->{}已下载，跳过", getUrl(), eh.getTitle());
            result = true;
            return;
        }
        CrudTools tools = SpringContext.getBeanWithGenerics(CrudTools.class);
        EhentaiConfig ehentaiConfig = SpringContext.getBeanWithGenerics(EhentaiConfig.class);
        Data data = new Data();
        Ehentai ehentai;
        data.setUrl(getUrl());
        data.setHeader(ehentaiConfig.getEhentaiCookies());
        data.setProxy(true);
        data.setAlwaysRetry();
        CommonIOUtils.withTimer(data);
        Document document = Jsoup.parse(data.getResult());
        Element fileSize = document.selectFirst(":containsOwn(File Size)");
        Element favcount = document.selectFirst("td#favcount");
        Elements tagList = document.select("div#taglist td");
        Element rating = document.selectFirst("td#rating_label");
        Element galleryInfo = document.selectFirst("div#gdd");
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
            result = true;
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
                    }
                }
                if (ehentaiConfig.isSaveDb()) {
                    RestResponse restResponse = tools.commonApiSave(ehentai);
                    log.debug("保存是否成功：{}", restResponse.isSuccess());
                    if (restResponse.isSuccess()) {
                        result = true;
                    }
                }
            }
        }
    }

    @Override
    public Boolean call() {
        run();
        return result;
    }
}
