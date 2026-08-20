package cn.zpl.spider.on.ehentai.util;

import cn.zpl.common.bean.Ehentai;
import cn.zpl.config.SpringContext;
import cn.zpl.pojo.Data;
import cn.zpl.spider.on.ehentai.config.EhentaiConfig;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ehentai 元数据本地存储工具
 * <p>
 * local 模式下不再依赖数据库（common-dao-center），
 * 所有 Ehentai 记录保存在 {savePath}/meta/ehentai.json 中，
 * 启动后懒加载进内存，保存时同步写文件。
 */
@Slf4j
@Component
public class EUtil {

    /**
     * 内存元数据缓存，key 为画廊 id
     */
    private static final Map<String, Ehentai> localStore = new ConcurrentHashMap<>();

    private static final Object storeLock = new Object();

    private static volatile boolean loaded = false;

    private static File storeFile;

    private EhentaiConfig ehentaiConfig;

    /**
     * 兼容非 Spring 注入场景（如 new EUtil()），懒获取配置
     */
    private EhentaiConfig getConfig() {
        if (ehentaiConfig == null) {
            ehentaiConfig = SpringContext.getBeanWithGenerics(EhentaiConfig.class);
        }
        return ehentaiConfig;
    }

    public static String getGalleryId(String url) {
        Pattern pattern = Pattern.compile("/g/(\\d+)/");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public String convertToTraditionalChinese(String simplifiedChinese) {
        return simplifiedChinese;
//        return ZhConverterUtil.toTraditional(simplifiedChinese);
    }

    /**
     * 兼容两种 Cookie 配置格式，避免空头/无冒号行导致 header 解析越界：
     * - "k=v; k2=v2" 浏览器 Cookie 格式 → 走 Data.cookie 字段（按 Cookie 头发送）
     * - "HeaderName: value"（含换行多行）请求头格式 → 走 Data.header 字段
     * - 空配置 → 不设置任何头
     */
    public static void setCookieHeader(Data data, String cookies) {
        if (data == null || cookies == null || cookies.trim().isEmpty()) {
            return;
        }
        String trimmed = cookies.trim();
        boolean headerStyle = trimmed.contains("\n") || trimmed.matches("^[^=;]+:.*");
        if (headerStyle) {
            data.setHeader(trimmed);
        } else {
            data.setCookie(trimmed);
        }
    }

    public Ehentai getEh(String id) {
        ensureLoaded();
        return localStore.get(id);
    }

    /**
     * 保存（新增或更新）一条 ehentai 元数据到本地文件
     */
    public void saveEh(Ehentai ehentai) {
        if (ehentai == null || ehentai.getId() == null) {
            return;
        }
        ensureLoaded();
        synchronized (storeLock) {
            localStore.put(ehentai.getId(), ehentai);
            flush();
        }
    }

    public void invalidCache(String comicId) {
        localStore.remove(comicId);
    }

    /**
     * 列出本地元数据记录，支持关键词模糊匹配（id/title/url），按收藏数降序
     */
    public List<Ehentai> listAll(String keyword, int size) {
        ensureLoaded();
        List<Ehentai> list = new ArrayList<>(localStore.values());
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim().toLowerCase();
            list.removeIf(e -> (e.getId() == null || !e.getId().toLowerCase().contains(kw))
                    && (e.getTitle() == null || !e.getTitle().toLowerCase().contains(kw))
                    && (e.getUrl() == null || !e.getUrl().toLowerCase().contains(kw)));
        }
        list.sort((a, b) -> {
            long fb = 0, fa = 0;
            try {
                fb = a.getFavcount() == null ? 0 : Long.parseLong(a.getFavcount());
                fa = b.getFavcount() == null ? 0 : Long.parseLong(b.getFavcount());
            } catch (NumberFormatException ignored) {
            }
            return Long.compare(fa, fb);
        });
        if (list.size() > size) {
            return list.subList(0, size);
        }
        return list;
    }

    private void ensureLoaded() {
        if (!loaded) {
            synchronized (storeLock) {
                if (!loaded) {
                    loadFromFile();
                    loaded = true;
                }
            }
        }
    }

    private void loadFromFile() {
        File file = getStoreFile();
        if (!file.exists()) {
            return;
        }
        try {
            String json = FileUtils.readFileToString(file, "utf-8");
            List<Ehentai> list = JSON.parseArray(json, Ehentai.class);
            if (list != null && !list.isEmpty()) {
                list.forEach(e -> localStore.put(e.getId(), e));
                log.debug("本地元数据加载完成，共 {} 条", localStore.size());
            }
        } catch (Exception e) {
            log.error("本地元数据加载失败：{}", file.getAbsolutePath(), e);
        }
    }

    private void flush() {
        try {
            FileUtils.writeStringToFile(getStoreFile(), JSON.toJSONString(new ArrayList<>(localStore.values())), "utf-8");
        } catch (Exception e) {
            log.error("本地元数据保存失败：{}", storeFile == null ? "" : storeFile.getAbsolutePath(), e);
        }
    }

    private File getStoreFile() {
        if (storeFile == null) {
            storeFile = new File(getConfig().getSavePath(), "meta" + File.separator + "ehentai.json");
            if (storeFile.getParentFile() != null) {
                storeFile.getParentFile().mkdirs();
            }
        }
        return storeFile;
    }
}
