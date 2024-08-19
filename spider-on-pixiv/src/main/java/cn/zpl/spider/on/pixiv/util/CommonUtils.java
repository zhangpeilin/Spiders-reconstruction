package cn.zpl.spider.on.pixiv.util;

import cn.zpl.common.bean.PixivPictures;
import cn.zpl.pojo.Data;
import cn.zpl.pojo.DownloadDTO;
import cn.zpl.pojo.SynchronizeLock;
import cn.zpl.spider.on.pixiv.config.PixivProperties;
import cn.zpl.spider.on.pixiv.thread.GetImgUrlThread2;
import cn.zpl.thread.OneFileOneThread;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.CommonProperties;
import cn.zpl.util.CrudTools;
import cn.zpl.util.DownloadTools;
import cn.zpl.util.UrlContainer;
import com.google.common.base.CaseFormat;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CommonUtils {

    public static LoadingCache<String, Object> exists;

    public static boolean cacheLoaded = false;
    public static int onPageCount = 100;
    public static ThreadLocal<List<File>> result = new ThreadLocal<>();

    @Resource
    CrudTools crudTools;

    @Resource
    PixivProperties pixivProperties;

    @Resource
    CommonProperties commonProperties;

    public void getImgUrl(@NotNull PixivPictures picture, Vector<DownloadDTO> list) {
        String url = "https://www.pixiv.net/ajax/illust/" + picture.getId() +
                "/pages";
        String illust_url = pixivProperties.getPageUrl() + picture.getId();
        UrlContainer container = new UrlContainer(url);
        container.setProxy(true);
        container.setCookies(pixivProperties.getCookies());

        try {
            if (picture.getTags() == null || picture.getBookmarkCount() == null) {
                getImgDetailInfo(picture);
            }
            List<String> path = new ArrayList<>();
            Data data = new Data();
            data.setProxy(true);
            data.setUrl(url);
            data.setHeader(commonProperties.getCommonHeader());
            data.setCookie(pixivProperties.getCookies());
            CommonIOUtils.withTimer(data);
            JsonElement json = CommonIOUtils.paraseJsonFromStr(Objects.requireNonNull(data.getResult()).toString());
            if (json.isJsonNull()) {
                log.error("解析图片出错，原因：json为空，传入的图片id：" + picture.getId());
                return;
            }
            if (CommonIOUtils.getFromJson2Boolean(json, "error")) {
                log.error("解析图片出错，原因：" + CommonIOUtils.getFromJson2Str(json, "message"));
                return;
            }
            JsonElement body = CommonIOUtils.getFromJson2(json, "body");
            String upper = CommonIOUtils.filterFileName(picture.getUserName()) + "(" + picture.getUserId() + ")";

            container.setUrl(illust_url);
            String imgeName = CommonIOUtils.filterFileName(picture.getIllustTitle() == null ? getTitle(container) : picture.getIllustTitle());
            //用list存放每个作品对应的下载信息，最后将list序列化存入数据库中
            List<DownloadDTO> toSave = new ArrayList<>();
            //多张图，需要遍历，会有多个dto对象生成，此时存储到数据中的是一个list集合
            if (body.isJsonArray() && body.getAsJsonArray().size() > 1) {
                int part = 1;
                for (JsonElement jsonElement : body.getAsJsonArray()) {
                    String img_url = CommonIOUtils.getFromJson2(jsonElement, "urls-original").getAsString();
                    //如果已经设置好了保存路径，则根据保存路径填写，如果没有则默认保存到用作者用户名命名的文件夹中
                    if (picture.getSavePath() == null) {
                        path.clear();
                        path.add(pixivProperties.getPixivSavePath());
                        path.add(upper);
                        path.add("(" + picture.getId() + ")" + imgeName);
                    } else {
                        path.clear();
                        path.add(picture.getSavePath());
                        path.add("(" + picture.getId() + ")" + imgeName);
                    }
                    String partName = part + img_url.substring(img_url.lastIndexOf("."));
                    part++;
                    DownloadDTO dto = new DownloadDTO();
                    dto.setSavePath(CommonIOUtils.makeFilePath(path, partName));
                    dto.setFileName(partName);
                    dto.setUrl(img_url);
                    dto.setId(picture.getId());
                    dto.setReferer(illust_url);
                    dto.setProxy(true);
                    list.add(dto);
                    toSave.add(dto);
                }
                picture.setSavePath(CommonIOUtils.makeFilePath(path, null));
//                picture.setImgObject(Object2Bytes(toSave));
                return;
            }
            if (body.isJsonArray() && body.getAsJsonArray().size() == 1) {
                String img_url =
                        CommonIOUtils.getFromJson2(body.getAsJsonArray().get(0), "urls-original").getAsString();
                boolean ugoira = img_url.substring(img_url.lastIndexOf("/")).contains("ugoira");
                //如果包含ugoira说明是动图，解析获取zip下载地址
                if (picture.getSavePath() == null) {
                    path.add(pixivProperties.getPixivSavePath());
                    path.add(upper);
                } else {
                    path.add(picture.getSavePath());
                }
                if (ugoira) {
                    //将压缩文件加入下载队列中
                    path.add("(" + picture.getId() + ")" + imgeName);
                    String zipUrl = getGifZipUrl(picture.getId());
                    DownloadDTO dto = new DownloadDTO();
                    String type = zipUrl.substring(zipUrl.lastIndexOf("."));
                    String partName = "all_img" + type;
                    dto.setSavePath(CommonIOUtils.makeFilePath(path, partName));
                    dto.setFileName(partName);
                    dto.setUrl(zipUrl);
                    dto.setReferer(zipUrl);
                    dto.setId(picture.getId());
                    list.add(dto);
                    toSave.add(dto);
                }
                //再将预览图片加入下载队列
                String fileName = (ugoira ? "" : "(" + picture.getId() + ")") + imgeName + img_url.substring(img_url.lastIndexOf("."));
                DownloadDTO dto = new DownloadDTO();
                dto.setSavePath(CommonIOUtils.makeFilePath(path, fileName));
                dto.setFileName(fileName);
                dto.setUrl(img_url);
                dto.setReferer(illust_url);
                dto.setId(picture.getId());
                list.add(dto);
                toSave.add(dto);
                picture.setSavePath(ugoira ? CommonIOUtils.makeFilePath(path, null) : CommonIOUtils.makeFilePath(path, fileName));
//                picture.setImgObject(Object2Bytes(toSave));
            }

        } catch (Exception e) {
            log.error("解析出错，异常信息：\n", e);
            log.error("重新解析illust_id" + picture.getId());
            getImgUrl(picture, list);
        }
    }
    public void downloadByUid(String uid) {
        String url = "https://www.pixiv.net/ajax/user/" + uid +
                "/profile/all";
        log.debug(url);
        JsonElement imagesJson;
        String upperName = getAuthorName(uid);

        Data data = new Data();
        data.setProxy(true);
        data.setWaitSeconds(10);
        data.setHeader(commonProperties.getCommonHeader());
        data.setCookie(pixivProperties.getCookies());
        data.setUrl(url);
        data.setAlwaysRetry();
        CommonIOUtils.withTimer(data);
        imagesJson = CommonIOUtils.paraseJsonFromStr(Objects.requireNonNull(data.getResult()));

        JsonElement illusts = CommonIOUtils.getFromJson2(imagesJson, "body-illusts");
        log.debug(String.valueOf(imagesJson));
        JsonElement manga = CommonIOUtils.getFromJson2(imagesJson, "body-manga");
        Vector<DownloadDTO> dtoList = new Vector<>();
        Vector<PixivPictures> picturesList = new Vector<>();
        DownloadTools getImgUrl = DownloadTools.getInstance(20, "");
        if (illusts instanceof JsonObject) {
            for (Map.Entry<String, JsonElement> entry : illusts.getAsJsonObject().entrySet()) {
                PixivPictures picture = new PixivPictures();
                picture.setId(entry.getKey());
                //这时候没有的属性在GetImg的时候补充
                picture.setUserId(uid);
                picture.setUserName(upperName);
                getImgUrl.ThreadExecutorAdd(new GetImgUrlThread2(picture, dtoList));
                picturesList.add(picture);
            }
        }
        if (manga instanceof JsonObject) {
            for (Map.Entry<String, JsonElement> entry : manga.getAsJsonObject().entrySet()) {
                PixivPictures picture = new PixivPictures();
                picture.setId(entry.getKey());
                picture.setUserId(uid);
                picture.setUserName(upperName);
                getImgUrl.ThreadExecutorAdd(new GetImgUrlThread2(picture, dtoList));
                picturesList.add(picture);
            }
        }
        getImgUrl.shutdown();
        //所有的下载共用一个锁
        SynchronizeLock lock = new SynchronizeLock();
        getImgUrl.restart(10);
        dtoList.forEach(downloadDTO -> {
            downloadDTO.setSynchronizeLock(lock);
            downloadDTO.setProxy(true);
            downloadDTO.setAlwaysRetry();
            getImgUrl.ThreadExecutorAdd(new OneFileOneThread(downloadDTO));
        });
        getImgUrl.shutdown();
        List<DownloadDTO> Exists =
                dtoList.stream().filter(downloadDTO -> new File(downloadDTO.getSavePath()).exists()).collect(Collectors.toList());
        List<PixivPictures> newList =  picturesList.stream().filter(pixivPictures -> Exists.stream().anyMatch(downloadDTO -> downloadDTO.getId().equals(pixivPictures.getId()))).collect(Collectors.toList());
        List<PixivPictures> rest =  picturesList.stream().filter(pixivPictures -> Exists.stream().noneMatch(downloadDTO -> downloadDTO.getId().equals(pixivPictures.getId()))).collect(Collectors.toList());
        List<String> path = new ArrayList<>();
        path.add(pixivProperties.getPixivSavePath());
        path.add(CommonIOUtils.filterFileName(upperName) + "(" + uid + ")");
        rest.forEach(pixivPictures -> {
            PixivPictures pic = getExists(pixivPictures.getId());
            if (pic != null && pic.getSavePath() != null) {
                File file = new File(pic.getSavePath());
                if (file.exists() && !file.getPath().equalsIgnoreCase(new File(CommonIOUtils.makeFilePath(path, file.getName())).getPath())) {
                    try {
                        FileUtils.copyToDirectory(file, new File(CommonIOUtils.makeFilePath(path, null)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        crudTools.commonApiSave(newList);
    }
    //api地址
    public String getAuthorName(String uid) {
        List<PixivPictures> pictures = crudTools.commonApiQueryBySql("select distinct * from pixiv_pictures " +
                "where user_id =" +
                " " + uid, PixivPictures.class);

        if (!pictures.isEmpty()) {
            return pictures.get(0).getUserName();
        }
        if (uid == null || "".equals(uid)) {
            return "佚名";
        }
        Data data = new Data();
        data.setProxy(true);
        data.setUrl("https://www.pixiv.net/ajax/user/" + uid + "/profile/top");
        CommonIOUtils.withTimer(data);
        if (data.getResult() == null) {
            //很难走到这一步
            log.error("此图片作者信息失败，作者id：" + uid);
            return "佚名";
        }
        JsonElement title = CommonIOUtils.getFromJson2(CommonIOUtils.paraseJsonFromStr(data.getResult().toString()),
                "body-extraData" +
                        "-meta" +
                        "-title");
        return title.isJsonNull() ? "佚名" : title.getAsString().replaceAll("- pixiv", "").trim();
    }

    public static String getTitle(@NotNull UrlContainer container) {
        Data data = new Data();
//        data.setHeader(CommonMethods.Cookies);
        data.setProxy(true);
        data.setUrl(container.getUrl());
        CommonIOUtils.withTimer(data);
        if (data.getResult() == null) {
            if (container.canDoRetry()) {
                container.doRetry();
                return getTitle(container);
            } else {
                //很难走到这一步
                log.error("此图片获取标题失败，请检查：" + container.getUrl());
                return UUID.randomUUID().toString().replace("-", "");
            }
        }
        Element title = CommonIOUtils.getElementFromStr(data.getResult().toString(), "[property=\"twitter:title\"]");
        String imageName = title.attr("content");
        return CommonIOUtils.filterFileName(imageName);
    }

    private String getGifZipUrl(String illust_id) {
        Data data = new Data();
        String url = "https://www.pixiv.net/ajax/illust/" +
                illust_id + "/ugoira_meta";
        data.setUrl(url);
        data.setProxy(true);
        data.setHeader(pixivProperties.getCookies());
        CommonIOUtils.withTimer(data);
        JsonElement json = CommonIOUtils.paraseJsonFromStr(data.getResult());
        return CommonIOUtils.getFromJson2Str(json, "body-originalSrc");
    }

    public boolean isLogin() {
        Data data = new Data();
        data.setUrl("https://www.pixiv.net/setting_profile.php");
        data.setProxy(true);
        data.setCookie(pixivProperties.getCookies());
        CommonIOUtils.withTimer(data);
        Element nick = CommonIOUtils.getElementFromStr(data.getResult().toString(), "input#nick");
        if (nick == null) {
            return false;
        }
        return "不拆的后期".equals(nick.attr("value"));
    }

    public int downLoadPage(int page, String keyWord) {
        log.info("分析第" + page + "页");
        Vector<DownloadDTO> dtoList = new Vector<>();
        Vector<PixivPictures> picturesList = new Vector<>();
        String url = null;
        try {
//            url = "https://www.pixiv.net/search.php?s_mode=s_tag_full&word=" + URLEncoder.encode(keyWord, "utf-8") +
//                    "&order" +
//                    "=date&p=" + page;
//            url = "https://www.pixiv.net/tags/" + URLEncoder.encode(keyWord, "utf-8") + "/artworks?p=" + page;
            url = "https://www.pixiv.net/ajax/search/artworks/" + URLEncoder.encode(keyWord, "utf-8") + "?word=" + URLEncoder.encode(keyWord, "utf-8") + "&order=" + (pixivProperties.isDateSort() ? "date_d" : "date") + "&mode=all&p=" + page + "&s_mode=s_tag_full&type=all&lang=zh";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Data data_ = new Data();
        data_.setHeader(commonProperties.getCommonHeader());
        data_.setCookie(pixivProperties.getCookies());
        data_.setProxy(true);
        assert url != null;
        data_.setUrl(url);
        CommonIOUtils.withTimer(data_);
        log.debug(data_.getResult());
        JsonElement json = CommonIOUtils.getFromJson2(data_.getResult(), "body-illustManga-data");
        if (json == null) {
            log.error("json解析失败，当前页面为第" + page + "页");
            throw new RuntimeException("json解析失败，当前页面为第" + page + "页");
        }
        DownloadTools GetImgUrl = DownloadTools.getInstance(20, "");
        if (json.isJsonArray()) {
            if (json.getAsJsonArray().size() == 0) {
                return 0;
            }
            for (JsonElement jsonElement : json.getAsJsonArray()) {
                String illustId = CommonIOUtils.getFromJson2Str(jsonElement, "id");
                String illustTitle = CommonIOUtils.getFromJson2Str(jsonElement, "title");
//                String tags = CommonIOUtils.getFromJson2Str(jsonElement, "tags");
                String userId = CommonIOUtils.getFromJson2Str(jsonElement, "userId");
                String userName = CommonIOUtils.getFromJson2Str(jsonElement, "userName");
                int width = CommonIOUtils.getIntegerFromJson(jsonElement, "width");
                int height = CommonIOUtils.getIntegerFromJson(jsonElement, "height");
                int bookmarkCount = CommonIOUtils.getIntegerFromJson(jsonElement, "bookmarkCount");
                PixivPictures picture = new PixivPictures();
                picture.setId(illustId);
                picture.setIllustTitle(illustTitle);
//                picture.setTags(tags);
                picture.setUserId(userId);
                picture.setUserName(userName);
                picture.setWidth(width);
                picture.setHeight(height);
                if (bookmarkCount != 0) {
                    picture.setBookmarkCount(bookmarkCount);
                }
                GetImgUrl.ThreadExecutorAdd(new GetImgUrlThread2(picture, dtoList));
                picturesList.add(picture);
            }
        }
        GetImgUrl.shutdown();

        if (picturesList.isEmpty()) {
            return 0;
        }
//        try {
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            ObjectOutputStream oos;
//            oos = new ObjectOutputStream(outputStream);
//            oos.writeObject(dtoList);
//            picturesList.get(0).setImgObject(outputStream.toByteArray());
//        } catch (IOException e) {
//            log.error(e.getMessage());
//            log.error("保存下载信息失败，当前页面为第" + page + "页");
////            throw new RuntimeException("保存下载信息失败，当前页面为第" + page + "页");
//        }
        picturesList.forEach(pixivPictures -> {
            log.debug("id:" + pixivPictures.getId() + "--->like:" + pixivPictures.getBookmarkCount());
            crudTools.commonApiSave(pixivPictures);
        });
        return picturesList.size();
    }

    @Test
    public void domain() {
        PixivPictures pictures = new PixivPictures();
        pictures.setId("85581348");
        Vector<DownloadDTO> list = new Vector<>();
        getImgUrl(pictures, list);
    }

    public void getImgDetailInfo(@NotNull PixivPictures picture) {
        String url = pixivProperties.getPageUrl() + picture.getId();
        Data data = new Data();
        data.setProxy(true);
        data.setUrl(url);
        data.setProxyIP("127.0.0.1");
        data.setProxyPort(8118);
        data.setWaitSeconds(10);
//        data.setSpecialProxyConfig(true);
        CommonIOUtils.withTimer(data);
        long begin = System.currentTimeMillis();
        Elements scripts = CommonIOUtils.getElementsFromStr(Objects.requireNonNull(data.getResult()).toString(), "meta#meta-preload-data");
        long end = System.currentTimeMillis();
        if (scripts.isEmpty()) {
            return;
        }
        for (Element script : scripts) {
            try {
                log.debug("耗时：" + CommonIOUtils.transformMills2Date2(end - begin));
                JsonElement json =
                        CommonIOUtils.getFromJson2(CommonIOUtils.paraseJsonFromStr(script.attr("content")), "illust-" + picture.getId());
                int bookmarkCount = CommonIOUtils.getFromJson2Integer(json, "bookmarkCount");
                String illustTitle = CommonIOUtils.getFromJson2Str(json, "illustTitle");
                String userName = CommonIOUtils.getFromJson2Str(json, "userName");
                String userId = CommonIOUtils.getFromJson2Str(json, "userId");
                picture.setUserName(picture.getUserName() == null ? userName : picture.getUserName());
                picture.setUserId(picture.getUserId() == null ? userId : picture.getUserId());
                StringBuilder tagStr = new StringBuilder();
                tagStr.append("[");
                JsonElement tags = CommonIOUtils.getFromJson2(json, "tags-tags");
                tags.getAsJsonArray().forEach(element -> {
                    String tag = CommonIOUtils.getFromJson2Str(element, "tag");
                    String translation = CommonIOUtils.getFromJson2Str(element, "translation-en");
                    tagStr.append("\"").append(tag).append("\"").append("".equals(translation) ? "," : ",\"" + translation + "\",");
                });
                tagStr.deleteCharAt(tagStr.length() - 1).append("]");
                int height = CommonIOUtils.getFromJson2Integer(json, "height");
                int width = CommonIOUtils.getFromJson2Integer(json, "width");
                picture.setBookmarkCount(bookmarkCount);
                picture.setWidth(width);
                picture.setHeight(height);
                picture.setTags(picture.getTags() == null ? tagStr.toString() : picture.getTags());
                picture.setIllustTitle(illustTitle);
                break;
            } catch (Exception e) {
                log.error("获取图片信息失败，异常信息：\n", e);
                if (data.doRetry()) {
                    log.error("3秒后重试");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    getImgDetailInfo(picture);
                }
                //重试次数用完后返回
                return;
            }
        }
    }

//    public boolean isExists(PixivPictures pixivPicture) {
//        //判断文件是否存在，不能单纯依靠文件是否存在，需要判断list.txt中是否存在
//        synchronized (CommonUtils.class) {
//            if (exists.isEmpty()) {
//                List<PixivPictures> list = crudTools.commonApiQuery("select * from pixiv_pictures", PixivPictures.class);
//                list.forEach(pixivPictures -> exists.put(pixivPictures.getId(), pixivPictures));
//            }
//            if (getExists(pixivPicture.getId()) != null) {
//                if (getExists(pixivPicture.getId()).getSavePath() == null) {
//                    return false;
//                }
//                File file = new File(getExists(pixivPicture.getId()).getSavePath());
//                if (file.isDirectory()) {
//                    //如果是目录，则判断同级目录中的list.txt文件中是否存在该目录，如果存在判定为文件已下载完成，否则判定未下载完成
//                    File list_txt = new File(file.getParent(), "list.txt");
//                    return SaveLogForImages.isChapterCompelete(file.getPath());
//                } else {
//                    //如果是文件
//                    return SaveLogForImages.isImgCompelete(file.getPath());
//                }
//            }
//            return false;
//        }
//    }

    public int saveCollectionInfo(int offset, String uid) {
        Data data = new Data();
        data.setCookie(pixivProperties.getCookies());
        data.setProxy(true);
        data.setWaitSeconds(10);
        data.setUrl("https://www.pixiv.net/ajax/user/" + uid +
                "/illusts" +
                "/bookmarks?tag=&offset=" + offset + "&limit=" + onPageCount + "&rest=show");
        CommonIOUtils.withTimer(data);
        JsonElement body = CommonIOUtils.getFromJson2(data.getResult().toString(), "body");
        JsonElement works = CommonIOUtils.getFromJson2(body, "works");
        int total = CommonIOUtils.getFromJson2Integer(body, "total");
        String savePath = CommonIOUtils.getFromJson2Str(body, "extraData-meta-title").replaceAll("- pixiv", "").trim();
        DownloadTools getImgUrl = DownloadTools.getInstance(10, "");
        getImgUrl.setName(uid);
        Vector<PixivPictures> picturesList = new Vector<>();
        if (works.isJsonArray()) {
            works.getAsJsonArray().forEach(jsonElement -> {
                PixivPictures picture = new PixivPictures();
                picture.setId(CommonIOUtils.getFromJson2Str(jsonElement, "illustId"));
                picture.setSavePath(pixivProperties.getPixivSavePath() + File.separator + savePath + "\\");
                picturesList.add(picture);
            });
        }
//        getImgUrl.shutdown();
        //所有的下载共用一个锁
        SynchronizeLock lock = new SynchronizeLock();
//        getImgUrl.restart(10);
        Vector<DownloadDTO> dtoList = new Vector<>();
        picturesList.forEach(pixivPictures -> getImgUrl.ThreadExecutorAdd(new GetImgUrlThread2(pixivPictures, dtoList)));
        getImgUrl.shutdown();
        getImgUrl.restart(10);
        dtoList.forEach(downloadDTO -> {
            downloadDTO.setSynchronizeLock(lock);
            getImgUrl.ThreadExecutorAdd(new OneFileOneThread(downloadDTO));
        });
        getImgUrl.shutdown();

        List<DownloadDTO> Exists =
                dtoList.stream().filter(downloadDTO -> new File(downloadDTO.getSavePath()).exists()).collect(Collectors.toList());
        List<PixivPictures> newList = picturesList.stream().filter(pixivPictures -> Exists.stream().anyMatch(downloadDTO -> downloadDTO.getId().equals(pixivPictures.getId()))).collect(Collectors.toList());
        crudTools.commonApiSave(newList);
        return total;
    }

    public static void addExists(@NotNull List<PixivPictures> list) {
        list.forEach(pixivPictures -> exists.put(pixivPictures.getId(), pixivPictures));
    }

    /**
     * 获取当前目录下所有的文件夹
     *
     * @param path
     * @return
     */
    public static List<File> getFolders(String path, String regex) {
        //如果表达式为空，则返回所有文件和文件夹，否则返回符合表达式格式的文件和文件夹
        File base = new File(path);
        if (result.get() == null) {
            List<File> list = new ArrayList<>();
            result.set(list);
        }
        if (base.isDirectory()) {
            if (!result.get().contains(base) && (regex == null || "".equals(regex) || Pattern.compile(regex).matcher(base.getName()).find())) {
                result.get().add(base);
            }
            for (File file : Objects.requireNonNull(base.listFiles())) {
                if (regex == null || "".equals(regex)) {
                    result.get().add(file);
                } else if (Pattern.compile(regex).matcher(file.getName()).find()) {
                    result.get().add(file);
                }
                getFolders(file.getPath(), regex);
            }
        }
        return result.get();
    }


    @NotNull
    public static byte[] Object2Bytes(Object obj) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(obj);
        return byteArrayOutputStream.toByteArray();
    }

    @Test
    public void test() {
        downLoadPage(153, "原神");
    }

    public static void delFile(@NotNull PixivPictures pixivPictures) {
        try {
            FileUtils.moveToDirectory(new File(pixivPictures.getSavePath()), new File("g:\\pixiv_del"), true);
        } catch (IOException e) {
            log.error(String.valueOf(e));
        }

    }

    public synchronized PixivPictures getExists(String pid) {
        if (exists != null) {
//            log.debug("当前缓存中数据条数：{}", exists.size());
            if (!cacheLoaded) {
                new Thread(() -> {
                    try {
                        log.debug("开始加载全量缓存");
                        cacheLoaded = true;
                        List<String> list = new ArrayList<>();
                        list.add("pixiv:5821859e5f6b9a4f93dbf759");
                        exists.getAll(list);
                    } catch (Exception ignored) {
                    }
                }).start();
            }
            Object exist = exists.getIfPresent(pid);
            if (exist == null) {
                try {
                    return (PixivPictures) exists.get(pid);
                } catch (Exception e) {
                    return null;
                }
            } else {
                return (PixivPictures) exist;
            }
        }
        exists = CacheBuilder.newBuilder().maximumSize(200000).expireAfterWrite(2000, TimeUnit.SECONDS).build(new CacheLoader<String, Object>() {
            @Override
            //key格式为表名:主键
            public @NotNull Object load(@NotNull String key) {
                String[] split = key.split(":");
                String tableName = split[0];
                String entityName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tableName);
                String queryKey = split[1];
                Class<?> entity = CommonIOUtils.getEntityExists(entityName);
                List<?> objects = crudTools.commonApiQueryBySql(String.format("select * from %2$s where id = '%1$s'", queryKey, tableName), entity);
                if (objects.size() != 0) {
                    return objects.get(0);
                }
                return null;
            }

            @Override
            public Map<String, Object> loadAll(Iterable<? extends String> keys) {
                List<PixivPictures> PixivPictures = crudTools.commonApiQueryBySql("select * from pixiv_pictures", PixivPictures.class);
                return PixivPictures.stream().collect(Collectors.toMap(picture -> "pixiv:" + picture.getId(), bika -> bika));
            }
        });
        return getExists(pid);
    }

    public static void delFile(@NotNull File file) {
        PixivPictures pictures = new PixivPictures();
        pictures.setSavePath(file.getPath());
        delFile(pictures);
    }
}
