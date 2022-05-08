package cn.zpl.thread;


import cn.zpl.pojo.DownloadDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.Vector;

@Slf4j
public abstract class CommonThread implements Runnable {


    Vector<DownloadDTO> dtoList = new Vector<>();
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
            log.error("执行出错：\n", e);
            log.error("5秒后重试");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            log.debug("准备重试");
            run();
        }
    }

    public abstract void domain();

}
