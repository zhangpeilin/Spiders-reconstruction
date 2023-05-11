package cn.zpl.spider.on.bilibili.common;

import cn.zpl.common.bean.ExceptionList;
import cn.zpl.common.bean.VideoInfo;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.CrudTools;
import cn.zpl.util.UrlContainer;
import com.google.common.base.CaseFormat;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author zpl
 */
@Component
@Slf4j
@EnableConfigurationProperties(BilibiliProperties.class)
public class BilibiliCommonUtils {

    @Resource
    CrudTools tools;

//    static ThreadLocal<BilibiliConfigParams> configParamsThreadLocal = new ThreadLocal<>();
    public static LoadingCache<String, Object> exists;
    public static boolean cacheLoaded = false;

    public VideoInfo getVideoInfo(String cid) {
        return (VideoInfo) getExists( "video_info" + ":" + cid);
    }

    public ExceptionList getExceptionList(String cid) {
        return (ExceptionList) getExists( "exception_list" + ":" + cid);
    }
    private synchronized Object getExists(String cid) {
        if (exists != null) {
            log.debug("当前缓存中数据条数：{}", exists.size());
            if (!cacheLoaded) {
                new Thread(() -> {
                    try {
                        log.debug("开始加载全量缓存");
                        cacheLoaded = true;
                        List<String> list = new ArrayList<>();
                        list.add("video_info:900735459");
                        exists.getAll(list);
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
            @Override
            //key格式为表名:主键
            public Object load(@NotNull String key) {
                String[] split = key.split(":");
                String tableName = split[0];
                String entityName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tableName);
                String queryKey = split[1];
                Class<?> entity = CommonIOUtils.getEntityExists(entityName);
                List<?> objects = tools.commonApiQueryBySql(String.format("select * from %2$s where video_id = '%1$s'", queryKey, tableName), entity);
                if (objects.size() != 0) {
                    return objects.get(0);
                }
                return null;
            }

            @Override
            public Map<String, Object> loadAll(Iterable<? extends String> keys) {
                List<VideoInfo> videoInfos = tools.commonApiQueryBySql("select * from video_info", VideoInfo.class);
                List<ExceptionList> exceptionLists = tools.commonApiQueryBySql("select * from exception_list", ExceptionList.class);
                Map<String, Object> videoInfoMap = videoInfos.stream().collect(Collectors.toMap(videoInfo -> "video_info:" + videoInfo.getVideoId(), videoInfo -> videoInfo));
                Map<String, Object> exceptionListMap = exceptionLists.stream().collect(Collectors.toMap(exceptionList -> "exception_list:" + exceptionList.getVideoId(), exceptionList -> exceptionList));
                videoInfoMap.putAll(exceptionListMap);
                return videoInfoMap;
            }
        });
        return getExists(cid);
    }

//    public static BilibiliConfigParams getConfigParams() {
//        if (configParamsThreadLocal.get() == null) {
//            configParamsThreadLocal.set(SpringContext.getBeanWithGenerics(BilibiliConfigParams.class));
//        }
//        return configParamsThreadLocal.get();
//    }

    public static String getUserInfo(String uid) throws JsonIOException, JsonSyntaxException {

        String url = "https://api.bilibili.com/x/space/acc/info?mid=" + uid + "&jsonp=jsonp";
        UrlContainer container = new UrlContainer(url);
        JsonElement json = CommonIOUtils.paraseJsonFromURL(container);
        return CommonIOUtils.getFromJson2Str(json, "data-name");
    }

    @Nullable
    public static JsonElement decryptIndexFile(byte[] result, int comic_id, int chapter_id) {

        result = Arrays.copyOfRange(result, 9, result.length);
        byte[] key = new byte[8];
        key[0] = (byte) chapter_id;
        key[1] = (byte) (chapter_id >> 8);
        key[2] = (byte) (chapter_id >> 16);
        key[3] = (byte) (chapter_id >> 24);
        key[4] = (byte) comic_id;
        key[5] = (byte) (comic_id >> 8);
        key[6] = (byte) (comic_id >> 16);
        key[7] = (byte) (comic_id >> 24);
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (result[i] ^ key[i % 8]);
        }
        try {
            ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(result));
            ZipEntry zipEntry;
            String line;
            JsonElement jsonElement = null;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(zipInputStream));
                while ((line = reader.readLine()) != null) {
                    jsonElement = CommonIOUtils.paraseJsonFromStr(line);
                }
            }
            zipInputStream.close();
            return jsonElement;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

//    public static String postUrl(String path, String params, String headers) {
//        if (headers == null)
//            headers = BilibiliStaticParams.commonHeaders;
//        return URLConnectionTool.postUrl(path, params, headers);
//    }

    public String test() {
        return BilibiliCommonUtils.getUserInfo("");
    }
}
