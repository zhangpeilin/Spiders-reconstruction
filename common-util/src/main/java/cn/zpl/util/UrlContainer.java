package cn.zpl.util;

import java.io.Serializable;

public class UrlContainer implements Serializable {

    private String referer;
    private int retryCount = 0;
    private String url;
    private boolean proxy;
    private String headers;
    private long sleepMills;
    private int retryMaxCount = 5;
    private String cookies;
    public UrlContainer(String url) {
        this.url = url;
        this.proxy = false;
    }
    public UrlContainer(String url, String headers) {
        this.url = url;
        this.proxy = false;
        this.headers = headers;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public int getRetryMaxCount() {
        return retryMaxCount;
    }

    public void setRetryMaxCount(int retryMaxCount) {
        this.retryMaxCount = retryMaxCount;
    }

    public boolean canDoRetry() {
        if (this.retryCount < retryMaxCount) {
            return true;
        } else {
            return false;
        }
    }

    public void doRetry() {
        retryCount++;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isProxy() {
        return proxy;
    }

    public void setProxy(boolean proxy) {
        this.proxy = proxy;
    }

    public boolean isHttps() {
        return url.startsWith("https");
    }

    public long getSleepMills() {
        return sleepMills;
    }

    public void setSleepMills(long sleepMills) {
        this.sleepMills = sleepMills;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getCookies() {
        return cookies;
    }

    public void setCookies(String cookies) {
        this.cookies = cookies;
    }
}
