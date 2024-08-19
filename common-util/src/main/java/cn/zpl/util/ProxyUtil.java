package cn.zpl.util;


import cn.zpl.pojo.Data;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.StringUtils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ProxyUtil {

    public static LoadingCache<String, String> cache;
    public static void checkProxy(String url, String website, String headers, CheckCallBack callBack) {
        synchronized (ProxyUtil.class) {
            String lastTimestamp = null;
            if (cache != null) {
                lastTimestamp = String.valueOf(System.currentTimeMillis());
                //result = url:timestamp
                //website=website:网站域名
                String result;
                try {
                    result = cache.get("website#" + website);
                } catch (ExecutionException e) {
                    log.error("变更节点失败");
                    return;
                }
                String timestamp = cache.getIfPresent(result);
                if (timestamp == null) {
                    try {
                        lastTimestamp = cache.get("url#" + url);
                        cache.put("website#" + website, "url#" + url);
                    } catch (ExecutionException e) {
                        log.error("变更节点失败");
                        return;
                    }
                }
            long timestamp1 = Long.parseLong(StringUtils.isEmpty(timestamp) ? lastTimestamp : timestamp);
            if (compareTimeStamps(timestamp1, System.currentTimeMillis())) {
//                cache.invalidate("website#" + website);
//                cache.invalidate(result);
                cache.invalidateAll();
                checkProxy(url, website, headers, callBack);
            } else {
                return;
            }
        } else {
                cache = CacheBuilder.newBuilder().maximumSize(200000).expireAfterWrite(2000, TimeUnit.HOURS).build(new CacheLoader<String, String>() {
                    @Override
                    public @NotNull String load(@NotNull String key) {
                        if (!key.contains("#")) {
                            String ifPresent = cache.getIfPresent(key);
                            return ifPresent == null ? "" : ifPresent;
                        }
                        String[] split = key.split("#");
                        String model = split[0];
                        String realKey = split[1];
                        if ("website".equalsIgnoreCase(model)) {
                            return "url#" + url;
                        }
                        if ("url".equalsIgnoreCase(model)) {
                            Data data = new Data();
                            data.setUrl(realKey);
                            data.setHeader(headers);
                            data.setProxy(true);
                            data.setAlwaysRetry();
                            CommonIOUtils.withTimer(data);
                            while (callBack.call(data.getResult())) {
                                CommonIOUtils.withTimer(data);
                                try {
                                    TimeUnit.SECONDS.sleep(2);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                log.debug("更换节点逻辑，尝试url为：{}", url);
                                //更换节点
                            }
                            //校验节点连通性，如果正常，返回时间戳
                            return String.valueOf(System.currentTimeMillis());
                        }
                        return "";
                    }
                });
                checkProxy(url, website, headers, callBack);
            }
        }
    }

    public static boolean compareTimeStamps(long timestamp1, long timestamp2) {
        long difference = Math.abs(timestamp1 - timestamp2);
        long fiveMinutesInMillis = 10 * 1000; // 5 minutes in milliseconds

        return difference > fiveMinutesInMillis;
    }
}