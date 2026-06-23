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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReadLocalServiceImpl implements ReadLocalService {

    @Value("${comic.folder.path:./comics}")
    private String comicFolderPath;

    private final ConcurrentHashMap<String, Ehentai> comicCache = new ConcurrentHashMap<>();

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

        scanZipFiles(folder);
    }

    private void scanZipFiles(File directory) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                scanZipFiles(file);
            } else if (file.isFile() && file.getName().toLowerCase().endsWith(".zip")) {
                String id = generateUniqueIdFromFile(file);
                Ehentai ehentai = createEhentaiFromFile(file, id);
                comicCache.put(id, ehentai);
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
            return allComics;
        }

        String titleKeyword = queryDTO.getTitle();
        String authorKeyword = queryDTO.getAuthor();

        return allComics.stream()
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
        log.info("重新扫描漫画文件夹");
        comicCache.clear();
        scanComicFolder();
        log.info("重新扫描完成，共找到 {} 个漫画文件", comicCache.size());
    }

    public Map<String, Ehentai> getAllComics() {
        return new ConcurrentHashMap<>(comicCache);
    }
}
