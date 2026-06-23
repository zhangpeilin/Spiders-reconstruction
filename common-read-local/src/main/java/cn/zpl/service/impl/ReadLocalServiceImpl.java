package cn.zpl.service.impl;

import cn.zpl.common.bean.Ehentai;
import cn.zpl.common.bean.QueryDTO;
import cn.zpl.service.ReadLocalService;
import com.github.houbb.opencc4j.util.ZhConverterUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReadLocalServiceImpl implements ReadLocalService {

    @Value("${comic.folder.path:./comics}")
    private String comicFolderPath;

    private final ConcurrentHashMap<String, Ehentai> comicCache = new ConcurrentHashMap<>();
    
    private final Set<String> scannedPaths = ConcurrentHashMap.newKeySet();

    private final ConcurrentHashMap<String, Set<String>> pathToComicIds = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("开始扫描漫画文件夹: {}", comicFolderPath);
        scanComicFolder();
        log.info("扫描完成，共找到 {} 个漫画文件", comicCache.size());
    }

    private void scanComicFolder() {
        File folder = new File(comicFolderPath);
        if (!folder.exists()) {
            log.warn("漫画文件夹不存在，创建默认路径: {}", comicFolderPath);
            folder.mkdirs();
            return;
        }

        Set<String> ids = new HashSet<>();
        scanZipFiles(folder, ids);
        scannedPaths.add(folder.getAbsolutePath());
        pathToComicIds.put(folder.getAbsolutePath(), ids);
        log.info("已记录扫描路径: {}", folder.getAbsolutePath());
    }

    private void scanZipFiles(File directory) {
        scanZipFiles(directory, null);
    }

    private void scanZipFiles(File directory, Set<String> collectedIds) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                scanZipFiles(file, collectedIds);
            } else if (file.isFile() && file.getName().toLowerCase().endsWith(".zip")) {
                String id = generateUniqueIdFromFile(file);
                Ehentai ehentai = createEhentaiFromFile(file, id);
                comicCache.put(id, ehentai);
                if (collectedIds != null) {
                    collectedIds.add(id);
                }
                log.debug("加载漫画: {} -> {}", id, file.getAbsolutePath());
            }
        }
    }

    private String generateUniqueIdFromFile(File file) {
        String absolutePath = file.getAbsolutePath();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(absolutePath.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5算法不可用", e);
            return String.valueOf(Math.abs(absolutePath.hashCode()));
        }
    }

    private Ehentai createEhentaiFromFile(File file, String id) {
        Ehentai ehentai = new Ehentai();
        ehentai.setId(id);
        ehentai.setTitle(file.getName().replaceAll("\\.zip$", ""));
        ehentai.setSavePath(file.getAbsolutePath());
        ehentai.setArtist("Unknown");
        ehentai.setFavcount("0");
        ehentai.setSize(file.length());
        return ehentai;
    }

    @Override
    public String convertToTraditionalChinese(String simplifiedChinese) {
        return ZhConverterUtil.toTraditional(simplifiedChinese);
    }

    @Override
    public Ehentai getEh(String id) {
        return comicCache.get(id);
    }

    @Override
    public void invalidCache(String comicId) {
        Ehentai removed = comicCache.remove(comicId);
        if (removed != null) {
            log.info("已清除缓存: {}", comicId);
        }
    }

    @Override
    public List<Ehentai> queryBySql(String sql) {
        return new ArrayList<>(comicCache.values());
    }

    public List<Ehentai> searchComics(QueryDTO queryDTO) {
        List<Ehentai> allComics = new ArrayList<>(comicCache.values());
        
        if (queryDTO == null) {
            return applySizeLimit(allComics, null);
        }

        String titleKeyword = queryDTO.getTitle();
        String authorKeyword = queryDTO.getAuthor();

        List<Ehentai> filteredComics = allComics.stream()
            .filter(comic -> {
                boolean titleMatch = true;
                boolean authorMatch = true;

                if (!StringUtils.isEmpty(titleKeyword)) {
                    titleMatch = matchWithTraditionalSimplified(comic.getTitle(), titleKeyword);
                }

                if (!StringUtils.isEmpty(authorKeyword)) {
                    authorMatch = matchWithTraditionalSimplified(comic.getArtist(), authorKeyword);
                }

                return titleMatch && authorMatch;
            })
            .collect(Collectors.toList());
        
        return applySizeLimit(filteredComics, queryDTO.getSize());
    }

    private List<Ehentai> applySizeLimit(List<Ehentai> comics, Integer size) {
        if (size == null || size <= 0 || size >= comics.size()) {
            return comics;
        }
        
        if (size > 999999) {
            log.debug("查询全部数据，不进行截断");
            return comics;
        }
        
        log.debug("应用大小限制: {}，原始数量: {}，截断后数量: {}", size, comics.size(), Math.min(size, comics.size()));
        return comics.subList(0, size);
    }

    private boolean matchWithTraditionalSimplified(String text, String keyword) {
        if (text == null) {
            return false;
        }
        
        String textLower = text.toLowerCase();
        String keywordLower = keyword.toLowerCase();
        
        if (textLower.contains(keywordLower)) {
            return true;
        }
        
        String textTraditional = ZhConverterUtil.toTraditional(textLower);
        String textSimplified = ZhConverterUtil.toSimple(textLower);
        String keywordTraditional = ZhConverterUtil.toTraditional(keywordLower);
        String keywordSimplified = ZhConverterUtil.toSimple(keywordLower);
        
        if (textTraditional.contains(keywordTraditional) ||
            textTraditional.contains(keywordSimplified) ||
            textSimplified.contains(keywordTraditional) ||
            textSimplified.contains(keywordSimplified)) {
            return true;
        }
        
        String normalizedText = normalizeJapaneseChinese(textLower);
        String normalizedKeyword = normalizeJapaneseChinese(keywordLower);
        
        if (!normalizedText.equals(textLower) || !normalizedKeyword.equals(keywordLower)) {
            if (normalizedText.contains(normalizedKeyword)) {
                return true;
            }
            
            String normalizedTextTraditional = ZhConverterUtil.toTraditional(normalizedText);
            String normalizedTextSimple = ZhConverterUtil.toSimple(normalizedText);
            String normalizedKeywordTraditional = ZhConverterUtil.toTraditional(normalizedKeyword);
            String normalizedKeywordSimple = ZhConverterUtil.toSimple(normalizedKeyword);
            
            return normalizedTextTraditional.contains(normalizedKeywordTraditional) ||
                   normalizedTextTraditional.contains(normalizedKeywordSimple) ||
                   normalizedTextSimple.contains(normalizedKeywordTraditional) ||
                   normalizedTextSimple.contains(normalizedKeywordSimple);
        }
        
        return false;
    }

    private String normalizeJapaneseChinese(String text) {
        if (text == null) {
            return text;
        }
        
        String normalized = text;
        
        normalized = normalized.replace("黒", "黑");
        normalized = normalized.replace("転", "转");
        normalized = normalized.replace("訳", "译");
        normalized = normalized.replace("録", "录");
        normalized = normalized.replace("画", "画");
        normalized = normalized.replace("図", "图");
        normalized = normalized.replace("実", "实");
        normalized = normalized.replace("体", "体");
        normalized = normalized.replace("気", "气");
        normalized = normalized.replace("関", "关");
        normalized = normalized.replace("東", "东");
        normalized = normalized.replace("西", "西");
        normalized = normalized.replace("南", "南");
        normalized = normalized.replace("北", "北");
        normalized = normalized.replace("門", "门");
        normalized = normalized.replace("間", "间");
        normalized = normalized.replace("時", "时");
        normalized = normalized.replace("分", "分");
        normalized = normalized.replace("後", "后");
        normalized = normalized.replace("前", "前");
        normalized = normalized.replace("国", "国");
        normalized = normalized.replace("学", "学");
        normalized = normalized.replace("生", "生");
        normalized = normalized.replace("校", "校");
        normalized = normalized.replace("高", "高");
        normalized = normalized.replace("校", "校");
        
        return normalized;
    }

    public void rescanFolder() {
        log.info("重新扫描所有已记录的路径");
        comicCache.clear();
        
        Set<String> pathsToRescan = new HashSet<>(scannedPaths);
        for (String path : pathsToRescan) {
            File folder = new File(path);
            if (folder.exists()) {
                log.info("重新扫描路径: {}", path);
                scanZipFiles(folder);
            } else {
                log.warn("路径不存在，跳过: {}", path);
                scannedPaths.remove(path);
            }
        }
        
        log.info("重新扫描完成，共找到 {} 个漫画文件", comicCache.size());
    }

    public Map<String, Ehentai> getAllComics() {
        return new ConcurrentHashMap<>(comicCache);
    }

    @Override
    public List<Ehentai> searchComicsWithCustomPath(QueryDTO queryDTO, String customPath) {
        if (customPath != null && !customPath.trim().isEmpty()) {
            String trimmedPath = customPath.trim();
            
            if (scannedPaths.contains(trimmedPath)) {
                log.info("路径已扫描过，直接使用缓存: {}", trimmedPath);
            } else {
                log.info("检测到新路径: {}，开始扫描", trimmedPath);
                
                File folder = new File(trimmedPath);
                if (folder.exists()) {
                    Set<String> ids = new HashSet<>();
                    synchronized (comicCache) {
                        scanZipFilesFromPath(folder, comicCache, ids);
                        scannedPaths.add(trimmedPath);
                        pathToComicIds.put(trimmedPath, ids);
                        log.info("新路径扫描完成并添加到记录，当前缓存总数: {}", comicCache.size());
                    }
                } else {
                    log.warn("自定义路径不存在: {}", trimmedPath);
                }
            }
        }
        
        return searchComics(queryDTO);
    }

    public List<Ehentai> searchComicsByPath(QueryDTO queryDTO, String path) {
        Set<String> comicIds = pathToComicIds.get(path);
        if (comicIds == null || comicIds.isEmpty()) {
            log.info("路径 {} 下没有找到任何漫画", path);
            return new ArrayList<>();
        }
        
        List<Ehentai> pathComics = comicIds.stream()
            .map(comicCache::get)
            .filter(e -> e != null)
            .collect(Collectors.toList());
        
        if (queryDTO == null) {
            return applySizeLimit(pathComics, null);
        }
        
        String titleKeyword = queryDTO.getTitle();
        String authorKeyword = queryDTO.getAuthor();
        
        List<Ehentai> filtered = pathComics.stream()
            .filter(comic -> {
                boolean titleMatch = true;
                boolean authorMatch = true;
                
                if (!StringUtils.isEmpty(titleKeyword)) {
                    titleMatch = matchWithTraditionalSimplified(comic.getTitle(), titleKeyword);
                }
                
                if (!StringUtils.isEmpty(authorKeyword)) {
                    authorMatch = matchWithTraditionalSimplified(comic.getArtist(), authorKeyword);
                }
                
                return titleMatch && authorMatch;
            })
            .collect(Collectors.toList());
        
        return applySizeLimit(filtered, queryDTO.getSize());
    }

    private void scanZipFilesFromPath(File directory, ConcurrentHashMap<String, Ehentai> cache, Set<String> collectedIds) {
        if (!directory.exists()) {
            log.warn("路径不存在: {}", directory.getAbsolutePath());
            return;
        }
        
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                scanZipFilesFromPath(file, cache, collectedIds);
            } else if (file.isFile() && file.getName().toLowerCase().endsWith(".zip")) {
                String id = generateUniqueIdFromFile(file);
                
                if (!cache.containsKey(id)) {
                    Ehentai ehentai = createEhentaiFromFile(file, id);
                    cache.put(id, ehentai);
                    log.debug("加载漫画: {} -> {}", id, file.getAbsolutePath());
                } else {
                    log.debug("漫画已存在，跳过: {}", id);
                }
                if (collectedIds != null) {
                    collectedIds.add(id);
                }
            }
        }
    }

    private void scanZipFilesFromPath(File directory, ConcurrentHashMap<String, Ehentai> cache) {
        scanZipFilesFromPath(directory, cache, null);
    }

    private List<Ehentai> filterComics(List<Ehentai> comics, QueryDTO queryDTO) {
        if (queryDTO == null) {
            return comics;
        }

        String titleKeyword = queryDTO.getTitle();
        String authorKeyword = queryDTO.getAuthor();

        return comics.stream()
            .filter(comic -> {
                boolean titleMatch = true;
                boolean authorMatch = true;

                if (!StringUtils.isEmpty(titleKeyword)) {
                    titleMatch = matchWithTraditionalSimplified(comic.getTitle(), titleKeyword);
                }

                if (!StringUtils.isEmpty(authorKeyword)) {
                    authorMatch = matchWithTraditionalSimplified(comic.getArtist(), authorKeyword);
                }

                return titleMatch && authorMatch;
            })
            .collect(Collectors.toList());
    }
}
