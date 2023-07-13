package cn.zpl.spider.on.ehentai.thread;

import cn.zpl.config.SpringContext;
import cn.zpl.pojo.Data;
import cn.zpl.spider.on.ehentai.config.EhentaiConfig;
import cn.zpl.thread.CommonThread;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.DownloadTools;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadPageThread  extends CommonThread {

    @Override
    public void domain() throws Exception {
        EhentaiConfig ehentaiConfig = SpringContext.getBeanWithGenerics(EhentaiConfig.class);
        Data data = new Data();
        data.setUrl(getUrl());
        data.setHeader(ehentaiConfig.getEhentaiCookies());
        data.setProxy(true);
        data.setAlwaysRetry();
        CommonIOUtils.withTimer(data);
        Document document = Jsoup.parse(data.getResult());
        DownloadTools downloadTools = DownloadTools.getInstance(2);
        String scriptContainTheStr = CommonIOUtils.getScriptContainTheStr(document, "build_rangebar");
        Pattern pattern = Pattern.compile("var\\s+(\\w+)=\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(scriptContainTheStr);
        Map<String, String> pageInfoMap = new HashMap<>();
        while (matcher.find()) {
            String variableName = matcher.group(1);
            String variableValue = matcher.group(2);
            pageInfoMap.put(variableName, variableValue);
        }
        Elements urlList = document.select("div.itg.gld div.gl1t > a");
        for (Element element : urlList) {
            ScanArchiveThread scanArchiveThread = new ScanArchiveThread(element.attr("href"));
            downloadTools.ThreadExecutorAdd(scanArchiveThread);
        }
        downloadTools.shutdown();
        if (pageInfoMap.get("nexturl") == null) {
            return;
        }
        setUrl(pageInfoMap.get("nexturl"));
        domain();
    }
}
