package cn.zpl.spider.on.ehentai.thread;

import cn.zpl.config.SpringContext;
import cn.zpl.pojo.Data;
import cn.zpl.spider.on.ehentai.config.EhentaiConfig;
import cn.zpl.spider.on.ehentai.util.EUtil;
import cn.zpl.thread.CommonThread;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.DownloadTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DownloadPageThread extends CommonThread {

    private boolean download = false;

    private boolean recursive = true;

    private AtomicInteger pageCount = new AtomicInteger(0);

    public void setDownload(boolean download) {
        this.download = download;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public void setPageCount(int i) {
        pageCount = new AtomicInteger(i);
    }


    @Override
    public void domain() throws Exception {
        EhentaiConfig ehentaiConfig = SpringContext.getBeanWithGenerics(EhentaiConfig.class);
        Data data = new Data();
        data.setUrl(getUrl());
        EUtil.setCookieHeader(data, ehentaiConfig.getEhentaiCookies());
        data.setProxy(true);
        data.setAlwaysRetry();
        CommonIOUtils.withTimer(data);
        Document document = Jsoup.parse(data.getResult());
        DownloadTools downloadTools = DownloadTools.getInstance(3);
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
        ConcurrentHashMap<String, Future<Boolean>> result = new ConcurrentHashMap<>();
        for (Element element : urlList) {
            String url = element.attr("href");
            if (download) {
                DownLoadArchiveThread downLoadArchiveThread = SpringContext.getBeanWithGenerics(DownLoadArchiveThread.class);
                downLoadArchiveThread.setUrl(url);
                downLoadArchiveThread.setCost(-1);
                downloadTools.ThreadExecutorAdd(downLoadArchiveThread);
            } else {
                //local 模式仅支持下载，扫描模式不再提供
                log.debug("local 模式不支持仅扫描，跳过：{}", url);
            }
        }
        downloadTools.shutdown();
        Optional<Future<Boolean>> any = result.values().parallelStream().filter(booleanFuture -> {
            try {
                return booleanFuture.isDone() && !booleanFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }).findAny();
        //如果存在下载失败的记录，则表明代理可能出现异常，将异常页面url记录到本地文件并退出
        if (any.isPresent()) {
            log.error("该页存在失败明细，判断代理异常，停止遍历并记录异常页面url");
            File failedFile = new File(ehentaiConfig.getSavePath(), "failed_pages.txt");
            try {
                FileUtils.writeStringToFile(failedFile, DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss") + " " + getUrl() + "\n", StandardCharsets.UTF_8, true);
            } catch (Exception e) {
                log.error("异常页面url记录失败", e);
            }
            return;
        }
        if (!recursive || pageInfoMap.get("nexturl") == null) {
            return;
        }
        if (pageCount.getAndDecrement() > 0) {
            setUrl(pageInfoMap.get("nexturl"));
            domain();
        }
    }

    public AtomicInteger getPageCount() {
        return pageCount;
    }
}
