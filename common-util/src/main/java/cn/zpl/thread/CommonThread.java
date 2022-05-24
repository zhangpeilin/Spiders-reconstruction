package cn.zpl.thread;


import cn.zpl.pojo.DoRetry;
import cn.zpl.pojo.DownloadDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.Vector;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class CommonThread implements Runnable {


    Vector<DownloadDTO> dtoList = new Vector<>();

    public DoRetry getDoRetry() {
        return doRetry;
    }

    DoRetry doRetry = new DoRetry();
    DownloadDTO dto = new DownloadDTO();
    public String url;

    public CommonThread() {

    }

    public CommonThread(Vector<DownloadDTO> dtoList) {
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
            domain();
        } catch (Exception e) {
            if (doRetry()) {
                log.error("执行出错：\n", e);
                log.error("2秒后重试");
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                log.debug("准备重试");
                run();
            }
        }
    }

    public abstract void domain();

    public boolean doRetry(){
        if (doRetry.canDoRetry()) {
            doRetry.doRetry();
            return true;
        } else {
            return false;
        }

    }
}
