package cn.zpl.spider.on.bilibili.common;

import cn.zpl.common.bean.Bika;
import cn.zpl.common.bean.VideoInfo;
import cn.zpl.config.SpringContext;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.CrudTools;
import cn.zpl.util.URLConnectionTool;
import cn.zpl.util.UrlContainer;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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

@Component
public class BilibiliCommonUtils {

    @Resource
    CrudTools tools;

    static ThreadLocal<BilibiliConfigParams> configParamsThreadLocal = new ThreadLocal<>();
    public static LoadingCache<String, VideoInfo> exists;
    public synchronized VideoInfo getExists(String cid) {
        if (exists != null) {
            try {
                List<String> list = new ArrayList<>();
                list.add("900735459");
                exists.getAll(list);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
            VideoInfo exist = exists.getIfPresent(cid);
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
        exists = CacheBuilder.newBuilder().maximumSize(200000).expireAfterWrite(2000, TimeUnit.SECONDS).build(new CacheLoader<String, VideoInfo>() {
            @Override
            public VideoInfo load(@NotNull String key) {
                List<VideoInfo> bikas = tools.commonApiQueryBySql(String.format("select * from video_info where video_id = '%1$s'", key), VideoInfo.class);
                if (bikas.size() != 0) {
                    return bikas.get(0);
                }
                return null;
            }

            @Override
            public Map<String, VideoInfo> loadAll(Iterable<? extends String> keys) throws Exception {
                List<VideoInfo> bikas = tools.commonApiQueryBySql("select * from video_info", VideoInfo.class);
                return bikas.stream().collect(Collectors.toMap(VideoInfo::getVideoId, video_info -> video_info));
            }
        });
        return getExists(cid);
    }

    public static BilibiliConfigParams getConfigParams() {
        if (configParamsThreadLocal.get() == null) {
            configParamsThreadLocal.set(SpringContext.getBeanWithGenerics(BilibiliConfigParams.class));
        }
        return configParamsThreadLocal.get();
    }

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
