package cn.zpl.spider.on.ehentai.util;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

@Slf4j
public class RedissonUtil {

    private static final Long time_locked = 500 * 1000L;
    private static final String key_locked = "myLock";

    private static RedissonClient redissonClient;

    public static void main(String[] args) {
        initRedissonClient();
        lock();
    }

    private static void initRedissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.139.130:31210").setPassword("123456");
        redissonClient = Redisson.create(config);
    }

    private static void lock() {
        RLock lock1 = redissonClient.getLock(key_locked);
        System.out.println(lock1);
        log.error("lock1 clas: {}", lock1.getClass());
        lock1.lock();
        log.info("lock, ThreadName：{} id：{} locked,重入次数:{}", Thread.currentThread().getName(), Thread.currentThread().getId(), lock1.getHoldCount());
        try {
            new Thread(() ->{
                RLock lock = redissonClient.getLock(key_locked);
                lock.lock();
            }).start();
            Thread.sleep(time_locked);
            reLock();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock1.unlock();
            log.info("lock, ThreadName: {} id: {} unlock, 重入次数: {}", Thread.currentThread().getName(), Thread.currentThread().getId(), lock1.getHoldCount());
        }
    }

    private static void reLock() {
        RLock lock1 = redissonClient.getLock(key_locked);
        lock1.lock();
        log.info("reLock, ThreadName:{} id:{} locked,重入次数:{}", Thread.currentThread().getName(), Thread.currentThread().getId(), lock1.getHoldCount());
        try {
            Thread.sleep(time_locked);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock1.unlock();
            log.info("reLock, ThreadName:{} id:{} locked,重入次数:{}", Thread.currentThread().getName(), Thread.currentThread().getId(), lock1.getHoldCount());
        }
    }
}
