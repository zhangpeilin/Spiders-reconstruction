package cn.zpl.pojo;

import java.io.Serializable;

public class DoRetry implements Serializable {
    private int retryCount = 0;
    private String url;

    public boolean isAlwaysRetry() {
        return alwaysRetry;
    }

    public DoRetry setAlwaysRetry(boolean alwaysRetry) {
        this.alwaysRetry = alwaysRetry;
        return this;
    }

    //一直要重试标志，默认为false
    private boolean alwaysRetry = false;

    public int getRetryMaxCount() {
        return retryMaxCount;
    }

    public void setRetryMaxCount(int retryMaxCount) {
        this.retryMaxCount = retryMaxCount;
    }

    private int retryMaxCount = 10;

    public boolean canDoRetry(){
        if (alwaysRetry) {
            return true;
        }
        return this.retryCount < retryMaxCount;
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
}
