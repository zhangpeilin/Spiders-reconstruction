package cn.zpl.spider.on.bika.utils;


import cn.zpl.common.bean.Bika;
import cn.zpl.common.bean.BikaDownloadFailed;
import cn.zpl.common.bean.BikaList;
import cn.zpl.common.bean.Token;
import cn.zpl.config.CommonParams;
import cn.zpl.config.SpringContext;
import cn.zpl.pojo.Data;
import cn.zpl.spider.on.bika.common.BikaParams;
import cn.zpl.spider.on.bika.thread.BikaComicThread;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.CrudTools;
import cn.zpl.util.DownloadTools;
import cn.zpl.util.GetSignature;
import cn.zpl.util.ZipUtils;
import com.alibaba.fastjson.JSON;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@EnableConfigurationProperties(BikaParams.class)
@Component
public class BikaUtils {

    //    public static Map<String, Bika> exists = new ConcurrentHashMap<>();
    public static LoadingCache<String, Bika> exists;

    public static LoadingCache<String, BikaList> bika_list_exists;
    private static String currentToken = "";
    public static String defaultSavePath = "e:\\bika";
    public static ThreadLocal<List<File>> result = new ThreadLocal<>();

    @Resource
    BikaParams bikaParams;
    @Resource
    CrudTools tools;

    public static final Map<String, AtomicInteger> progress = new HashMap<>();

