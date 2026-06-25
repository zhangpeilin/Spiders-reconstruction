package cn.zpl.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class DiskImageCache {

    private final Cache<String, byte[]> memoryCache;
    private final Path cacheDir;
    private final int maxDiskSize;
    private final ConcurrentHashMap<String, Object> writeLocks = new ConcurrentHashMap<>();
    private volatile long lastEvictTime = 0;
    private static final long EVICT_INTERVAL_MS = 60_000;

    public DiskImageCache(int maxMemoryEntries, int maxDiskSize, String cacheDirPath) {
        this.memoryCache = CacheBuilder.newBuilder()
                .maximumSize(maxMemoryEntries)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build();
        this.maxDiskSize = maxDiskSize;
        this.cacheDir = Paths.get(cacheDirPath);
        try {
            Files.createDirectories(this.cacheDir);
            log.info("磁盘图片缓存初始化完成，目录: {}，最大文件数: {}", cacheDirPath, maxDiskSize);
        } catch (IOException e) {
            log.error("创建图片磁盘缓存目录失败: {}", cacheDirPath, e);
        }
    }

    public byte[] get(String key, Callable<byte[]> loader) throws Exception {
        byte[] cached = memoryCache.getIfPresent(key);
        if (cached != null && cached.length > 0) {
            return cached;
        }

        byte[] fromDisk = readFromDisk(key);
        if (fromDisk != null) {
            memoryCache.put(key, fromDisk);
            return fromDisk;
        }

        byte[] result = loader.call();
        if (result != null && result.length > 0) {
            memoryCache.put(key, result);
            writeToDisk(key, result);
        }
        return result;
    }

    public byte[] getIfPresent(String key) {
        byte[] cached = memoryCache.getIfPresent(key);
        if (cached != null && cached.length > 0) {
            return cached;
        }
        return readFromDisk(key);
    }

    public void put(String key, byte[] data) {
        if (data == null || data.length == 0) return;
        memoryCache.put(key, data);
        writeToDisk(key, data);
    }

    public void invalidateByPrefix(String prefix) {
        memoryCache.asMap().keySet().removeIf(k -> k.startsWith(prefix));
        String sanitizedPrefix = sanitizeFilename(prefix);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(cacheDir)) {
            for (Path file : stream) {
                if (Files.isRegularFile(file) && file.getFileName().toString().startsWith(sanitizedPrefix)) {
                    Files.deleteIfExists(file);
                }
            }
        } catch (IOException e) {
            log.warn("按前缀清理磁盘缓存失败: {}", prefix, e);
        }
    }

    public void invalidateAll() {
        memoryCache.invalidateAll();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(cacheDir)) {
            for (Path file : stream) {
                if (Files.isRegularFile(file)) {
                    Files.deleteIfExists(file);
                }
            }
        } catch (IOException e) {
            log.warn("清理磁盘缓存目录失败", e);
        }
    }

    private byte[] readFromDisk(String key) {
        Path file = cacheDir.resolve(sanitizeFilename(key));
        if (Files.exists(file)) {
            try {
                byte[] data = Files.readAllBytes(file);
                Files.setLastModifiedTime(file, FileTime.fromMillis(System.currentTimeMillis()));
                return data;
            } catch (IOException e) {
                log.warn("从磁盘缓存读取失败: {}", key, e);
            }
        }
        return null;
    }

    private void writeToDisk(String key, byte[] data) {
        Path file = cacheDir.resolve(sanitizeFilename(key));
        Path tempFile = cacheDir.resolve(sanitizeFilename(key) + ".tmp");
        Object lock = writeLocks.computeIfAbsent(key, k -> new Object());
        synchronized (lock) {
            try {
                Files.write(tempFile, data);
                Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                log.warn("写入磁盘缓存失败: {}", key, e);
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
            } finally {
                writeLocks.remove(key);
            }
        }
        long now = System.currentTimeMillis();
        if (now - lastEvictTime > EVICT_INTERVAL_MS) {
            lastEvictTime = now;
            evictDiskIfNeeded();
        }
    }

    private void evictDiskIfNeeded() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(cacheDir)) {
            List<Path> files = new ArrayList<>();
            for (Path p : stream) {
                if (Files.isRegularFile(p) && !p.getFileName().toString().endsWith(".tmp")) {
                    files.add(p);
                }
            }
            if (files.size() <= maxDiskSize) return;

            int toDelete = files.size() - maxDiskSize + (maxDiskSize / 10);
            files.sort(Comparator.comparingLong(p -> {
                try {
                    return Files.getLastModifiedTime(p).toMillis();
                } catch (IOException e) {
                    return 0L;
                }
            }));

            for (int i = 0; i < toDelete && i < files.size(); i++) {
                try {
                    Files.deleteIfExists(files.get(i));
                } catch (IOException e) {
                    log.debug("淘汰文件失败(可能正在被使用): {}", files.get(i));
                }
            }
            log.info("磁盘缓存淘汰完成，当前文件数: {}", files.size() - toDelete);
        } catch (IOException e) {
            log.warn("磁盘缓存LRU淘汰失败", e);
        }
    }

    private String sanitizeFilename(String key) {
        return key.replaceAll("[^a-zA-Z0-9_\\-.]", "_");
    }

    /**
     * 写入一个保活标记文件，用于防止磁盘自动休眠。
     * 保活文件内容来自真实压缩包中的图片数据，产生实际磁盘I/O。
     * 所有保活文件以 "keepalive_" 为前缀，便于统一清理。
     *
     * @param comicId   漫画ID（用于生成唯一文件名）
     * @param imageData 从压缩包中读取的图片二进制数据
     */
    public void writeKeepalive(String comicId, byte[] imageData) {
        if (imageData == null || imageData.length == 0) return;
        String fileName = "keepalive_" + comicId + "_" + System.nanoTime();
        Path file = cacheDir.resolve(fileName);
        try {
            Files.write(file, imageData);
        } catch (IOException e) {
            log.warn("写入保活文件失败: {}", fileName, e);
        }
    }

    /**
     * 清理所有保活标记文件。
     * 定时调用，防止保活文件长期堆积占用磁盘空间。
     */
    public void cleanKeepalive() {
        int deleted = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(cacheDir, "keepalive_*")) {
            for (Path file : stream) {
                if (Files.isRegularFile(file)) {
                    Files.deleteIfExists(file);
                    deleted++;
                }
            }
        } catch (IOException e) {
            log.warn("清理保活文件失败", e);
        }
        if (deleted > 0) {
            log.debug("已清理 {} 个保活文件", deleted);
        }
    }
}
