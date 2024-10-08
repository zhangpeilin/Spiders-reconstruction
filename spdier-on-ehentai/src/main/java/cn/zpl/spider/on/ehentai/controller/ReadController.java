package cn.zpl.spider.on.ehentai.controller;

import cn.zpl.common.bean.Ehentai;
import cn.zpl.common.bean.ImageComparator;
import cn.zpl.common.bean.QueryDTO;
import cn.zpl.common.bean.RestResponse;
import cn.zpl.spider.on.ehentai.thread.DownloadPageThread;
import cn.zpl.spider.on.ehentai.thread.ScanArchiveThreadv2;
import cn.zpl.spider.on.ehentai.util.EUtil;
import cn.zpl.util.CrudTools;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class ReadController {

    LoadingCache<String, Map<String, Map<Integer, List<String>>>> cache;
    LoadingCache<String, byte[]> imageCache;
    @Resource
    EUtil utils;
    @Resource
    CrudTools tools;

    @RequestMapping("/search")
    public String search(@CookieValue(value = "userToken", required = false) Cookie cookie, HttpServletResponse response) {
        if (cookie != null) {
            String value = cookie.getValue();
        }
        String uuid = UUID.randomUUID().toString();
        Cookie newCookie = new Cookie("userToken", uuid);
        newCookie.setPath("/");
        System.out.println("search设置cookie:" + uuid);
        response.addCookie(newCookie);
        return "search";
    }
    /**
     * 展示搜索结果
     * @param cookie cookie
     * @param session 将查询信息保存到会话中
     * @param queryDTO 查询实体
     * @param response 相应
     */
    @RequestMapping("/getList")
    public String getList(@CookieValue(value = "userToken", required = false) Cookie cookie, HttpSession session, Model model, QueryDTO queryDTO, HttpServletResponse response) {

        //处理tags和categories标签分隔符
        if (!StringUtils.isEmpty(queryDTO.getTags())) {
            queryDTO.setTags(queryDTO.getTags().replace("，",",").replace(" ",","));
        }
        if (!StringUtils.isEmpty(queryDTO.getCategories())) {
            queryDTO.setCategories(queryDTO.getCategories().replace("，",",").replace(" ",","));
        }
        String uuid;
        if (cookie == null) {
            uuid = UUID.randomUUID().toString();
            Cookie newCookie = new Cookie("userToken", uuid);
            newCookie.setPath("/");
            System.out.println("getList设置cookie:" + uuid);
            response.addCookie(newCookie);
        } else {
            uuid = cookie.getValue();
        }
        queryDTO.setTitle(queryDTO.getTitle().toLowerCase());
        RestResponse restResponse = getList(queryDTO);
        if (!restResponse.isSuccess()) {
            return "redirect:/search";
        }
        List<Ehentai> list = restResponse.getList(Ehentai.class);
        model.addAttribute("list", list);
        model.addAttribute("query", queryDTO);
        queryDTO.toMap().forEach(session::setAttribute);
        return "list";
    }

    public RestResponse getList(@RequestBody QueryDTO query) {
        StringBuilder sql = new StringBuilder();
        sql.append("select *from ehentai where lower(title) like '%").append(query.getTitle()).append("%' ");
        sql.append(StringUtils.isEmpty(query.getAuthor()) ? "" : String.format(" and artist like '%%%1$s%%'", query.getAuthor()));
        StringBuilder searchContext = new StringBuilder();
//        https://e-hentai.org/?f_search=female:ponygirl+female:blindfold+female:bondage+female:"masked+face"+female:"pig+girl"&advsearch=1&f_srdd=4
        String[] femaleList = StringUtils.isEmpty(query.getFemale()) ? null : query.getFemale().split(",");
        StringBuilder condition = new StringBuilder(" and (");
        if (femaleList != null) {
            for (String tag : femaleList) {
                if (tag.isEmpty()) {
                    continue;
                }
                tag = utils.convertToTraditionalChinese(tag);
                String con = tag.contains(" ") ? "\"" + tag.replaceAll(" ", "+") + "\"": tag;
                searchContext.append(String.format("female:%1$s ", con));
                condition.append(String.format(" female like '%%%1$s%%' %2$s ", tag, query.getCondition()));
            }

            condition.delete(condition.lastIndexOf(query.getCondition()), condition.lastIndexOf(query.getCondition()) + query.getCondition().length());
            condition.append(")");
            sql.append(femaleList.length != 0 ? condition : "");
        }
        condition.setLength(0);
        condition.append(" and (");
        String[] maleList = StringUtils.isEmpty(query.getMale()) ? null : query.getMale().split(",");
        if (maleList != null) {
            for (String category : maleList) {
                if (category.isEmpty()) {
                    continue;
                }
                category = utils.convertToTraditionalChinese(category);
                String con = category.contains(" ") ? "\"" + category.replaceAll(" ", "+") + "\"": category;
                searchContext.append(String.format("male:%1$s ", con));
                condition.append(String.format(" male like '%%%1$s%%' %2$s ", category, query.getCondition()));
            }
            condition.delete(condition.lastIndexOf(query.getCondition()), condition.lastIndexOf(query.getCondition()) + query.getCondition().length());
            condition.append(")");
            sql.append(maleList.length != 0 ? condition : "");
        }
//        searchContext.delete(searchContext.lastIndexOf("+"), searchContext.length());
        if (query.isComplete()) {
            searchContext.append(" chinese");
        }
        if (query.isSearchOnline()) {
            DownloadPageThread downLoadArchiveThread = new DownloadPageThread();
            downLoadArchiveThread.setDownload(false);
            downLoadArchiveThread.setRecursive(false);
            try {
                String url = "https://e-hentai.org/?f_search=" + URLEncoder.encode(searchContext.toString(), "utf-8");
                if (query.getStar() != 0) {
                    url += "&advsearch=1&f_srdd=" + query.getStar();
                }
                downLoadArchiveThread.setUrl(url);
                downLoadArchiveThread.run();
            } catch (UnsupportedEncodingException ignored) {
            }
        }
        sql.append(" order by favcount desc ").append("limit ").append(query.getSize());
        List<Ehentai> ehentais = tools.commonApiQueryBySql(sql.toString(), Ehentai.class);
        return RestResponse.ok(ehentais);
    }

    @GetMapping("/clearCache/{comicId}")
    public String clearCache(@PathVariable("comicId") String comicId) {
        if (cache != null) {
            Ehentai ehentai = utils.getEh(comicId);
            cache.invalidate(ehentai.getSavePath());
        }
        if (imageCache != null) {
            List<String> collect = imageCache.asMap().keySet().stream().filter(bytes -> bytes.startsWith(comicId)).collect(Collectors.toList());
            collect.forEach(s -> imageCache.invalidate(s));
        }
        return "redirect:/comic/" + comicId;
    }

    @RequestMapping("/comic/{id}")
    public String getChapters(@PathVariable("id") String id, Model model) {

        Ehentai ehentai = utils.getEh(id);
        File file = new File(ehentai.getSavePath());
        if (!file.exists()) {
            return "search";
        }
        List<String> chapters = getChapters(ehentai.getSavePath());
        //获取章节列表后，开始缓存每章节前10页图片
        new Thread(() -> loadImageCache(id, chapters)).start();
        model.addAttribute("fileName", file.getName());
        model.addAttribute("chapters", chapters);
        model.addAttribute("id", id);
        //获取路径后，将章节列表返回前台
        return "chapter";
    }

    private void loadImageCache(String comicId, List<String> chapters) {
        chapters.parallelStream().forEach(chapter -> {
            List<String> images  = getImagesInChapter(comicId, chapter);
            if (CollectionUtils.isEmpty(images)) {
                return;
            }
            images.sort(new ImageComparator());
            List<String> subList = images.size() > 10 ? images.subList(0, 9) : images;
            subList.parallelStream().forEach(image -> getImage(comicId, chapter, image));
        });
    }

    public List<String> getChapters(String zipPath) {
        if (cache != null) {
            Map<String, Map<Integer, List<String>>> ifPresent = cache.getIfPresent(zipPath);
            if (ifPresent == null || ifPresent.isEmpty()) {
                try {
                    ifPresent = cache.get(zipPath);
                } catch (ExecutionException e) {
                    log.error("缓存加载失败", e);
                    return null;
                }
            }
            if (ifPresent.isEmpty()) {
                cache.invalidate(zipPath);
                return getChapters(zipPath);
            }
            Set<String> strings = ifPresent.keySet();
            Optional<String> first = strings.stream().findFirst();
            String folder = first.get();
            List<String> chapters = ifPresent.get(folder).get(0);
            chapters.sort((s1, s2) -> {
                // 将字符串解析为整数进行比较
                int num1 = Integer.parseInt(s1);
                int num2 = Integer.parseInt(s2);
                return Integer.compare(num1, num2);
            });
            return chapters;
        } else {
            cache = CacheBuilder.newBuilder().maximumSize(200000).expireAfterWrite(2000, TimeUnit.HOURS).build(new CacheLoader<String, Map<String, Map<Integer, List<String>>>>() {
                @Override
                public @NotNull Map<String, Map<Integer, List<String>>> load(@NotNull String key) {
                    Map<String, Map<Integer, List<String>>> resultMap = new HashMap<>();
                    File zip = new File(key);
                    try (ZipFile zipFile = new ZipFile(zip)) {
                        List<ZipArchiveEntry> sortedEntries = Collections.list(zipFile.getEntries());
                        List<String> paths = sortedEntries.stream().map(ZipArchiveEntry::getName).collect(Collectors.toList());
                        Optional<String> any = paths.stream().filter(s -> s.contains("/")).findAny();
                        if (!any.isPresent()) {
                            String folder = zip.getName();
                            int subfolder = 1;
                            resultMap.putIfAbsent(folder, new HashMap<>());
                            List<String> subs = new ArrayList<>();
                            subs.add(String.valueOf(subfolder));
                            resultMap.get(folder).putIfAbsent(0, subs);
                            resultMap.get(folder).putIfAbsent(subfolder, new ArrayList<>());
                            for (String path : paths) {
                                resultMap.get(folder).get(subfolder).add(path);
                            }
                            return resultMap;
                        }
                        for (String path : paths) {
                            String[] parts = path.split("/");
                            String folder = parts[0];
                            if (!path.contains("/")) {
                                folder = zip.getName();
                            }
                            resultMap.putIfAbsent(folder, new HashMap<>());
                            int subfolder = 1;
                            String fileName = parts[parts.length - 1];
                            //如果只有一个/表示只有根目录，如果有两个//表示带章节目录，如果有3个//表示带图片名称
                            if (parts.length == 1) {
                                List<String> subs = new ArrayList<>();
                                subs.add(String.valueOf(subfolder));
                                resultMap.get(folder).putIfAbsent(0, subs);
                                continue;
                            }
                            if (parts.length == 2) {
                                resultMap.get(folder).putIfAbsent(subfolder, new ArrayList<>());
                                resultMap.get(folder).get(subfolder).add(fileName);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return resultMap;
                }
            });
        }
        return getChapters(zipPath);
    }

    @RequestMapping("/chapter")
    public String toChapter(@RequestParam("id") String id, @RequestParam("chapter") String num, Model model) {
        File next = getFile(id, String.valueOf(Integer.parseInt(num) + 1));
        model.addAttribute("previous", Integer.parseInt(num) - 1);
        if (next != null) {
            model.addAttribute("next", Integer.parseInt(num) + 1);
        }
        List<String> list = getImagesInChapter(id, num);
        if (list != null) {
            list.sort(new ImageComparator());
        }
        model.addAttribute("imgs", list);
        model.addAttribute("id", id);
        model.addAttribute("num", num);
        return "read2";
    }

    @ResponseBody
    @GetMapping("/read")
    public void read(@RequestParam("id") String id, @RequestParam("chapter") String num, @RequestParam("img") String img, HttpServletResponse response) {
        OutputStream outputStream;
        byte[] image = getImage(id, num, img);
        if (image != null && image.length != 0) {
            try {
                response.setContentType("image/png");
                outputStream = response.getOutputStream();
                outputStream.write(image);
                outputStream.close();
            } catch (IOException ignored) {
            }
        }
    }

    @ResponseBody
    @GetMapping("/loadCoverImg")
    public void loadCoverImg(@RequestParam("id") String id, HttpServletResponse response) {
        //加载封面，先从压缩包路径根目录读取cover文件夹，如果cover文件夹中能找到封面，则返回该封面；如果不能找到，则从压缩包中读取图片存放到cover文件夹中后再返回
        Ehentai ehentai = utils.getEh(id);
        if (StringUtils.isEmpty(ehentai.getSavePath())) {
            utils.invalidCache(id);
            ehentai = utils.getEh(id);
        }
        byte[] image = new byte[0];
        OutputStream outputStream;
        Path cover = Paths.get(new File(ehentai.getSavePath()).getParent(), "cover", id);
        if (cover.toFile().isFile() && cover.toFile().exists()) {
            try {
                image = Files.readAllBytes(cover);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            List<String> imagesInChapter = getImagesInChapter(id, "1");
            if (imagesInChapter != null && !imagesInChapter.isEmpty()) {
                image = getImage(id, "1", imagesInChapter.get(0));
                try {
                    if (image != null) {
                        Files.createDirectories(cover.getParent());
                        Files.write(cover, image, StandardOpenOption.CREATE);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (image != null && image.length != 0) {
            try {
                response.setContentType("image/png");
                outputStream = response.getOutputStream();
                outputStream.write(image);
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Map<String, Map<Integer, List<String>>> getZipInfo(String zipPath) {
        if (cache == null) {
            getChapters(zipPath);
        }
        Map<String, Map<Integer, List<String>>> ifPresent = cache.getIfPresent(zipPath);
        if (ifPresent == null || ifPresent.isEmpty()) {
            try {
                ifPresent = cache.get(zipPath);
                return ifPresent;
            } catch (ExecutionException e) {
                log.error("缓存加载失败", e);
                return null;
            }
        } else {
            return ifPresent;
        }
    }

    private File getFile(String id, String child) {
        if (child == null || "".equalsIgnoreCase(child)) {
            child = "";
        }
        Ehentai ehentai = utils.getEh(id);
        List<String> chapters = getChapters(ehentai.getSavePath());
        if (chapters.contains(child)) {
            return new File(ehentai.getSavePath(), child);
        } else {
            return null;
        }
    }

    private List<String> getImagesInChapter(String id, String chapter) {
        Ehentai ehentai = utils.getEh(id);
        Map<String, Map<Integer, List<String>>> zipInfo = getZipInfo(ehentai.getSavePath());
        if (zipInfo == null || zipInfo.isEmpty()) {
            return null;
        }
        Optional<String> folder = zipInfo.keySet().stream().findFirst();
        Set<Map.Entry<Integer, List<String>>> entries = zipInfo.get(folder.get()).entrySet();
        for (Map.Entry<Integer, List<String>> entry : entries) {
            if (String.valueOf(entry.getKey()).equalsIgnoreCase(chapter)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private byte[] getImage(String comicId, String chapter, String image) {
        String myKey = comicId + "#" + chapter + "#" + image;
        if (imageCache != null) {
            byte[] ifPresent = imageCache.getIfPresent(myKey);
            if (ifPresent == null || ifPresent.length == 0) {
                try {
                    ifPresent = imageCache.get(myKey);
                    return ifPresent;
                } catch (Exception e) {
                    log.error("压缩包id:{}读取失败", comicId, e);
                    return null;
                }
            } else {
                return ifPresent;
            }
        } else {
            imageCache = CacheBuilder.newBuilder().maximumSize(50).expireAfterWrite(1, TimeUnit.MINUTES).build(new CacheLoader<String, byte[]>() {
                @Override
                public byte @NotNull [] load(@NotNull String key) throws Exception {
                    String[] split = key.split("#");
                    String comicId = split[0];
                    String chapter = split[1];
                    String image = split[2];
                    Ehentai ehentai = utils.getEh(comicId);
                    Map<String, Map<Integer, List<String>>> zipInfo = getZipInfo(ehentai.getSavePath());
                    if (zipInfo == null || zipInfo.isEmpty()) {
                        return null;
                    }
                    try (ZipFile zipFile = new ZipFile(ehentai.getSavePath())) {
                        ZipArchiveEntry entry = zipFile.getEntry(image);
                        if (entry == null) {
                            entry = zipFile.getEntry(chapter + "/" + image);
                        }
                        InputStream inputStream = zipFile.getInputStream(entry);
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[20000];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            bos.write(buffer, 0, bytesRead);
                        }
                        inputStream.close();
                        bos.close();
                        return bos.toByteArray();
                    }
                }
            });
        }
        return getImage(comicId, chapter, image);
    }
}
