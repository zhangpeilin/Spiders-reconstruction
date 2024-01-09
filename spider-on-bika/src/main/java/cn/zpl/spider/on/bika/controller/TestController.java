package cn.zpl.spider.on.bika.controller;

import cn.zpl.util.DownloadTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@RestController
public class TestController {


    @GetMapping("create/{name}")
    public void test(@PathVariable("name") String name) {
        DownloadTools tools = DownloadTools.getInstance(5, name);
        for (int i = 0; i < 10; i++) {
            tools.submitTask(new TestShutdown());

        }
        tools.shutdown();
    }

    @GetMapping("shutdown/{name}")
    public void shutdown(@PathVariable("name") String name) {
        DownloadTools downloadTools = DownloadTools.getToolsByName(name);
        for (Future<Boolean> future : downloadTools.getFutureVector()) {
            future.cancel(true);
        }
        DownloadTools.getToolsByName(name);
    }
}

@Slf4j
class TestShutdown implements Callable<Boolean> {

    @Override
    public Boolean call() throws Exception {

        log.debug("开始执行任务");
        TimeUnit.SECONDS.sleep(20);
        log.debug("任务执行完成");
        return null;
    }
}
