package cn.zpl.util;

import cn.zpl.pojo.DownloadDTO;
import cn.zpl.pojo.MultiPartInfoHolder;
import cn.zpl.thread.CommonThread;
import cn.zpl.thread.DownloadWithMultipleThread;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DownloadTools {

    public static ConcurrentHashMap<Integer, Map<String, DownloadTools>> executorCache = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<Integer, Vector<Future<Boolean>>> futureCache = new ConcurrentHashMap<>();

    public static DownloadTools getToolsByName(String name) {
        Map.Entry<Integer, Map<String, DownloadTools>> mapEntry = executorCache.entrySet().stream().filter(integerMapEntry -> integerMapEntry.getValue().get(name) != null).findFirst().orElse(null);
        if (mapEntry != null) {
            return executorCache.get(mapEntry.getKey()).get(name);
        } else {
            return DownloadTools.getInstance(1, name);
        }
    }

    public Vector<Future<Boolean>> getFutureVector() {
        return futureCache.get(this.hashCode()) == null ? new Vector<>() : futureCache.get(this.hashCode());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Map<String, DownloadTools> toolsMap = executorCache.remove(this.hashCode());
        if (toolsMap != null) {
            toolsMap.clear();
            toolsMap.put(name, this);
            executorCache.put(this.hashCode(), toolsMap);
        }
        this.name = name;
    }

    private String name = "未命名";

    public void setSleepTimes(long sleepTimes) {
        this.sleepTimes = sleepTimes;
    }

    private long sleepTimes = 2000;

    boolean needLog = true;

    public void setNeedLog(boolean needLog) {
        this.needLog = needLog;
    }

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    private ThreadPoolExecutor executor;

    public static DownloadTools getInstance(int coreSize, String... toolName){
        DownloadTools downloadTools = new DownloadTools(coreSize);
        if (toolName.length != 0 && !StringUtils.isEmpty(toolName[0])) {
            downloadTools.setName(toolName[0]);
        }
        executorCache.put(downloadTools.hashCode(), new HashMap<String, DownloadTools>(){
            {
                put(downloadTools.getName(), downloadTools);
            }
        });
        futureCache.put(downloadTools.hashCode(), new Vector<>());
        return downloadTools;
    }

    public void removeFromCache() {
        executorCache.remove(this.hashCode());
    }

    private DownloadTools(int coreSize) {
        this.executor = new ThreadPoolExecutor(coreSize, coreSize, 200,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }

    public void restart(int coreSize){
        this.executor = new ThreadPoolExecutor(coreSize, coreSize, 200,
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

            long threadCount = executor.getCorePoolSize();
            //如果大小超过500MB，则按照20MB一份切割
            if (length > 524288000) {
                threadCount = length / 20971520;
            }
            long size = length / Long.parseLong(String.valueOf(threadCount));
            for (int i = 0; i < threadCount; i++) {
                DownloadDTO copy = new DownloadDTO();
                BeanUtils.copyProperties(copy, data);
                long startIndex = i * size;
                long endIndex = (i + 1) * size - 1;
                if (i == threadCount - 1) {
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
            long threadCount = executor.getCorePoolSize();
            //如果大小超过500MB，则按照20MB一份切割
            if (length > 524288000) {
                threadCount = length / 20971520;
            }
            long size = length / Long.parseLong(String.valueOf(threadCount));
            for (int i = 0; i < threadCount; i++) {
                DownloadDTO copy = new DownloadDTO();
                BeanUtils.copyProperties(copy, data);
                long startIndex = i * size;
                long endIndex = (i + 1) * size - 1;
                if (i == threadCount - 1) {
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

    public void submitTask(Callable<Boolean> callable) {
        futureCache.get(this.hashCode()).add(executor.submit(callable));

    }

    public void shutdown() {
        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(sleepTimes);
                if (!needLog) {
                    continue;
                }
                log.info("【" + name + "】线程池，其中核心线程数目：" + executor.getPoolSize()
                        + "，待执行任务数目：" + executor.getQueue().size()
                        + "，已完成任务数目：" + executor.getCompletedTaskCount());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        executorCache.remove(this.hashCode());
    }
}






