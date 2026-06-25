package cn.zpl.controller;

import cn.zpl.cache.DiskImageCache;
import cn.zpl.common.bean.Ehentai;
import cn.zpl.common.bean.ImageComparator;
import cn.zpl.common.bean.QueryDTO;
import cn.zpl.common.bean.RestResponse;
import cn.zpl.service.ReadLocalService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
import java.nio.charset.StandardCharsets;
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
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class ReadController {

    public static LoadingCache<String, Map<String, Map<Integer, List<String>>>> cache;
    public static LoadingCache<String, byte[]> imageCache;

    private static DiskImageCache diskImageCache;

    @Resource
    private ReadLocalService readLocalService;

    @Value("${comic.cover.cache.path:./cover-cache}")
    private String coverCachePath;

    @Value("${comic.image.cache.enabled:false}")
    private boolean useDiskCache;

    @Value("${comic.image.cache.path:./image-cache}")
    private String imageCachePath;

    @Value("${comic.image.cache.max-disk:5000}")
    private int imageCacheMaxDisk;

    /** 是否启用磁盘保活功能（防止磁盘自动休眠） */
    @Value("${comic.disk.keepalive.enabled:false}")
    private boolean keepaliveEnabled;

    private ScheduledExecutorService keepaliveScheduler;
    private final Random random = new Random();

    @PostConstruct
    public void init() {
        if (useDiskCache) {
            diskImageCache = new DiskImageCache(100, imageCacheMaxDisk, imageCachePath);
            log.info("启用磁盘图片缓存模式，路径: {}，最大文件数: {}", imageCachePath, imageCacheMaxDisk);
        } else if (keepaliveEnabled) {
            // 保活功能需要DiskImageCache来写入保活文件，即使图片缓存未启用也要初始化
            diskImageCache = new DiskImageCache(10, 100, imageCachePath);
            log.info("图片缓存未启用，但保活功能需要磁盘缓存，已初始化轻量级DiskImageCache");
        } else {
            log.info("使用原始内存图片缓存模式");
        }

        if (keepaliveEnabled) {
            startKeepalive();
        }
    }

    @PreDestroy
    public void destroy() {
        if (keepaliveScheduler != null && !keepaliveScheduler.isShutdown()) {
            keepaliveScheduler.shutdownNow();
            log.info("保活定时任务已停止");
        }
        if (useDiskCache && diskImageCache != null) {
            diskImageCache.invalidateAll();
        }
    }

    @GetMapping("/rescan")
    @ResponseBody
    public RestResponse rescan() {
        try {
            if (readLocalService instanceof cn.zpl.service.impl.ReadLocalServiceImpl) {
                ((cn.zpl.service.impl.ReadLocalServiceImpl) readLocalService).rescanFolder();
                return RestResponse.ok("重新扫描完成");
            } else {
                return RestResponse.fail("不支持重新扫描操作");
            }
        } catch (Exception e) {
            log.error("重新扫描失败", e);
            return RestResponse.fail("重新扫描失败: " + e.getMessage());
        }
    }

    @RequestMapping("/search")
    public String search(@CookieValue(value = "userToken", required = false) Cookie cookie, HttpServletResponse response) {
        if (cookie != null) {
            String value = cookie.getValue();
        }
        String uuid = UUID.randomUUID().toString();
        Cookie newCookie = new Cookie("userToken", uuid);
        newCookie.setPath("/");
        log.info("search设置cookie:{}", uuid);
        response.addCookie(newCookie);
        return "search";
    }

    @RequestMapping("/getList")
    public String getList(@CookieValue(value = "userToken", required = false) Cookie cookie,
                         HttpSession session, Model model, QueryDTO queryDTO, 
                         @RequestParam(value = "scanPath", required = false) String scanPath,
                         HttpServletResponse response) {
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
            log.info("getList设置cookie:{}", uuid);
            response.addCookie(newCookie);
        } else {
            uuid = cookie.getValue();
        }

        queryDTO.setTitle(queryDTO.getTitle().toLowerCase());
        
        RestResponse restResponse;
        if (scanPath != null && !scanPath.trim().isEmpty()) {
            log.info("使用自定义扫描路径: {}", scanPath);
            session.setAttribute("scanPath", scanPath);
            if (StringUtils.isEmpty(queryDTO.getTitle())) {
                restResponse = getListByPath(queryDTO, scanPath);
            } else {
                restResponse = getListWithCustomPath(queryDTO, scanPath);
            }
        } else {
            restResponse = getList(queryDTO);
        }
        
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
        List<Ehentai> ehentais = readLocalService.searchComics(query);
        return RestResponse.ok(ehentais);
    }

    public RestResponse getListWithCustomPath(@RequestBody QueryDTO query, String customPath) {
        List<Ehentai> ehentais = readLocalService.searchComicsWithCustomPath(query, customPath);
        return RestResponse.ok(ehentais);
    }

    public RestResponse getListByPath(@RequestBody QueryDTO query, String scanPath) {
        if (readLocalService instanceof cn.zpl.service.impl.ReadLocalServiceImpl) {
            cn.zpl.service.impl.ReadLocalServiceImpl impl = (cn.zpl.service.impl.ReadLocalServiceImpl) readLocalService;
            impl.searchComicsWithCustomPath(query, scanPath);
            List<Ehentai> ehentais = impl.searchComicsByPath(query, scanPath.trim());
            return RestResponse.ok(ehentais);
        }
        return getListWithCustomPath(query, scanPath);
    }

    @GetMapping("/clearCache/{comicId}")
    public String clearCache(@PathVariable("comicId") String comicId) {
        if (cache != null) {
            Ehentai ehentai = readLocalService.getEh(comicId);
            cache.invalidate(ehentai.getSavePath());
        }
        if (useDiskCache && diskImageCache != null) {
            diskImageCache.invalidateByPrefix(comicId + "#");
        } else if (imageCache != null) {
            List<String> collect = imageCache.asMap().keySet().stream()
                .filter(bytes -> bytes.startsWith(comicId)).collect(Collectors.toList());
            collect.forEach(s -> imageCache.invalidate(s));
        }
        return "redirect:/comic/" + comicId;
    }

    @RequestMapping("/comic/{id}")
    public String getChapters(@PathVariable("id") String id, Model model) {
        Ehentai ehentai = readLocalService.getEh(id);
        File file = new File(ehentai.getSavePath());
        if (!file.exists()) {
            return "search";
        }
        List<String> chapters = getChapters(ehentai.getSavePath());
        model.addAttribute("fileName", file.getName());
        model.addAttribute("chapters", chapters);
        model.addAttribute("id", id);
        return "chapter";
    }

    private void loadImageCache(String comicId, List<String> chapters) {
        chapters.parallelStream().forEach(chapter -> {
            List<String> images = getImagesInChapter(comicId, chapter);
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
                int num1 = Integer.parseInt(s1);
                int num2 = Integer.parseInt(s2);
                return Integer.compare(num1, num2);
            });
            return chapters;
        } else {
            synchronized (ReadController.class) {
                if (cache == null) {
                    cache = CacheBuilder.newBuilder()
                        .maximumSize(200000)
                        .expireAfterWrite(2000, TimeUnit.HOURS)
                        .build(new CacheLoader<String, Map<String, Map<Integer, List<String>>>>() {
                            @Override
                            public @NotNull Map<String, Map<Integer, List<String>>> load(@NotNull String key) {
                                Map<String, Map<Integer, List<String>>> resultMap = new HashMap<>();
                                File zip = new File(key);
                                try (ZipFile zipFile = new ZipFile(zip)) {
                                    List<ZipArchiveEntry> sortedEntries = Collections.list(zipFile.getEntries());
                                    List<String> paths = sortedEntries.stream()
                                        .map(ZipArchiveEntry::getName)
                                        .collect(Collectors.toList());
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
            }
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
            new Thread(() -> getImage(id, num, "ALL")).start();
        }
        model.addAttribute("imgs", list);
        model.addAttribute("id", id);
        model.addAttribute("num", num);
        return "read2";
    }

    @ResponseBody
    @GetMapping("/read")
    public void read(@RequestParam("id") String id, @RequestParam("chapter") String num,
                    @RequestParam("img") String img, HttpServletResponse response) {
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
        Ehentai ehentai = readLocalService.getEh(id);
        if (StringUtils.isEmpty(ehentai.getSavePath())) {
            readLocalService.invalidCache(id);
            ehentai = readLocalService.getEh(id);
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

    @ResponseBody
    @GetMapping("/getCover")
    public void getCover(@RequestParam("id") String id, HttpServletResponse response) {
        Ehentai ehentai = readLocalService.getEh(id);
        if (StringUtils.isEmpty(ehentai.getSavePath())) {
            readLocalService.invalidCache(id);
            ehentai = readLocalService.getEh(id);
        }
        byte[] image = new byte[0];
        OutputStream outputStream;
        Path cover = Paths.get(coverCachePath, id);
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
        Ehentai ehentai = readLocalService.getEh(id);
        List<String> chapters = getChapters(ehentai.getSavePath());
        if (chapters.contains(child)) {
            return new File(ehentai.getSavePath(), child);
        } else {
            return null;
        }
    }

    private List<String> getImagesInChapter(String id, String chapter) {
        Ehentai ehentai = readLocalService.getEh(id);
        Map<String, Map<Integer, List<String>>> zipInfo = getZipInfo(ehentai.getSavePath());
        if (zipInfo == null || zipInfo.isEmpty()) {
            return null;
        }
        Optional<String> folder = zipInfo.keySet().stream().findFirst();
        Set<Map.Entry<Integer, List<String>>> entries = zipInfo.get(folder.get()).entrySet();
        for (Map.Entry<Integer, List<String>> entry : entries) {
            if (String.valueOf(entry.getKey()).equalsIgnoreCase(chapter)) {
                return new ArrayList<>(entry.getValue());
            }
        }
        return null;
    }

    private byte[] getImage(String comicId, String chapter, String image) {
        if (useDiskCache) {
            return getImageFromDiskCache(comicId, chapter, image);
        }
        String myKey = comicId + "#" + chapter + "#" + image;
        if (imageCache != null) {
            byte[] ifPresent = imageCache.getIfPresent(myKey);
            if (ifPresent == null || ifPresent.length == 0) {
                try {
                    ifPresent = imageCache.get(myKey);
                    log.debug("{}从压缩包读取", myKey);
                    return ifPresent;
                } catch (Exception e) {
                    log.error("压缩包id:{}读取失败", comicId, e);
                    return null;
                }
            } else {
                log.debug("{}缓存命中，直接返回", myKey);
                return ifPresent;
            }
        } else {
            synchronized (ReadController.class) {
                if (imageCache == null) {
                    imageCache = CacheBuilder.newBuilder()
                        .maximumSize(500000)
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .build(new CacheLoader<String, byte[]>() {
                            @Override
                            public byte @NotNull [] load(@NotNull String key) throws Exception {
                                log.debug("开始读取压缩包id:{}", key);
                                String[] split = key.split("#");
                                String comicId = split[0];
                                String chapter = split[1];
                                String image = split[2];
                                Ehentai ehentai = readLocalService.getEh(comicId);
                                Map<String, Map<Integer, List<String>>> zipInfo = getZipInfo(ehentai.getSavePath());
                                if (zipInfo == null || zipInfo.isEmpty()) {
                                    return "NULL".getBytes(StandardCharsets.UTF_8);
                                }
                                try (ZipFile zipFile = new ZipFile(ehentai.getSavePath())) {
                                    if ("ALL".equalsIgnoreCase(image)) {
                                        List<String> imgsList = getImagesInChapter(comicId, chapter);
                                        if (CollectionUtils.isEmpty(imgsList)) {
                                            return "NULL".getBytes(StandardCharsets.UTF_8);
                                        }
                                        imgsList.sort(new ImageComparator());
                                        for (String img : imgsList) {
                                            ZipArchiveEntry entry = zipFile.getEntry(img);
                                            if (entry == null) {
                                                entry = zipFile.getEntry(chapter + "/" + img);
                                            }
                                            InputStream inputStream = zipFile.getInputStream(entry);
                                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                            byte[] buffer = new byte[1024];
                                            int bytesRead;
                                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                                bos.write(buffer, 0, bytesRead);
                                            }
                                            inputStream.close();
                                            bos.close();
                                            String myKey = comicId + "#" + chapter + "#" + img;
                                            imageCache.put(myKey, bos.toByteArray());
                                        }
                                        return "ALL".getBytes(StandardCharsets.UTF_8);
                                    }
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
            }
        }
        return getImage(comicId, chapter, image);
    }

    private byte[] getImageFromDiskCache(String comicId, String chapter, String image) {
        String myKey = comicId + "#" + chapter + "#" + image;
        try {
            byte[] cached = diskImageCache.getIfPresent(myKey);
            if (cached != null && cached.length > 0) {
                log.debug("{}磁盘缓存命中", myKey);
                return cached;
            }

            if ("ALL".equalsIgnoreCase(image)) {
                return loadAllImagesDiskCache(comicId, chapter);
            }

            return diskImageCache.get(myKey, () -> loadSingleImageFromZip(comicId, chapter, image));
        } catch (Exception e) {
            log.error("磁盘缓存模式读取失败，comicId:{}", comicId, e);
            return null;
        }
    }

    private byte[] loadAllImagesDiskCache(String comicId, String chapter) {
        List<String> imgsList = getImagesInChapter(comicId, chapter);
        if (CollectionUtils.isEmpty(imgsList)) {
            return null;
        }
        imgsList.sort(new ImageComparator());
        byte[] lastBytes = null;
        for (String img : imgsList) {
            String key = comicId + "#" + chapter + "#" + img;
            try {
                byte[] data = diskImageCache.get(key, () -> loadSingleImageFromZip(comicId, chapter, img));
                if (data != null) {
                    lastBytes = data;
                }
            } catch (Exception e) {
                log.error("磁盘缓存预加载图片失败: {}", key, e);
            }
        }
        return lastBytes;
    }

    private byte[] loadSingleImageFromZip(String comicId, String chapter, String image) throws Exception {
        Ehentai ehentai = readLocalService.getEh(comicId);
        Map<String, Map<Integer, List<String>>> zipInfo = getZipInfo(ehentai.getSavePath());
        if (zipInfo == null || zipInfo.isEmpty()) {
            return null;
        }
        try (ZipFile zipFile = new ZipFile(ehentai.getSavePath())) {
            ZipArchiveEntry entry = zipFile.getEntry(image);
            if (entry == null) {
                entry = zipFile.getEntry(chapter + "/" + image);
            }
            if (entry == null) {
                return null;
            }
            InputStream inputStream = zipFile.getInputStream(entry);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[20000];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            return bos.toByteArray();
        }
    }

    /**
     * ==================== 磁盘保活功能 ====================
     * 目的：防止存储漫画的磁盘因长时间无I/O操作而自动进入休眠状态。
     * 原理：
     *   1. 每5秒从随机一个已扫描的压缩包中读取2张图片，写入缓存目录作为保活文件
     *      —— 读取压缩包会产生真实的磁盘I/O，阻止磁盘休眠
     *   2. 保活文件以 "keepalive_" 为前缀进行特殊标记
     *   3. 每5分钟清理一次所有保活文件，防止磁盘空间被持续占用
     * 配置项：comic.disk.keepalive.enabled=true 启用
     */
    private void startKeepalive() {
        keepaliveScheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "disk-keepalive");
            t.setDaemon(true);
            return t;
        });

        // 任务1：每5秒读取2张随机图片写入保活文件（产生磁盘I/O）
        keepaliveScheduler.scheduleAtFixedRate(this::keepaliveTick, 5, 5, TimeUnit.SECONDS);

        // 任务2：每5分钟清理所有保活文件（释放磁盘空间）
        keepaliveScheduler.scheduleAtFixedRate(() -> {
            try {
                if (diskImageCache != null) {
                    diskImageCache.cleanKeepalive();
                }
            } catch (Exception e) {
                log.warn("保活文件清理任务异常", e);
            }
        }, 5, 5, TimeUnit.MINUTES);

        log.info("磁盘保活功能已启动：每5秒读取随机图片，每5分钟清理保活文件");
    }

    /**
     * 单次保活操作：从随机漫画的压缩包中读取2张图片，写入保活文件。
     * 读取压缩包中的图片会产生真实的磁盘I/O，从而阻止磁盘进入休眠。
     */
    private void keepaliveTick() {
        try {
            if (!keepaliveEnabled || diskImageCache == null) {
                return;
            }

            List<String> comicIds = readLocalService.getAllComicIds();
            if (comicIds.isEmpty()) {
                log.debug("保活：无已扫描漫画，跳过");
                return;
            }

            // 随机选取一个漫画
            String comicId = comicIds.get(random.nextInt(comicIds.size()));
            Ehentai ehentai = readLocalService.getEh(comicId);
            if (ehentai == null || ehentai.getSavePath() == null) {
                return;
            }

            File zipFile = new File(ehentai.getSavePath());
            if (!zipFile.exists()) {
                return;
            }

            // 获取该漫画的章节图片列表
            List<String> images = getImagesInChapter(comicId, "1");
            if (images == null || images.size() < 2) {
                return;
            }

            // 随机选取2张图片
            int idx1 = random.nextInt(images.size());
            int idx2 = random.nextInt(images.size());
            String img1 = images.get(idx1);
            String img2 = images.get(idx2);

            // 从压缩包中读取图片并写入保活文件
            byte[] data1 = loadSingleImageFromZip(comicId, "1", img1);
            diskImageCache.writeKeepalive(comicId, data1);

            byte[] data2 = loadSingleImageFromZip(comicId, "1", img2);
            diskImageCache.writeKeepalive(comicId, data2);

            log.debug("保活：从漫画 {} 读取了2张随机图片", ehentai.getTitle());
        } catch (Exception e) {
            log.warn("保活操作异常", e);
        }
    }
}
