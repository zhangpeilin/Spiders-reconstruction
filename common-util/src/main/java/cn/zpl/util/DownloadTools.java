package cn.zpl.util;

import cn.zpl.pojo.DownloadDTO;
import cn.zpl.pojo.MultiPartInfoHolder;
import cn.zpl.thread.CommonThread;
import cn.zpl.thread.DownloadWithMultipleThread;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;

import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DownloadTools {

    public Object getLock() {
        return lock;
    }

    public void setLock(Object lock) {
        this.lock = lock;
    }

    private Object lock;

    @Deprecated
    public static String cache = "n:\\视频爬虫\\temp\\";

    public int getParts() {
        return parts;
    }

    public void setParts(int parts) {
        this.parts = parts;
    }

    private int parts = 0;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name = "未命名";

    public void setSleepTimes(long sleepTimes) {
        this.sleepTimes = sleepTimes;
    }

    private long sleepTimes = 2000;

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    private ThreadPoolExecutor executor;

    public static DownloadTools getInstance(int coreSize){
        return new DownloadTools(coreSize);
    }

    private DownloadTools(int coreSise) {
        this.executor = new ThreadPoolExecutor(coreSise, coreSise, 200,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }

    public void restart(int coreSise){
        this.executor = new ThreadPoolExecutor(coreSise, coreSise, 200,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }
    public void setCorePoolSize(int count) {
        executor.setCorePoolSize(count);
        executor.setMaximumPoolSize(count);
    }

    public void ThreadExecutorAdd(Runnable thread) {
        try {
            executor.execute(thread);
        } catch (Exception e) {
            e.printStackTrace();
            //发生异常时，重启该任务，已下载片段会跳过
            executor.execute(thread);
        }
    }
    public void ThreadExecutorAdd(CommonThread thread) {
        try {
            executor.execute(thread);
        } catch (Exception e) {
            e.printStackTrace();
            //发生异常时，重启该任务，已下载片段会跳过
            executor.execute(thread);
        }
    }

    public void MultipleThread(DownloadDTO data) {
        try {
            long length = data.getFileLength();
            MultiPartInfoHolder infoHolder = data.getInfoHolder();
            File saveDir = new File(data.getSavePath());
            if (!saveDir.getParentFile().exists()) {
                if (!saveDir.getParentFile().mkdirs()) {
                    log.error("创建目录失败：" + saveDir);
                }
            }
            long threadcount = parts == 0 ? executor.getCorePoolSize() : parts;
            long size = length / Long.parseLong(String.valueOf(threadcount));
            for (int i = 0; i < threadcount; i++) {
                DownloadDTO copy = new DownloadDTO();
                BeanUtils.copyProperties(copy, data);
                long startIndex = i * size;
                long endIndex = (i + 1) * size - 1;
                if (i == threadcount - 1) {
                    endIndex = length - 1;
                }
                copy.setStartIndex(startIndex);
                copy.setEndIndex(endIndex);
                copy.setInfoHolder(infoHolder);
                executor.execute(new DownloadWithMultipleThread(copy));
            }
        } catch (Exception e) {
            e.printStackTrace();
            MultipleThread(data);
        }
    }

    public void MultipleThreadWithLog(DownloadDTO data) {
        try {
            long length = data.getFileLength();
            if (0 == length) {
                URLConnectionTool.getDataLength(data);
            }
            MultiPartInfoHolder infoHolder = data.getInfoHolder();
            File saveDir = new File(data.getSavePath());
            if (!saveDir.getParentFile().exists()) {
                if (!saveDir.getParentFile().mkdirs()) {
                    log.error("创建目录失败：" + saveDir);
                }
            }
            long threadcount = parts == 0 ? executor.getCorePoolSize() : parts;
            long size = length / Long.parseLong(String.valueOf(threadcount));
            for (int i = 0; i < threadcount; i++) {
                DownloadDTO copy = new DownloadDTO();
                BeanUtils.copyProperties(copy, data);
                long startIndex = i * size;
                long endIndex = (i + 1) * size - 1;
                if (i == threadcount - 1) {
                    endIndex = length - 1;
                }
                copy.setStartIndex(startIndex);
                copy.setEndIndex(endIndex);
                infoHolder.addPartInfo(startIndex, endIndex);
                copy.setInfoHolder(infoHolder);
                executor.execute(new DownloadWithMultipleThread(copy));
            }
        } catch (Exception e) {
            e.printStackTrace();
            MultipleThread(data);
        }
    }

    public void shutdown() {
        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(sleepTimes);
                log.info("【" + name + "】线程池，其中核心线程数目：" + executor.getPoolSize()
                        + "，待执行任务数目：" + executor.getQueue().size()
                        + "，已完成任务数目：" + executor.getCompletedTaskCount());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void keepRunning(long sleepTimes){
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(sleepTimes);
                log.info("【" + name + "】线程池，其中核心线程数目：" + executor.getPoolSize()
                        + "，待执行任务数目：" + executor.getQueue().size()
                        + "，已完成任务数目：" + executor.getCompletedTaskCount());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void printStatus() {
        log.info("【" + name + "】线程池，其中核心线程数目：" + executor.getPoolSize()
                + "，待执行任务数目：" + executor.getQueue().size()
                + "，已完成任务数目：" + executor.getCompletedTaskCount());
    }

    /**
     * 获取进度信息
     */
    public void progress() {

    }
}






