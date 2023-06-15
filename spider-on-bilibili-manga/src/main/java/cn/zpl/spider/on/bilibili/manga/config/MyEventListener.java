package cn.zpl.spider.on.bilibili.manga.config;

import cn.zpl.common.bean.BilibiliManga;
import cn.zpl.config.SpringContext;
import cn.zpl.spider.on.bilibili.manga.thread.BuyWaitFreeEpisodeThread;
import cn.zpl.spider.on.bilibili.manga.util.BilibiliCommonUtils;
import cn.zpl.spider.on.bilibili.manga.util.BilibiliMangaProperties;
import cn.zpl.util.DownloadTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class MyEventListener {

    @Resource
    BilibiliCommonUtils bilibiliCommonUtils;

    public final  static ConcurrentHashMap<String, String> chapterTime = new ConcurrentHashMap<>();

    BlockingQueue<Future<Map<String, String>>> futureQueue = new LinkedBlockingQueue<>(5);


    @EventListener(ApplicationReadyEvent.class)
    @SuppressWarnings("")
    public void onApplicationReadyEvent() {
        log.debug("Spring Boot 完成启动！");
        DownloadTools downloadTools = DownloadTools.getInstance(20);

        int hashCode = 0;
        HashMap<String, Long> minWait = new HashMap<>();
        AtomicBoolean exec = new AtomicBoolean(false);
        while (true) {
            try {
                //1、获取最近一批，如果这一批中存在大于0的，那么直接丢进线程池执行；minTime赋值为最小需要等待的秒数。
                //如果为空，则将列表存入
                //如果上次没有任务成功执行，则判断是否存在等待时间，如果存在，则进行等待
                if (!futureQueue.isEmpty()) {
                    TimeUnit.SECONDS.sleep(5);
                    continue;
                }
                if (!exec.get() && !minWait.isEmpty()) {
                    log.debug("上次没有任务成功执行，准备休眠，休眠时间{}秒", minWait.get("wait"));
                    Thread mainThread = Thread.currentThread();
                    int finalHashCode = hashCode;
                    new Thread(() -> {
                        while (true) {
                            try {
                                TimeUnit.SECONDS.sleep(1);
                            } catch (Exception ignored) {
                            }
                            if (finalHashCode != chapterTime.hashCode()) {
                                log.debug("检测到数组改变，终止休眠，开始遍历");
                                mainThread.interrupt();
                                break;
                            }
                        }
                    }).start();
                    try {
                        TimeUnit.SECONDS.sleep(minWait.get("wait"));
                    } catch (InterruptedException e) {
                        log.debug("休眠被终止，开始执行遍历");
                    }
                }
                if (chapterTime.isEmpty()) {
                    log.debug("首次执行，填充chapterTime");
                    List<BilibiliManga> waitList = bilibiliCommonUtils.getWaitList();
                    waitList.forEach(bilibiliManga -> chapterTime.put(bilibiliManga.getChapterWaitBuy(), bilibiliManga.getWaitFreeAt()));
                }
                exec.set(false);
                //遍历map，将其中waitTime大于0的放入线程池执行
                chapterTime.forEach((chapterId, wait) -> {
                    Long time2Wait = getTime2Wait(wait);
                    if (time2Wait > 0) {
                        log.debug("存在完成等待的manga，开始调用下载线程");
                        exec.set(true);
                        BuyWaitFreeEpisodeThread buyThread = SpringContext.getBeanWithGenerics(BuyWaitFreeEpisodeThread.class);
                        buyThread.setEpId(chapterId);
                        try {
                            futureQueue.put(downloadTools.getExecutor().submit(buyThread));
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        if (!futureQueue.isEmpty()) {
                            getResultDealer().start();
                        }
                    } else {
                        time2Wait = time2Wait * -1;
                        minWait.putIfAbsent("wait", time2Wait);
                        Long finalTime2Wait = time2Wait;
                        minWait.computeIfPresent("wait", (key, aLong) -> {
                            log.debug("当前最小等待值为{}，比较值为{}",aLong, finalTime2Wait);
                            if (finalTime2Wait < aLong) {
                                log.debug("比较值更小，替换");
                                return finalTime2Wait;
                            } else {
                                log.debug("现存值更小，保留");
                                return aLong;
                            }
                        });
                    }
                });
                hashCode = chapterTime.hashCode();
            } catch (Exception ignored) {
            }
        }
    }

    public Long getTime2Wait(String targetTimeStr, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        LocalDateTime time = LocalDateTime.parse(targetTimeStr, formatter);
        LocalDateTime now = LocalDateTime.now();
        //如果time晚于now，则结果为负数，例如10:00比9:30晚30分钟，between = -30
        return ChronoUnit.SECONDS.between(time, now);
    }

    public Long getTime2Wait(String targetTimeStr) {
        return getTime2Wait(targetTimeStr, "yyyy-MM-dd HH:mm:ss") - 5;
    }

    public Thread getResultDealer() {
        return new Thread(() ->{
            try {
                while (!futureQueue.isEmpty()) {
                    log.debug("开始等待执行结果");
                    Future<Map<String, String>> peek = futureQueue.peek();
                    if (peek == null) {
                        log.debug("队列为空，等待30秒钟");
                        TimeUnit.SECONDS.sleep(30);
                        continue;
                    }
                    log.debug("队列中获取首位元素");
                    if (peek.isDone()) {
                        log.debug("元素执行完成，将执行结果放回chapterTime中");
                        Future<Map<String, String>> take = futureQueue.take();
                        log.debug(take.get().toString());
                        chapterTime.putAll(take.get());
                    } else {
                        TimeUnit.SECONDS.sleep(5);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
}