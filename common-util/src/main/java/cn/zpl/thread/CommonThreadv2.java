package cn.zpl.thread;


import cn.zpl.pojo.DoRetry;
import cn.zpl.pojo.DownloadDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class CommonThreadv2<T> implements Runnable, Callable<T> {


    public void init() {
    }
    Vector<DownloadDTO> dtoList = new Vector<>();

    public DoRetry getDoRetry() {
        return doRetry;
    }

    DoRetry doRetry = new DoRetry();
    DownloadDTO dto = new DownloadDTO();
    public String url;

    public CommonThreadv2() {

    }

    public CommonThreadv2(Vector<DownloadDTO> dtoList) {
        this.dtoList = dtoList;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void run() {
        try {
            init();
            domain();
        } catch (Exception e) {
            if (doWhenFailed(e) && doRetry()) {
                log.error("执行出错：\n", e);
                log.error("2秒后重试");
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                log.debug("准备重试");
                run();
            } else {
                retryMaxFailed();
            }
        }
    }
    public boolean doWhenFailed(Exception e){
        return true;
    }

    /**
     * 尝试次数超过最大值后执行该方法
     */
    public void retryMaxFailed(){
    }

    public abstract void domain() throws Exception;

    public boolean doRetry(){
        if (doRetry.canDoRetry()) {
            doRetry.doRetry();
            return true;
        } else {
            return false;
        }

    }

    @Override
    public T call() throws Exception {
        return null;
    }
}
