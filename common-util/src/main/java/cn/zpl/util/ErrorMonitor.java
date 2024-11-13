package cn.zpl.util;

import cn.zpl.common.bean.RestResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@Slf4j
public class ErrorMonitor {  
    private final int errorThreshold;  
    private final AtomicInteger errorCount = new AtomicInteger(0);
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    public ErrorMonitor(int errorThreshold) {
        this.errorThreshold = errorThreshold;
    }

    public boolean recordError(String errorMessage) {
        synchronized (ErrorMonitor.class) {
            if (!isShutdown.get() && errorCount.incrementAndGet() > errorThreshold) {
                int currentCount = errorCount.get();
                isShutdown.set(true);
                stopThreadPool();
                sendSmsNotification(currentCount);
            }
            System.out.println("Error recorded: " + errorMessage);
            System.out.println("Current error count: " + errorCount.get());
            return isShutdown.get();
        }
    }

    private void stopThreadPool() {  
        System.out.println("Stopping thread pool due to excessive errors.");
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<RestResponse> forEntity = restTemplate.getForEntity("http://127.0.0.1:8084/common/api/shutdownAllPools", RestResponse.class);
        RestResponse response = forEntity.getBody();
        log.debug("关闭线程池：{}", response);
    }

    private void sendSmsNotification(int currentCount) {  
        // 这里是伪代码，你可以使用实际的短信发送服务来实现  
        String smsMessage = "Error threshold exceeded! Total errors: " + currentCount;  
        System.out.println("Sending SMS notification: " + smsMessage);  
        // 短信发送代码...  
    }  
}