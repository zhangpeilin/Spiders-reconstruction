package cn.zpl.spider.on.ehentai.util;

import cn.zpl.common.bean.Ehentai;
import cn.zpl.config.SpringContext;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.CrudTools;
import com.google.common.base.CaseFormat;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class EUtil {

    public static LoadingCache<String, Object> exists;
    public static boolean cacheLoaded = false;

    public static String getGalleryId(String url) {
        Pattern pattern = Pattern.compile("/g/(\\d+)/");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public Ehentai getEh(String id) {
        return (Ehentai) getExists( "ehentai" + ":" + id);
    }

    private synchronized Object getExists(String cid) {
        if (exists != null) {
            log.debug("当前缓存中数据条数：{}", exists.size());
            if (!cacheLoaded) {
                new Thread(() -> {
                    try {
                        log.debug("开始加载全量缓存");
                        List<String> list = new ArrayList<>();
                        list.add("ehentai:111");
                        exists.getAll(list);
                        cacheLoaded = true;
                    } catch (Exception ignored) {
                    }
                }).start();
            }
            Object exist = exists.getIfPresent(cid);
            if (exist == null) {
                try {
                    return exists.get(cid);
                } catch (Exception e) {
                    return null;
                }
            } else {
                return exist;
            }
        }
        exists = CacheBuilder.newBuilder().maximumSize(200000).expireAfterWrite(2000, TimeUnit.SECONDS).build(new CacheLoader<String, Object>() {
            final CrudTools tools = SpringContext.getBeanWithGenerics(CrudTools.class);
            @Override
            //key格式为表名:主键
            public Object load(@NotNull String key) {
                String[] split = key.split(":");
                String tableName = split[0];
                String entityName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tableName);
                String queryKey = split[1];
                Class<?> entity = CommonIOUtils.getEntityExists(entityName);
                List<?> objects = tools.commonApiQueryBySql(String.format("select * from %2$s where id = '%1$s'", queryKey, tableName), entity);
                if (objects.size() != 0) {
                    return objects.get(0);
                }
                return null;
            }

            @Override
            public Map<String, Object> loadAll(Iterable<? extends String> keys) {
                List<Ehentai> ehentaiList = tools.commonApiQueryBySql("select * from ehentai", Ehentai.class);
                return ehentaiList.stream().collect(Collectors.toMap(ehentai -> "ehentai:" + ehentai.getId(), ehentai -> ehentai));
            }
        });
        return getExists(cid);
    }
}
