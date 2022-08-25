package cn.zpl.tencent.controller;

import cn.zpl.tencent.thread.AsyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * [异步调用控制器]
 *
 * @author: courage007
 * @date: 2022/04/17 19:39
 */
@RestController
@RequestMapping("/hello")
public class AsyncController {
    @Autowired
    private AsyncService asyncService;

    @PostMapping("/async-task")
    public void doAsyncTask() {
        System.out.println("doAsyncTask begin");
        asyncService.asyncInVoid();
        System.out.println("doAsyncTask end");
    }

    @PostMapping("/async-task-with-result")
    public Integer doAsyncTaskWithResult() {
        System.out.println("doAsyncTaskWithResult begin");
        Future<Integer> result = asyncService.asyncWithResult();
        try{
            System.out.println("doAsyncTaskWithResult end");
            return result.get();
        } catch (ExecutionException | InterruptedException ex) {
            throw new RuntimeException("execute failed");
        }
    }

    @PostMapping("/async-task-with-result-2")
    public Integer doAsyncTaskWithResult2() {
        System.out.println("doAsyncTaskWithResult2 begin");
        Future<Integer> result = asyncService.asyncWithResult();
        try{
            System.out.println("doAsyncTaskWithResult end");
            Thread.sleep(15000);
            // get()方法会一直阻塞，方法最后的执行时间，依赖执行时间最长的线程
            return result.get();
        } catch (ExecutionException | InterruptedException ex) {
            throw new RuntimeException("execute failed");
        }
    }
}