    //    @Async("BikaAsync")
    public void search(String key, boolean download) {
        try {
            String url = "comics/search?page=1&q=" + URLEncoder.encode(key, "utf-8");
            JsonObject partJson = getJsonByUrl(url);
            JsonElement comics = CommonIOUtils.getFromJson2(partJson, "data-comics-docs");

            DownloadTools tool = DownloadTools.getInstance(2);
            tool.setName("漫画");
            tool.setSleepTimes(10000);
            for (JsonElement detail : comics.getAsJsonArray()) {
                if (download) {
                    tool.ThreadExecutorAdd(new BikaComicThread(detail.getAsJsonObject().get("_id").getAsString(), true));
                } else {
                    System.out.printf("%1$s:%2$s%n", CommonIOUtils.getFromJson2Str(detail, "title"), CommonIOUtils.getFromJson2Str(detail, "_id"));
                }
            }
            tool.shutdown();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void H24() {

        //H24 D7 D30
        String url = "comics/leaderboard?tt=H24&ct=VC";
        JsonObject partJson = getJsonByUrl(url);
        JsonElement comics = CommonIOUtils.getFromJson2(partJson, "data-comics");

        DownloadTools tool = DownloadTools.getInstance(10);
        tool.setName("漫画");
        tool.setSleepTimes(10000);
        List<String> stringList = new ArrayList<>();
        for (JsonElement detail : comics.getAsJsonArray()) {
            tool.ThreadExecutorAdd(new BikaComicThread(detail.getAsJsonObject().get("_id").getAsString(), true));
            stringList.add(CommonIOUtils.getFromJson2Str(detail, "_id"));
        }
        tool.shutdown();
//        stringList.forEach(s -> {
//            File file = new File(getExists(s).getLocalPath());
//            if (file.exists()) {
//                try {
//                    FileUtils.copyDirectoryToDirectory(file, new File("e:\\bika24H"));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }


    public void downloadById(String id) {

        //H24 D7 D30
        DownloadTools tool = DownloadTools.getInstance(10);
        tool.setName("漫画");
        tool.setSleepTimes(10000);
        tool.ThreadExecutorAdd(new BikaComicThread(id, true));
        tool.shutdown();
    }

    public void showH24() {

        //H24 D7 D30
        String url = "comics/leaderboard?tt=H24&ct=VC";
        JsonObject partJson = getJsonByUrl(url);
        JsonElement comics = CommonIOUtils.getFromJson2(partJson, "data-comics");

        DownloadTools tool = DownloadTools.getInstance(10);
        tool.setName("漫画");
        tool.setSleepTimes(10000);
        List<String> stringList = new ArrayList<>();
        for (JsonElement detail : comics.getAsJsonArray()) {
            System.out.printf("%1$s:%2$s%n", CommonIOUtils.getFromJson2Str(detail, "title"), CommonIOUtils.getFromJson2Str(detail, "_id"));
        }
    }

    public void favourite() {

        int i = 9;
        int maxPage = 99;
        while (!(i > maxPage)) {
            String url = "users/favourite?page=" + i;
            JsonObject partJson = getJsonByUrl(url);
            JsonElement comics = CommonIOUtils.getFromJson2(partJson, "data-comics-docs");

            maxPage = CommonIOUtils.getFromJson2Integer(partJson, "data-comics-pages");

            DownloadTools tool = DownloadTools.getInstance(5);
            tool.setName("漫画");
            tool.setSleepTimes(10000);
            for (JsonElement detail : comics.getAsJsonArray()) {
                tool.ThreadExecutorAdd(new BikaComicThread(detail.getAsJsonObject().get("_id").getAsString(), true));
            }
            tool.shutdown();
            i--;
//            i++;
        }
    }

    public LoadingCache<String, Bika> loadCache() {
        return exists;
    }

    public void invalidCache(String comicId) {
        exists.invalidate(comicId);
    }

    public synchronized Bika getExists(String comicId) {
        if (!bikaParams.isWriteDB()) {
            return null;
        }
        if (exists != null) {
            try {
                List<String> list = new ArrayList<>();
                list.add("1111");
                exists.getAll(list);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
            Bika exist = exists.getIfPresent(comicId);
            if (exist == null) {
                try {
                    return exists.get(comicId);
                } catch (Exception e) {
//                        log.error("加载失败", e);
                    return null;
                }
            } else {
                return exist;
            }
        }
        exists = CacheBuilder.newBuilder().maximumSize(200000).expireAfterWrite(2000, TimeUnit.SECONDS).build(new CacheLoader<String, Bika>() {
            @Override
            public Bika load(@NotNull String key) {
                List<Bika> bikas = tools.commonApiQueryBySql(String.format("select * from bika where id = '%1$s'", key), Bika.class);
                if (bikas.size() != 0) {
                    return bikas.get(0);
                }
                return null;
            }

            @Override
            public Map<String, Bika> loadAll(Iterable<? extends String> keys) throws Exception {
                List<Bika> bikas = tools.commonApiQueryBySql("select * from bika", Bika.class);
                return bikas.stream().collect(Collectors.toMap(Bika::getId, bika -> bika));
            }
        });
        return getExists(comicId);
    }


    public synchronized BikaList getFromBikaList(String comicId) {
        if (!bikaParams.isWriteDB()) {
            return null;
        }
        if (bika_list_exists != null) {
            try {
                if (bika_list_exists.size() == 0) {
                    List<String> list = new ArrayList<>();
                    list.add("5fd8d1aecc2a3c407ff3da5e");
                    bika_list_exists.getAll(list);
                }
                //加载全部缓存
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
            return bika_list_exists.getIfPresent(comicId);
        }
        bika_list_exists = CacheBuilder.newBuilder().maximumSize(200000).expireAfterWrite(2000, TimeUnit.HOURS).build(new CacheLoader<String, BikaList>() {
            @Override
            public BikaList load(@NotNull String key) {
                List<BikaList> bikas = tools.commonApiQueryBySql(String.format("select * from bika_list where id = '%1$s'", key), BikaList.class);
                if (bikas.size() != 0) {
                    return bikas.get(0);
                }
                return null;
            }

            @Override
            public Map<String, BikaList> loadAll(Iterable<? extends String> keys) throws Exception {
                List<BikaList> bikas = tools.commonApiQueryBySql("select * from bika_list", BikaList.class);
                return bikas.stream().collect(Collectors.toMap(BikaList::getId, bika -> bika));
            }
        });
        return getFromBikaList(comicId);
    }

    public boolean isNeedUpdate(String comicid) {
        if (!bikaParams.isWriteDB()) {
            return true;
        }
        Bika already = getExists(comicid);
        if (already != null) {
            if (already.getIsDeleted() != null && already.getIsDeleted() == 1) {
                return false;
            }
            if (already.getIsComplete() != null && already.getIsComplete() == 1) {
                return false;
            }
            if (already.getDownloadedAt() != null && !"".equals(already.getDownloadedAt())) {
                return new BigDecimal(System.currentTimeMillis()).subtract(new BigDecimal(already.getDownloadedAt())).compareTo(new BigDecimal(604800000)) > 0;
            }
        }
        return true;
    }

    private void Login() {
        try {

            log.debug("登录失效，开始登录……");
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("email", bikaParams.getEmail());
            jsonObject.addProperty("password", bikaParams.getPassword());
            JsonObject json = postUrl("auth/sign-in", jsonObject.toString());
            if (json == null) {
                CommonIOUtils.waitSeconds(5);
                Login();
            }
            String token = CommonIOUtils.getFromJson2(json, "data").getAsJsonObject().get("token").getAsString();
            Token dto = new Token();
            dto.setId(String.valueOf(System.currentTimeMillis()));
            dto.setToken(token);
            tools.commonApiSave(dto);
            currentToken = token;
        } catch (Exception e) {
            Login();
        }
    }

    private String getCurrentToken() {
        if (currentToken != null && !"".equals(currentToken)) {
            return currentToken;
        }
        //id是字符串，需要转换成数字后排序
        List<Token> list0 = tools.commonApiQueryBySql("sql:SELECT * from token ORDER BY CAST(id as UNSIGNED) desc LIMIT 0,1", Token.class);
        if (list0.size() == 1) {
            currentToken = list0.get(0).getToken();
        }
        return currentToken;
    }

    public JsonObject post(String path, String params) {
        try {
            return postUrl(path, params);
        } catch (IOException e) {
            return post(path, params);
        }
    }

    public JsonObject postUrl(String path, String params) throws IOException {
        JsonObject json = new JsonObject();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpHost proxy = new HttpHost(CommonParams.hostName, CommonParams.proxyPort);
        RequestConfig requestConfig = RequestConfig.custom().setProxy(proxy).setSocketTimeout(10000)
                .setConnectTimeout(10000).setConnectionRequestTimeout(3000).build();
        HttpPost post = new HttpPost("https://picaapi.picacomic.com/" + path);
        post.setConfig(requestConfig);
        if (path.contains("sign-in")) {
            addHeaderForLogin(post, path);
        } else {
            addHeader2(post, path);
        }
        StringEntity postingString = new StringEntity(params, "utf-8");
        post.setEntity(postingString);
        HttpResponse response = httpClient.execute(post);
        String content = EntityUtils.toString(response.getEntity());
        log.debug(content);
        json = Objects.requireNonNull(CommonIOUtils.paraseJsonFromStr(content)).getAsJsonObject();
        if ("401".equals(json.get("code").getAsString()) && "unauthorized".equals(json.get("message").getAsString())) {
            Login();
            return postUrl(path, params);
        }
        return json;
    }

    private void addHeader2(HttpRequestBase http, String path) {
        String time = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String method;
        if (http instanceof HttpGet) {
            method = "get";
        } else {
            method = "post";
        }
        String substring = time.substring(0, time.length() - 3);
        String signature = GetSignature.generateSignature(path, substring, uuid, method);
        String authorization = getCurrentToken() + "\n";
        String headers = "authorization: " + authorization +
                "api-key: C69BAF41DA5ABD1FFEDC6D2FEA56B\n" +
                "accept: application/vnd.picacomic.com.v1+json\n" +
                "app-channel: 1\n" +
                "time: " + substring + "\n" +
                "nonce: " + uuid + "\n" +
                "signature: " + signature + "\n" +
                "app-version: 2.2.1.3.3.4\n" +
                "app-uuid: defaultUuid\n" +
                "image-quality: original\n" +
                "app-platform: android\n" +
                "app-build-version: 44\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "Host: picaapi.picacomic.com\n" +
                "Connection: Keep-Alive\n" +
                "Accept-Encoding: gzip\n" +
                "User-Agent: okhttp/3.8.1\n";

        String[] cookieArray = headers.trim().split("\n");
        for (String pair :
                cookieArray) {
            http.addHeader(pair.split(":")[0].trim(), pair.split(":")[1].trim());
        }
    }

    private static void addHeaderForLogin(HttpRequestBase http, String path) {
        String time = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String method;
        if (http instanceof HttpGet) {
            method = "get";
        } else {
            method = "post";
        }
        String signature = GetSignature.generateSignature(path, time.substring(0, time.length() - 3), uuid, method);
        String headers = "api-key: C69BAF41DA5ABD1FFEDC6D2FEA56B\n" +
                "accept: application/vnd.picacomic.com.v1+json\n" +
                "app-channel: 1\n" +
                "time: " + time.substring(0, time.length() - 3) + "\n" +
                "nonce: " + uuid + "\n" +
                "signature: " + signature + "\n" +
                "app-version: 2.2.1.2.3.3\n" +
                "app-uuid: defaultUuid\n" +
                "image-quality: original\n" +
                "app-platform: android\n" +
                "app-build-version: 44\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "Host: picaapi.picacomic.com\n" +
                "Connection: Keep-Alive\n" +
                "Accept-Encoding: gzip\n" +
                "User-Agent: okhttp/3.8.1\n";

        String[] cookieArray = headers.trim().split("\n");
        for (String pair :
                cookieArray) {
            http.addHeader(pair.split(":")[0].trim(), pair.split(":")[1].trim());
        }
    }

    public JsonObject getJsonByUrl(String path) {
        Data data = new Data();
        data.setProxy(true);
        data.setType("json");
        data.setUrl(path);
        build(data);
        data.setWaitSeconds(1000);
        CommonIOUtils.withTimer(data);
        if (data.getStatusCode() == 401) {
            Login();
            return getJsonByUrl(path);
        }
        if (data.getStatusCode() == 400) {
            throw new RuntimeException("解析错误，错误代码：" + data.getStatusCode() + "，错误内容：" + data.getResult());
        }
        JsonObject json = Objects.requireNonNull(CommonIOUtils.paraseJsonFromStr(data.getResult())).getAsJsonObject();
        if (json != null && "200".equals(json.get("code").getAsString())) {
            return json;
        }
        if (json == null) {
            throw new RuntimeException("获取json失败");
        }
        if ("401".equals(json.get("code").getAsString()) && "unauthorized".equals(json.get("message").getAsString())) {
            Login();
            return getJsonByUrl(path);
        }
        if (!"200".equals(json.get("code").getAsString())) {
            throw new RuntimeException("服务器响应失败，错误代码：" + json.get("code").getAsString() + "，错误内容：" + json.get("error") + json.get("message"));
        }
        return json;
    }


    public void dosave(String comicid, JsonObject json, boolean isNeedDownload, String localPath) {
        //判断是否需要下载，如果数据存在：downloaded_at时间在一周之内，则认为不需要继续下载返回false，否则更新下载日期并且返回true
        if (json == null) {
            String getComicsInfo = "comics/" + comicid;
            json = getJsonByUrl(getComicsInfo);
        }
        Bika bika = getBika(json, localPath);
        BikaList list = getBikaList(json, localPath);
        list.setLocalPath(localPath);
        BikaDownloadFailed failed = new BikaDownloadFailed();
        failed.setId(comicid);
        List<Bika> result = tools.commonApiQuery(null, null, Bika.class);
        if (!CollectionUtils.isEmpty(result)) {
            CrudTools.commonApiDelete(null, Bika.class);
        }
        try {
            if (isNeedDownload) {
                if (!tools.commonApiSave(bika).isSuccess()) {
                    log.error("保存失败" + bika);
                }
            }
            tools.commonApiSave(list);
        } catch (Exception e) {
//            base64ErrorCols(((GenericJDBCException) e.getCause()).getSQLException().getMessage(), bika);
//            base64ErrorCols(((GenericJDBCException) e.getCause()).getSQLException().getMessage(), list);
            if (isNeedDownload) {
//                DBManager.ForceSave(bika);
                if (!tools.commonApiSave(bika).isSuccess()) {
                    log.error("保存失败-->{}", bika);
                }
            }
            tools.commonApiSave(list);
        }
    }

    public static Bika getBika(JsonElement json, String localPath) {
        Bika bika = new Bika();
        try {
            bika.setId(CommonIOUtils.getFromJson2(json, "data-comic-_id").getAsString());
            bika.setTitle(CommonIOUtils.filterFileName(CommonIOUtils.getFromJson2Str(json, "data-comic-title")));
            bika.setDescription(CommonIOUtils.getFromJson2Str(json, "data-comic-description"));
            bika.setAuthor(CommonIOUtils.getFromJson2Str(json, "data-comic-author"));
            bika.setCategories(CommonIOUtils.getFromJson2(json, "data-comic-categories").toString());
            bika.setTags(CommonIOUtils.getFromJson2(json, "data-comic-tags").toString());
            bika.setPagesCount(CommonIOUtils.getFromJson2Integer(json, "data-comic-pagesCount"));
            bika.setEpsCount(CommonIOUtils.getFromJson2Integer(json, "data-comic-epsCount"));
            bika.setFinished(CommonIOUtils.getFromJson2Boolean(json, "data-comic-finished") ? 1 : 0);
            bika.setUpdatedAt(CommonIOUtils.getFromJson2Str(json, "data-comic-updated_at"));
            bika.setCreatedAt(CommonIOUtils.getFromJson2Str(json, "data-comic-created_at"));
            bika.setViewsCount(CommonIOUtils.getFromJson2Integer(json, "data-comic-viewsCount"));
            bika.setLikesCount(CommonIOUtils.getFromJson2Integer(json, "data-comic-likesCount"));
            bika.setIsFavourite(CommonIOUtils.getFromJson2Boolean(json, "data-comic-isFavourite") ? 1 : 0);
            bika.setIsLiked(CommonIOUtils.getFromJson2Boolean(json, "data-comic-isLiked") ? 1 : 0);
            bika.setCommentsCount(CommonIOUtils.getFromJson2Integer(json, "data-comic-commentsCount"));
            bika.setDownloadedAt(String.valueOf(System.currentTimeMillis()));
            bika.setIsComplete(0);
            bika.setIsTranslated(1);
            bika.setIsDeleted(0);
            bika.setLocalPath(localPath);
//            if (new File(bika.getLocalPath()).exists()) {
//                bika.setRealPagesCount(Objects.requireNonNull(new File(bika.getLocalPath()).listFiles(File::isDirectory)).length);
//            }
            bika.setIsDeleted(0);
            return bika;
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            log.error(bika.getId() + "存在错误，请核对");
        }
        return bika;
    }

    private static BikaList getBikaList(JsonObject json, String localPath) {
        BikaList bika = new BikaList();
        try {
            bika.setId(CommonIOUtils.getFromJson2(json, "data-comic-_id").getAsString());
            bika.setTitle(CommonIOUtils.filterFileName(CommonIOUtils.getFromJson2Str(json, "data-comic-title")));
            bika.setDescription(CommonIOUtils.getFromJson2Str(json, "data-comic-description"));
            bika.setAuthor(CommonIOUtils.getFromJson2Str(json, "data-comic-author"));
            bika.setCategories(CommonIOUtils.getFromJson2(json, "data-comic-categories").toString());
            bika.setTags(CommonIOUtils.getFromJson2(json, "data-comic-tags").toString());
            bika.setPagesCount(CommonIOUtils.getFromJson2Integer(json, "data-comic-pagesCount"));
            bika.setEpsCount(CommonIOUtils.getFromJson2Integer(json, "data-comic-epsCount"));
            bika.setFinished(CommonIOUtils.getFromJson2Boolean(json, "data-comic-finished") ? 1 : 0);
            bika.setUpdatedAt(CommonIOUtils.getFromJson2Str(json, "data-comic-updated_at"));
            bika.setCreatedAt(CommonIOUtils.getFromJson2Str(json, "data-comic-created_at"));
            bika.setViewsCount(CommonIOUtils.getFromJson2Integer(json, "data-comic-viewsCount"));
            bika.setLikesCount(CommonIOUtils.getFromJson2Integer(json, "data-comic-likesCount"));
            bika.setIsFavourite(CommonIOUtils.getFromJson2Boolean(json, "data-comic-isFavourite") ? 1 : 0);
            bika.setIsLiked(CommonIOUtils.getFromJson2Boolean(json, "data-comic-isLiked") ? 1 : 0);
            bika.setCommentsCount(CommonIOUtils.getFromJson2Integer(json, "data-comic-commentsCount"));
            bika.setDownloadedAt(String.valueOf(System.currentTimeMillis()));
            bika.setIsDeleted(0);
            bika.setLocalPath(localPath);
//            if (new File(bika.getLocalPath()).exists()) {
//                bika.setRealPagesCount(Objects.requireNonNull(new File(bika.getLocalPath()).listFiles(File::isDirectory)).length);
//            }
            bika.setIsDeleted(0);
            return bika;
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(bika.getId() + "存在错误，请核对");
        }
        return bika;
    }

    private static void base64ErrorCols(String erroinfo, Object bika) {
        String regex = "for column\\s'(\\w+)'";
        String clearn = "(for column\\s)*(')*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(erroinfo);
        Base64.Encoder encoder = Base64.getEncoder();
        while (matcher.find()) {
            String columnName = erroinfo.substring(matcher.start(), matcher.end()).replaceAll(clearn, "");
            try {
                BeanUtils.setProperty(bika, columnName, encoder.encodeToString(BeanUtils.getProperty(bika, columnName).getBytes()));
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }

        }
    }

    public boolean needSkip(JsonObject info) {
        String categories = CommonIOUtils.getFromJson2(info, "data-comic-categories").toString();
        if (categories.contains("耽美") && !(categories.contains("偽娘") || categories.contains("性轉換"))) {
            log.debug("跳过BL本");
            Bika bika = getBika(info, "");
            bika.setIsDeleted(1);
//            DBManager.update(bika);
            if (bikaParams.isWriteDB() && !tools.commonApiSave(bika).isSuccess()) {
                log.error("保存失败：" + bika);
            }
            return true;
        }
        return false;
    }

    private void build(Data result) {

        String time = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String signature = GetSignature.generateSignature(result.getUrl(), time.substring(0, time.length() - 3), uuid, "get");
        String authorization = getCurrentToken() + "\n";
        String headers = "authorization: " + authorization +
                "api-key: C69BAF41DA5ABD1FFEDC6D2FEA56B\n" +
                "accept: application/vnd.picacomic.com.v1+json\n" +
                "app-channel: 1\n" +
                "time: " + time.substring(0, time.length() - 3) + "\n" +
                "nonce: " + uuid + "\n" +
                "signature: " + signature + "\n" +
                "app-version: 2.2.1.3.3.4\n" +
                "app-uuid: defaultUuid\n" +
                "image-quality: original\n" +
                "app-platform: android\n" +
                "app-build-version: 44\n" +
                "Host: picaapi.picacomic.com\n" +
                "Connection: Keep-Alive\n" +
                "Accept-Encoding: gzip\n" +
                "User-Agent: okhttp/3.8.1\n";

        result.setUrl("https://picaapi.picacomic.com/" + result.getUrl());
        result.setHeader(headers);
    }

    public static String getFileId(File file) {
        String id = null;
        Matcher matcher = Pattern.compile("^\\(\\w+\\)").matcher(file.getName());
        if (matcher.find()) {
            id = Pattern.compile("[()]").matcher(file.getName().substring(matcher.start(), matcher.end())).replaceAll("");
        }
        return id;
    }

    public static String getFolder(Bika bika) {
        return "(" + bika.getId() + ")" + bika.getTitle();
    }

    public static String getFolder(String id, String title) {
        return "(" + id + ")" + title;
    }

    public static String getLocalPath(String comicId, String title) {
        BikaUtils bikaUtils = SpringContext.getBeanWithGenerics(BikaUtils.class);
        Bika exist = bikaUtils.getExists(comicId);
        String chapterPath;
        if (exist != null && exist.getLocalPath() != null && !"".equals(exist.getLocalPath())) {
            return exist.getLocalPath();
        } else {
            chapterPath = BikaUtils.defaultSavePath + "\\(" + comicId + ")" + title;
            return chapterPath;
        }
    }

    /**
     * 获取当前目录下所有的文件夹
     *
     * @param path
     * @return
     */
    public static List<File> getFolders(String path, String regex) {
        //如果表达式为空，则返回所有文件夹，否则返回符合表达式格式的文件夹
        File base = new File(path);
        if (result.get() == null) {
            List<File> list = new ArrayList<>();
            result.set(list);
        }
        if (base.isDirectory()) {
            for (File file : Objects.requireNonNull(base.listFiles())) {
                if (regex == null || "".equals(regex)) {
                    result.get().add(file);
                } else if (Pattern.compile(regex).matcher(file.getName()).find()) {
                    result.get().add(file);
                }
                getFolders(file.getPath(), regex);
            }
        }
//        if (base.isDirectory()) {
//            for (File file : Objects.requireNonNull(base.listFiles(regex == null || "".equals(regex) ? FileFilterUtils.trueFileFilter() : (FilenameFilter) (dir, name) -> Pattern.compile(regex).matcher(name).find()))) {
//                result.get().add(file);
//                getFolders(file.getPath(), regex);
//            }
//        }
        return result.get();
    }

    public Path GetAvailablePath(long size, File file) {
        List<String> savePath = bikaParams.getSavePath();
        if (file != null && file.exists()) {
            Optional<ImmutableMap<String, Object>> priority = savePath.stream().map(path -> ImmutableMap.<String, Object>of("size", new File(path).getParentFile().getFreeSpace(), "path", path)).filter(hashMap -> ((long) Objects.requireNonNull(hashMap.get("size"))) > size && String.valueOf(Objects.requireNonNull(hashMap.get("path"))).toLowerCase().startsWith(file.getPath().toLowerCase().substring(0, 1))).findAny();
            if (priority.isPresent()) {
                ImmutableMap<String, Object> fitMap = priority.get();
                return Paths.get(String.valueOf(fitMap.get("path")));
            }
        }
        Optional<ImmutableMap<String, Object>> first = savePath.stream().map(path -> ImmutableMap.<String, Object>of("size", new File(path).getParentFile().getFreeSpace(), "path", path)).filter(hashMap -> ((long) Objects.requireNonNull(hashMap.get("size"))) > size).findFirst();
        if (first.isPresent()) {
            ImmutableMap<String, Object> fitMap = first.get();
            return Paths.get(String.valueOf(fitMap.get("path")));
        }
        return Paths.get("d:\\temp");
    }

    /**
     * m
     * 将文件移到存档点
     *
     * @param file
     * @param des
     */
    @SneakyThrows
    public Path moveFile(File file, Path des) {
        if (!file.toPath().equals(Paths.get(des.toString(), file.getName()))) {
            FileUtils.moveFileToDirectory(file, des.toFile(), true);
        }
        return Paths.get(des.toString(), file.getName());
    }


    public void test() {
        List<File> list = getFolders("G:\\Bika完结", "\\(\\w+\\)");
        System.out.println(list.size());
        for (File file : list) {
            System.out.println(file);
        }
    }

    @SneakyThrows
    @Async("BikaAsync")
    public void bus(String type, File file, BikaUtils bikaUtils, Function<File, List<String>> callback, BiFunction<File, Path, Path> after) {
        if (callback == null) {
            throw new RuntimeException("没有传入正确的处理函数");
        }
        CrudTools crudTools = SpringContext.getBeanWithGenerics(CrudTools.class);
        String fileId = CommonIOUtils.getFileId(file);
        if (fileId == null) {
            return;
        }
        Bika exist = bikaUtils.getExists(fileId);
        if (exist != null) {
            File localFile = new File(exist.getLocalPath());
            if (!localFile.exists()) {
                //如果数据库记录的位置没有文件，则以该文件为准更新
                exist.setEpsCount(callback.apply(file).size());
                Path des = after.apply(file, bikaUtils.GetAvailablePath(file.length(), file));
                exist.setLocalPath(des.toString());
                crudTools.AsyncApiSave(exist);
            } else {
                //如果数据库中记录位置有，判断记录位置是否与该文件一致，如果一致则直接返回，否则进入比较逻辑
                if (exist.getLocalPath().equalsIgnoreCase(file.getPath())) {
                    return;
                }
                Integer existsEpsCount = exist.getEpsCount();
                List<String> chaptersList = callback.apply(file);
                System.out.println(fileId);
                //如果文件中章节数大于数据库中记录，则文件为最新记录，删除数据库中记录位置处的文件
                if (chaptersList.size() > existsEpsCount) {
                    FileUtils.delete(new File(exist.getLocalPath()));
                    Path des = after.apply(file, bikaUtils.GetAvailablePath(file.length(), file));
                    exist.setLocalPath(des.toString());
                    crudTools.AsyncApiSave(exist);
                } else {
                    int fileCount = ZipUtils.getFileCount(file);
                    int existFileCount = ZipUtils.getFileCount(new File(exist.getLocalPath()));
                    if (fileCount <= existFileCount) {
                        FileUtils.delete(file);
                    } else if (chaptersList.size() == existsEpsCount) {
                        //如果章节数相同且扫描文件中大于数据库记录文件中的图片数，则以文件为最新记录
                        FileUtils.delete(new File(exist.getLocalPath()));
                        Path des = after.apply(file, bikaUtils.GetAvailablePath(file.length(), file));
                        exist.setLocalPath(des.toString());
                        crudTools.AsyncApiSave(exist);
                    }
                }
            }
        } else {
            //如果数据库中没有，则查询bika_list表，如果有的话将记录复制到bika表中，并更新保存记录
            BikaList bikaList = bikaUtils.getFromBikaList(fileId);
            if (bikaList == null) {
                log.warn("该记录无法在bika_list中找到");
                return;
            }
            Bika bika = JSON.parseObject(JSON.toJSONString(bikaList), Bika.class);
            bika.setEpsCount(callback.apply(file).size());
            Path des = after.apply(file, bikaUtils.GetAvailablePath(file.length(), file));
            bika.setLocalPath(des.toString());
            crudTools.commonApiSave(bika);
        }
//            if (exist != null) {
//                Integer existsEpsCount = exist.getEpsCount();
//                List<String> chaptersList = callback.apply(file);
//                //如果文件中章节数大于数据库中记录，则文件为最新记录，解压并且打成tar放入存档；
//                if (chaptersList.size() > existsEpsCount) {
//                    after.apply(file, bikaUtils.GetAvaliablePath(file.getUsableSpace()));
//                }
//            } else {
//                //如果数据库不存在记录，则文件为最新记录
//            }
    }
}
