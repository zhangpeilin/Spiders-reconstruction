package cn.zpl.spider.on.ehentai.util;

import cn.zpl.common.bean.Ehentai;
import cn.zpl.spider.on.ehentai.config.EhentaiConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * EUtil 本地 JSON 存储读写验证
 * <p>
 * 覆盖：saveEh 写入文件、getEh 从文件加载、模拟重启后的重新加载
 */
public class EUtilTest {

    private Path tempDir;
    private EUtil util;

    @BeforeEach
    public void setUp() throws Exception {
        tempDir = Files.createTempDirectory("ehentai-local-test");
        EhentaiConfig config = new EhentaiConfig();
        config.setSavePath(tempDir.toString());
        util = new EUtil();
        injectConfig(util, config);
        // 重置静态状态，保证测试独立
        resetStaticState();
    }

    @AfterEach
    public void tearDown() throws Exception {
        resetStaticState();
        deleteRecursively(tempDir.toFile());
    }

    @Test
    public void testSaveAndGet() {
        Ehentai eh = new Ehentai();
        eh.setId("1001");
        eh.setTitle("测试画廊");
        eh.setUrl("https://e-hentai.org/g/1001/x/");
        eh.setFinish(0);
        util.saveEh(eh);

        Ehentai loaded = util.getEh("1001");
        Assertions.assertNotNull(loaded, "保存后应能读取");
        Assertions.assertEquals(loaded.getTitle(), "测试画廊", "标题应一致");
        Assertions.assertEquals(loaded.getFinish(), 0, "finish 应一致");

        File storeFile = new File(tempDir.toFile(), "meta/ehentai.json");
        Assertions.assertTrue(storeFile.exists(), "元数据文件应存在");
    }

    @Test
    public void testReloadFromFile() {
        Ehentai eh = new Ehentai();
        eh.setId("1002");
        eh.setTitle("重启后仍可读");
        eh.setFinish(1);
        eh.setSavePath("E:\\archive\\1002.zip");
        util.saveEh(eh);

        // 模拟应用重启：重置静态缓存状态，重新 new EUtil 加载
        resetStaticState();
        EUtil util2 = new EUtil();
        injectConfig(util2, tempDir.toString());

        Ehentai reloaded = util2.getEh("1002");
        Assertions.assertNotNull(reloaded, "重启后应能从文件加载");
        Assertions.assertEquals(reloaded.getTitle(), "重启后仍可读", "标题应一致");
        Assertions.assertEquals(reloaded.getSavePath(), "E:\\archive\\1002.zip", "savePath 应一致");
        Assertions.assertEquals(reloaded.getFinish(), 1, "finish 应一致");
    }

    @Test
    public void testUpdateExisting() {
        Ehentai eh = new Ehentai();
        eh.setId("1003");
        eh.setTitle("初次保存");
        eh.setFinish(0);
        util.saveEh(eh);

        eh.setFinish(1);
        eh.setSavePath("E:\\archive\\1003.zip");
        util.saveEh(eh);

        Ehentai loaded = util.getEh("1003");
        Assertions.assertEquals(loaded.getFinish(), 1, "更新后 finish 应为 1");
        Assertions.assertEquals(loaded.getSavePath(), "E:\\archive\\1003.zip", "更新后 savePath 应写入");
    }

    @Test
    public void testGetGalleryId() {
        Assertions.assertEquals("1001", EUtil.getGalleryId("https://e-hentai.org/g/1001/x/abc/"));
        Assertions.assertNull(EUtil.getGalleryId("https://e-hentai.org/gallery/12345/"));
    }

    private void injectConfig(EUtil target, String savePath) {
        EhentaiConfig config = new EhentaiConfig();
        config.setSavePath(savePath);
        injectConfig(target, config);
    }

    private void injectConfig(EUtil target, EhentaiConfig config) {
        try {
            Field field = EUtil.class.getDeclaredField("ehentaiConfig");
            field.setAccessible(true);
            field.set(target, config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void resetStaticState() {
        try {
            Field loaded = EUtil.class.getDeclaredField("loaded");
            loaded.setAccessible(true);
            loaded.set(null, false);
            Field storeFile = EUtil.class.getDeclaredField("storeFile");
            storeFile.setAccessible(true);
            storeFile.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteRecursively(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        File[] files = file.listFiles();
        if (files != null) {
            for (File child : files) {
                deleteRecursively(child);
            }
        }
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }
}
