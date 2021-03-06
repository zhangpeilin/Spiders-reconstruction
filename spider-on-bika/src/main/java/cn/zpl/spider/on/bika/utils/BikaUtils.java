package cn.zpl.spider.on.bika.utils;


import cn.zpl.common.bean.Bika;
import cn.zpl.common.bean.BikaDownloadFailed;
import cn.zpl.config.CommonParams;
import cn.zpl.pojo.Data;
import cn.zpl.spider.on.bika.common.BikaParams;
import cn.zpl.util.CommonIOUtils;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.istack.internal.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class BikaUtils {

    public static Map<String, Bika> exists = new ConcurrentHashMap<>();
    private static String currentToken = "";
    public static String defaultSavePath = "e:\\bika";
    public static ThreadLocal<List<File>> result = new ThreadLocal<>();

    public static final Map<String, AtomicInteger> progress = new HashMap<>();

    public static Bika getExists(String comicid) {
        if (!BikaParams.writeDB){
            return null;
        }
        synchronized (BikaUtils.class) {
            if (exists.size() == 0) {
                List<Bika> list = DBManager.getInstance().findDtoBySql("select * from bika", Bika.class);
                list.forEach(bika -> exists.put(bika.getId(), bika));
            }
            return exists.get(comicid);
        }
    }

    public static boolean isNeedUpdate(String comicid) {
        if (!BikaParams.writeDB){
            return true;
        }
        synchronized (BikaUtils.class) {
            if (exists.size() == 0) {
                List<Bika> list = DBManager.getInstance().findDtoBySql("select * from bika", Bika.class);
                list.forEach(bika -> exists.put(bika.getId(), bika));
            }
        }
        Bika already = exists.get(comicid);
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

    private static void Login() {
        try {

            log.debug("?????????????????????????????????");
            JsonObject json = postUrl("auth/sign-in", "{\"email\":\"huashenhweijian\",\"password\":\"z3462638\"}");
            if (json == null) {
                CommonIOUtils.waitSeconds(5);
                Login();
            }
            String token = CommonIOUtils.getFromJson2(json, "data").getAsJsonObject().get("token").getAsString();
            Token dto = new Token();
            dto.setId(String.valueOf(System.currentTimeMillis()));
            dto.setToken(token);
            DBManager.ForceSave(dto);
            currentToken = token;
        } catch (Exception e) {
            Login();
        }
    }

    private static String getCurrentToken() {
        if (currentToken != null && !"".equals(currentToken)) {
            return currentToken;
        }
        //id?????????????????????????????????????????????
        List<Token> list0 = DBManager.getInstance().findDtoBySql("SELECT * from token ORDER BY CAST(id as UNSIGNED) desc LIMIT 0,1", Token.class);
        if (list0.size() == 1) {
            currentToken = list0.get(0).getToken();
        }
        return currentToken;
    }

    public static JsonObject post(String path, String params) {
        try {
            return postUrl(path, params);
        } catch (IOException e) {
            return post(path, params);
        }
    }

    public static JsonObject postUrl(@NotNull String path, String params) throws IOException {
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

    private static void addHeader2(HttpRequestBase http, String path) {
        String time = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String method;
        if (http instanceof HttpGet) {
            method = "get";
        } else {
            method = "post";
        }
        String signature = GetSignature.generateSignature(path, time.substring(0, time.length() - 3), uuid, method);
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

    @NotNull
    public static JsonObject getJsonByUrl(String path) {
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
            throw new RuntimeException("??????????????????????????????" + data.getStatusCode() + "??????????????????" + data.getResult());
        }
        JsonObject json = Objects.requireNonNull(CommonIOUtils.paraseJsonFromStr(data.getResult())).getAsJsonObject();
        if (json != null && "200".equals(json.get("code").getAsString())) {
            return json;
        }
        if (json == null) {
            throw new RuntimeException("??????json??????");
        }
        if ("401".equals(json.get("code").getAsString()) && "unauthorized".equals(json.get("message").getAsString())) {
            Login();
            return getJsonByUrl(path);
        }
        if (!"200".equals(json.get("code").getAsString())) {
            throw new RuntimeException("???????????????????????????????????????" + json.get("code").getAsString() + "??????????????????" + json.get("error") + json.get("message"));
        }
        return json;
    }


    public static void dosave(String comicid, JsonObject json, boolean isNeedDownload, String localPath) {
        //????????????????????????????????????????????????downloaded_at????????????????????????????????????????????????????????????false???????????????????????????????????????true
        if (json == null) {
            String getComicsInfo = "comics/" + comicid;
            json = getJsonByUrl(getComicsInfo);
        }
        Bika bika = getBika(json, localPath);
        BikaList list = getBikaList(json, localPath);
        list.setLocalPath(localPath);
        BikaDownloadFailed failed = new BikaDownloadFailed();
        failed.setId(comicid);
        if (DBManager.getDTOById2(BikaDownloadFailed.class, comicid) != null) {
            DBManager.delete(failed);
        }
        try {
            if (isNeedDownload) {
//                DBManager.ForceSave(bika);
                if (!BikaUtils.saveBika(bika)) {
                    log.error("????????????" + bika);
                }
            }
            DBManager.ForceSave(list);
        } catch (Exception e) {
            base64ErrorCols(((GenericJDBCException) e.getCause()).getSQLException().getMessage(), bika);
            base64ErrorCols(((GenericJDBCException) e.getCause()).getSQLException().getMessage(), list);
            if (isNeedDownload) {
//                DBManager.ForceSave(bika);
                if (!BikaUtils.saveBika(bika)) {
                    log.error("????????????" + bika);
                }

            }
            DBManager.ForceSave(list);
        }
    }

    @NotNull
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
            bika.setTranslated(1);
            bika.setIsDeleted(0);
            bika.setLocalPath(localPath);
            if (new File(bika.getLocalPath()).exists()) {
                bika.setRealPagesCount(Objects.requireNonNull(new File(bika.getLocalPath()).listFiles(File::isDirectory)).length);
            }
            bika.setIsDeleted(0);
            return bika;
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            log.error(bika.getId() + "????????????????????????");
        }
        return bika;
    }

    @NotNull
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
            if (new File(bika.getLocalPath()).exists()) {
                bika.setRealPagesCount(Objects.requireNonNull(new File(bika.getLocalPath()).listFiles(File::isDirectory)).length);
            }
            bika.setIsDeleted(0);
            return bika;
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(bika.getId() + "????????????????????????");
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

    public static boolean needSkip(JsonObject info) {
        String categories = CommonIOUtils.getFromJson2(info, "data-comic-categories").toString();
        if (categories.contains("??????") && !categories.contains("??????")) {
            log.debug("??????BL???");
            Bika bika = getBika(info, "");
            bika.setIsDeleted(1);
//            DBManager.update(bika);
            if (BikaParams.writeDB && !BikaUtils.saveBika(bika)) {
                log.error("???????????????" + bika);
            }
            return true;
        }
        return false;
    }

    private static void build(@NotNull Data result) {

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

    public static String getFileId(@NotNull File file) {
        String id = null;
        Matcher matcher = Pattern.compile("^\\(\\w+\\)").matcher(file.getName());
        if (matcher.find()) {
            id = Pattern.compile("[()]").matcher(file.getName().substring(matcher.start(), matcher.end())).replaceAll("");
        }
        return id;
    }

    @NotNull
    public static String getFolder(@NotNull Bika bika) {
        return "(" + bika.getId() + ")" + bika.getTitle();
    }

    public static String getLocalPath(String comicid, String title) {
        Bika exist = BikaUtils.exists.get(comicid);
        String chapterPath;
        if (exist != null && exist.getLocalPath() != null && !"".equals(exist.getLocalPath())) {
            return exist.getLocalPath();
        } else {
            chapterPath = BikaUtils.defaultSavePath + "\\(" + comicid + ")" + title;
            return chapterPath;
        }
    }

    /**
     * ???????????????????????????????????????
     *
     * @param path
     * @return
     */
    public static List<File> getFolders(String path, String regex) {
        //????????????????????????????????????????????????????????????????????????????????????????????????
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

    public static Bika getById(String id) {
        Data data = new Data();
        data.setUrl(RemoteRequestConstant.GETBYID + id);
        CommonIOUtils.withTimer(data);
        JsonElement element = CommonIOUtils.getFromJson2(data.getResult(), "item");
        return JSONObject.parseObject(element.toString(), Bika.class);
    }

    public static boolean saveBika(Bika bika) {
        Data data = new Data();
        data.setUrl(RemoteRequestConstant.SAVEBIKA);
        data.setParams(JSONObject.toJSONString(bika));
        data.setHeader("Content-Type: application/json");
        CommonIOUtils.postUrl(data);
        RestResponseBean restResponseBean;
        try {
            restResponseBean = JSONObject.parseObject(data.getResult(), RestResponseBean.class);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return false;
        }
        return restResponseBean.isSuccess();
    }

    public void test() {
        List<File> list = getFolders("G:\\Bika??????", "\\(\\w+\\)");
        System.out.println(list.size());
        for (File file : list) {
            System.out.println(file);
        }
    }
}
