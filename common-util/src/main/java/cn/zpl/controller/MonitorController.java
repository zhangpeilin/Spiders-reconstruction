package cn.zpl.controller;

import cn.zpl.common.bean.RestResponse;
import cn.zpl.util.DownloadTools;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/common/api")
public class MonitorController {


    @GetMapping("/getStatus/{toolName}")
    public String getTheDownloadToolsStatus(@PathVariable("toolName") String toolName) {
        DownloadTools downloadTools = DownloadTools.getToolsByName(toolName);
        ThreadPoolExecutor executor = downloadTools.getExecutor();
        return ("【" + downloadTools.getName() + "】线程池，其中核心线程数目：" + executor.getPoolSize()
                + "，待执行任务数目：" + executor.getQueue().size()
                + "，已完成任务数目：" + executor.getCompletedTaskCount());
    }

    @GetMapping("/shutdown/{toolName}")
    public String shutdown(@PathVariable("toolName") String toolName) {
        DownloadTools downloadTools = DownloadTools.getToolsByName(toolName);
        downloadTools.getExecutor().shutdownNow();
        ThreadPoolExecutor executor = downloadTools.getExecutor();
        return ("线程池执行结果：【" + downloadTools.getName() + "】线程池，其中核心线程数目：" + executor.getPoolSize()
                + "，待执行任务数目：" + executor.getQueue().size()
                + "，已完成任务数目：" + executor.getCompletedTaskCount());
    }

    @GetMapping("/shutdownAllPools")
    public RestResponse shutdownAllPools() {
        DownloadTools.shutDownAll();
        return RestResponse.ok("线程池已全部全部关闭");
    }

    @GetMapping("/start")
    public void start(){
        DownloadTools downloadTools = DownloadTools.getInstance(50, "测试线程池");
        for (int i = 0; i < 100; i++) {
            downloadTools.ThreadExecutorAdd(() -> {
                while (true) {
                    System.out.println("线程执行中");
                }
//                try {
//                    TimeUnit.SECONDS.sleep(30);
//                } catch (InterruptedException e) {
//                    System.out.println("线程被终止");
//                }
            });
        }
        downloadTools.shutdown();
    }

    @GetMapping("/getAllStatus")
    public String getAllDownloadToolsStatus() {
        StringBuilder stringBuilder = new StringBuilder();
        DownloadTools.executorCache.forEach((hashCode, toolMap) -> {
            String toolName = toolMap.keySet().stream().findFirst().orElse(null);
            DownloadTools downloadTools = toolMap.get(toolName);
            ThreadPoolExecutor executor = downloadTools.getExecutor();
            stringBuilder.append("【").append(downloadTools.getName()).append("】线程池，其中核心线程数目：").append(executor.getPoolSize()).append("，待执行任务数目：").append(executor.getQueue().size()).append("，已完成任务数目：").append(executor.getCompletedTaskCount()).append("\n");
        });
        return stringBuilder.toString();
    }
}
