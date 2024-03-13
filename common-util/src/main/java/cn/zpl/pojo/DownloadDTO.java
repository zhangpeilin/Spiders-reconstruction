package cn.zpl.pojo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
@Slf4j
public class DownloadDTO implements Serializable {
    private static final long serialVersionUID = -7236529950036387800L;
    private String url;
    private String savePath;
    private boolean needLog;
    private boolean isProxy;
    private String Referer;
    private String fileName;
    private String webSite;
    private long startIndex;
    private long endIndex;
    private String header;
    private Logger logger;
    private String charsetName;
    private String id;
    private String type;
    private Map<String, AtomicInteger> progress = new HashMap<>();
    /**
     * 是否严格校验，true表示需要校验响应头中的文件类型字段不为空
     */
    private boolean isStrict;
    //默认不是图片
    private boolean isImage;
    private List<String> pathMake = new ArrayList<>();
    private DoRetry doRetry;
    private SynchronizeLock synchronizeLock;
    private long fileLength = 0;

    private boolean isComplete;

    private MultiPartInfoHolder infoHolder;

    public DownloadDTO() {
        this.isProxy = false;
        this.doRetry = new DoRetry();
        this.doRetry.setRetryMaxCount(3);
        this.needLog = true;
        this.synchronizeLock = new SynchronizeLock();
        this.isImage = false;
        this.infoHolder = new MultiPartInfoHolder(this);
        this.charsetName = "gbk";
        this.isStrict = false;
    }

    public boolean isImage() {
        return isImage;
    }

    public void setImage(boolean image) {
        isImage = image;
    }

    public long getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(long startIndex) {
        this.startIndex = startIndex;
    }

    public long getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(long endIndex) {
        this.endIndex = endIndex;
    }

    public String getWebSite() {
        return webSite == null ? "" : webSite;
    }

    public void setWebSite(String webSite) {
        this.webSite = webSite;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getReferer() {
        return Referer;
    }

    public void setReferer(String referer) {
        Referer = referer;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(@NotNull String url) {
        if (!url.startsWith("http")) {
            this.url = "http:" + url;
        } else {
            this.url = url;
        }
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public boolean isNeedLog() {
        return needLog;
    }

    public void setNeedLog(boolean needLog) {
        this.needLog = needLog;
    }

    public boolean isProxy() {
        return isProxy;
    }

    public void setProxy(boolean proxy) {
        isProxy = proxy;
    }

    public Object getSynchronizeLock() {
        return synchronizeLock;
    }

    public void setSynchronizeLock(SynchronizeLock synchronizeLock) {
        this.synchronizeLock = synchronizeLock;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public List<String> getPathMake() {
        return pathMake;
    }

    public void setPathMake(List<String> pathMake) {
        this.pathMake = pathMake;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public String getCharsetName() {
        return charsetName;
    }

    public void setCharsetName(String charsetName) {
        this.charsetName = charsetName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isStrict() {
        return isStrict;
    }

    public void setStrict(boolean strict) {
        isStrict = strict;
    }

    public void setAlwaysRetry(){
        this.doRetry.setAlwaysRetry(true);
    }

    public void stopRetry(){
        doRetry.setAlwaysRetry(false).setRetryMaxCount(0);
    }

    public void setComplete() {
        if (id == null) {
            return;
        }
        if (progress.get(id) == null) {
            progress.put(id, new AtomicInteger(1));
        }
        progress.get(id).incrementAndGet();
    }

    public void setProgress(Map<String, AtomicInteger> progress) {
        this.progress = progress;
    }

    public boolean doRetry(){
        if (doRetry.canDoRetry()) {
            doRetry.doRetry();
            return true;
        } else {
            return false;
        }

    }

    public void resetRetry() {
        doRetry.setRetryMaxCount(3);
    }

    public MultiPartInfoHolder getInfoHolder() {
        return infoHolder;
    }

    public void setInfoHolder(MultiPartInfoHolder infoHolder) {
        this.infoHolder = infoHolder;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }
}

