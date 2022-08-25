package cn.zpl.tencent.thread;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;

@Service
@Slf4j
public class AsyncService {
    /**
     * 异步调用示例，无返回值
     */
    @Async
    public void asyncInVoid() {
        System.out.println("asyncInVoid begin");
        try {
            Thread.sleep(20000);
            System.out.println("current time is: " + System.currentTimeMillis());
        } catch (InterruptedException ex) {
            log.error("thread sleep failed: ", ex);
        }
        throw new RuntimeException("asdfasd");
//        System.out.println("asyncInVoid end");
    }

    /**
     * 异步调用示例，有返回值
     *
     * @return
     */
    @Async
    public Future<Integer> asyncWithResult() {
        System.out.println("asyncWithResult begin");
        try {
            Thread.sleep(10000);
            System.out.println("current time is: " + System.currentTimeMillis());
        } catch (InterruptedException ex) {
            log.error("thread sleep failed: ", ex);
        }
        System.out.println("asyncWithResult end");
        return new AsyncResult<Integer>(1000);
    }
}
