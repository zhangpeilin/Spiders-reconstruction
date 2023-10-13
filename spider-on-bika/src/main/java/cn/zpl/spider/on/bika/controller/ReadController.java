package cn.zpl.spider.on.bika.controller;

import cn.zpl.common.bean.Bika;
import cn.zpl.common.bean.RestResponse;
import cn.zpl.common.bean.ImageComparator;
import cn.zpl.common.bean.QueryDTO;
import cn.zpl.spider.on.bika.utils.BikaUtils;
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
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    BikaUtils utils;
    @Resource
    CrudTools tools;

    @RequestMapping("/search")
    public String search(@CookieValue(value = "userToken", required = false) Cookie cookie, HttpServletResponse response, RedirectAttributes redirectAttributes) {
        if (cookie != null) {
            String value = cookie.getValue();
//            if (BikaUtils.cache.get(value) instanceof QueryDTO) {
//                redirectAttributes.addFlashAttribute(BikaUtils.cache.get(value));
//                return "redirect:/getList";
//            }
        }
        String uuid = UUID.randomUUID().toString();
//        BikaUtils.cache.put(uuid, new Object());
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
        RestResponse restResponse = getList(queryDTO);
        if (!restResponse.isSuccess()) {
            return "redirect:/search";
        }
        List<Bika> list = restResponse.getList(Bika.class);
        model.addAttribute("list", list);
        model.addAttribute("query", queryDTO);
//        model.addAttribute("pages", restResponseBean.getPages());
//        model.addAttribute("total", restResponseBean.getTotal());
        queryDTO.toMap().forEach(session::setAttribute);
        return "list";
    }

    public RestResponse getList(@RequestBody QueryDTO query) {
        StringBuilder sql = new StringBuilder();
        sql.append("select *from bika where lower(title) like '%").append(query.getTitle()).append("%' and is_deleted = 0 ");
        String[] tagsList = query.getTags().isEmpty() ? null : query.getTags().split(",");
        StringBuilder condition = new StringBuilder(" and (");
        if (tagsList != null) {
            for (String tag : tagsList) {
                if (tag.isEmpty()) {
                    continue;
                }
                if (query.isSearchOnline()) {
                    utils.search(tag, false);
                }
                condition.append(String.format(" tags like '%%%1$s%%' %2$s ", tag, query.getCondition()));
            }
            condition.delete(condition.lastIndexOf(query.getCondition()), condition.lastIndexOf(query.getCondition()) + query.getCondition().length());
            condition.append(")");
            sql.append(tagsList.length != 0 ? condition : "");
        }
        condition.setLength(0);
        condition.append(" and (");
        String[] categoriesList = query.getCategories().isEmpty() ? null : query.getCategories().split(",");
        if (categoriesList != null) {
            for (String category : categoriesList) {
                if (category.isEmpty()) {
                    continue;
                }
                if (query.isSearchOnline()) {
                    utils.search(category, false);
                }
                condition.append(String.format(" categories like '%%%1$s%%' %2$s ", category, query.getCondition()));
            }
            condition.delete(condition.lastIndexOf(query.getCondition()), condition.lastIndexOf(query.getCondition()) + query.getCondition().length());
            condition.append(")");
            sql.append(categoriesList.length != 0 ? condition : "");
        }

        sql.append(" order by likes_count desc");
        if (query.isSearchOnline()) {
            utils.search(query.getTitle(), false);
        }
        List<Bika> bikas = tools.commonApiQueryBySql(sql.toString(), Bika.class);
        return RestResponse.ok(bikas);
    }

    @GetMapping("/clearCache/{comicId}")
    public String clearCache(@PathVariable("comicId") String comicId) {
        if (cache != null) {
            Bika bikaExist = utils.getBikaExist(comicId);
            cache.invalidate(bikaExist.getLocalPath());
        }
        if (imageCache != null) {
            List<String> collect = imageCache.asMap().keySet().stream().filter(bytes -> bytes.startsWith(comicId)).collect(Collectors.toList());
            collect.forEach(s -> imageCache.invalidate(s));
        }
        return "redirect:/comic/" + comicId;
    }

    @RequestMapping("/comic/{id}")
    public String getChapters(@PathVariable("id") String id, Model model) {

        Bika bika = utils.getBikaExist(id);
        File file = new File(bika.getLocalPath());
        if (!file.exists()) {
            return "search";
        }
        List<String> chapters = getChapters(bika.getLocalPath());
        model.addAttribute("fileName", file.getName());
        model.addAttribute("chapters", chapters);
        model.addAttribute("id", id);
        //获取路径后，将章节列表返回前台
        return "chapter";
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
                        for (String path : paths) {
                            if (path.contains("list.txt")) {
                                continue;
                            }
                            String[] parts = path.split("/");
                            String folder = parts[0];
                            resultMap.putIfAbsent(folder, new HashMap<>());
                            int subfolder = parts.length >= 2 ? Integer.parseInt(parts[1]) : 0;
                            String fileName = parts[parts.length - 1];
                            //如果只有一个/表示只有根目录，如果有两个//表示带章节目录，如果有3个//表示带图片名称
                            if (parts.length == 1) {
                                continue;
                            }
                            resultMap.get(folder).putIfAbsent(0, new ArrayList<>());
                            if (parts.length == 2) {
                                resultMap.get(folder).get(0).add(String.valueOf(subfolder));
                            }
                            resultMap.get(folder).putIfAbsent(subfolder, new ArrayList<>());
                            if (parts.length == 3) {
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
        FileInputStream fileInputStream;
        OutputStream outputStream;
        byte[] image = getImage(id, num, img);
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

    @RequestMapping("/test")
    public String test(Model model) {
        File base = new File("C:\\Users\\zpl\\Pictures");
        String[] names = base.list((dir, name) -> name.contains("png"));
        model.addAttribute("names", names);
        return "test";
    }

    private File getFile(String id, String child) {
        if (child == null || "".equalsIgnoreCase(child)) {
            child = "";
        }
        Bika bika = utils.getBikaExist(id);
        List<String> chapters = getChapters(bika.getLocalPath());
        if (chapters.contains(child)) {
            return new File(bika.getLocalPath(), child);
        } else {
            return null;
        }
    }

    private List<String> getImagesInChapter(String id, String chapter) {
        Bika bika = utils.getBikaExist(id);
        Map<String, Map<Integer, List<String>>> zipInfo = getZipInfo(bika.getLocalPath());
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
                    log.error("压缩包读取失败", e);
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
                    Bika bika = utils.getBikaExist(comicId);
                    Map<String, Map<Integer, List<String>>> zipInfo = getZipInfo(bika.getLocalPath());
                    if (zipInfo == null || zipInfo.isEmpty()) {
                        return null;
                    }
                    Optional<String> folder = zipInfo.keySet().stream().findFirst();
                    try (ZipFile zipFile = new ZipFile(bika.getLocalPath())) {
                        ZipArchiveEntry entry = zipFile.getEntry(folder.get() + "/" + chapter + "/" + image);
                        InputStream inputStream = zipFile.getInputStream(entry);
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
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
