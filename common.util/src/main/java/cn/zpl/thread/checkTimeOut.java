package cn.zpl.thread;

import cn.zpl.myInterface.DownloadThreadInterface;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class checkTimeOut extends Thread {

    private DownloadThreadInterface run;
    private Long begin;

    private void setBegin() {
        this.begin = System.currentTimeMillis();
    }

    private void setEnd() {
        this.end = System.currentTimeMillis();
    }

    @NotNull
    @Contract(pure = true)
    private String getCost() {
        long duration = (end - begin) / 1000;
        return "耗时: " + (duration / 60) + "分" + duration % 60 + "秒";
    }

    private Long end;
    private int seconds;

    checkTimeOut(DownloadThreadInterface run, int seconds) {
        this.run = run;
        this.seconds = seconds;
    }

    public void run() {
        timeOut();
    }

    private void timeOut() {
        try {
            setBegin();
            log.debug("开始计时");
            Thread.sleep(seconds * 1000);
            run.setTimeOut(true);
        } catch (InterruptedException e) {
            log.debug("计时停止");
            setEnd();
            log.debug(getCost());
        }
    }
}