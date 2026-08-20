package cn.zpl.spider.on.ehentai.util;

import cn.zpl.common.bean.Ehentai;
import cn.zpl.config.SpringContext;
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
