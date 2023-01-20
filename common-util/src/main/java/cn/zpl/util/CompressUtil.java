package cn.zpl.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * zip压缩解压工具类
 *
 * @author liuxb
 * @date 2022/7/22 8:24
 */
@Slf4j
public class CompressUtil {
    /**
     * 压缩文件，支持中文，多个文件压缩
     *
     * @param files
     * @param zipFilePath
     */
    public static void compress(File[] files, String zipFilePath) {
        if (files != null && files.length > 0) {
            File zipFile = new File(zipFilePath);
            log.info("{}  开始压缩...", zipFilePath);
            try (ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(zipFile)) {
                zaos.setUseZip64(Zip64Mode.AsNeeded);
                for (File file : files) {
                    if (file != null) {
                        ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(file, file.getName());
                        zaos.putArchiveEntry(zipArchiveEntry);
                    }
                    try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
                        byte[] buffer = new byte[1024 * 10];
                        int len = -1;
                        while ((len = in.read(buffer)) != -1) {
                            zaos.write(buffer, 0, len);
                        }
                    }
                }
                log.info("{}  压缩完成...", zipFilePath);
                zaos.closeArchiveEntry();
                zaos.finish();
            } catch (IOException e) {
                throw new RuntimeException("压缩异常", e);
            }
        }
    }

    /**
     * 解压
     *
     * @param zipFilePath
     */
    public static void decompress(String zipFilePath) {
        decompress(zipFilePath, zipFilePath.replace(".zip", ""));
    }


    /**
     * 解压
     *
     * @param zipFilePath
     * @param descDir
     */
    public static void decompress(String zipFilePath, String descDir) {
        File zipFile = new File(zipFilePath);
        File pathFile = new File(descDir);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
        log.info("{}  开始解压...", zipFilePath);
        try (ZipFile zip = new ZipFile(zipFile)) {
            Enumeration<ZipArchiveEntry> entries = zip.getEntries();
            while (entries.hasMoreElements()) {
                // 遍历获取压缩文件内全部条目，包括子条目中的条目
                ZipArchiveEntry entry = entries.nextElement();
                String entryName = entry.getName();
                try (InputStream in = zip.getInputStream(entry)) {
                    String outPath = (descDir + "/" + entryName);
                    // 判断路径是否存在，不存在则创建文件路径
                    File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
                    if (!file.exists()) {
                        file.mkdirs();
                    }                // 判断文件全路径是否为文件夹,如果是上面已经创建,不需要解压
                    if (new File(outPath).isDirectory()) {
                        continue;
                    }
                    try (OutputStream out = new FileOutputStream(outPath)) {
                        byte[] buf = new byte[4 * 1024];
                        int len = 0;
                        while ((len = in.read(buf)) >= 0) {
                            out.write(buf, 0, len);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("解压失败", e);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("解压失败", e);
                }
            }
            log.info("{}  解压完毕...", zipFilePath);
        } catch (Exception e) {
            throw new RuntimeException("解压失败", e);
        }
    }

    /**
     * 删除文件夹 * * @param dirPath 文件夹路径及名称 如c:/test
     */
    public static void delFolder(String dirPath) {
        try {
            delAllFile(dirPath);
            //删除完里面所有内容
            log.info("删除{}内所有文件及子目录文件", dirPath);
            File myFilePath = new File(dirPath);
            myFilePath.delete();
            //删除空文件夹
            log.info("删除目录: {}", dirPath);
        } catch (Exception e) {
            log.error("删除文件夹fail", e);
        }
    }

    /**
     * 删除文件夹里面的所有文件 (不删除最外层目录)
     *
     * @param path 文件夹路径 如 c:/test/
     */
    public static void delAllFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        if (!file.isDirectory()) {
            return;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                //先删除文件夹里面的文件
                delAllFile(path + "/" + tempList[i]);
                //再删除空文件夹
                delFolder(path + "/" + tempList[i]);
            }
        }
    }

    /**
     * 获取给定路径内所有是文件的绝地地址列表
     *
     * @param dir 路径
     * @return 遍历的路径集合
     */
    public static List<String> getFiles(String dir) {
        List<String> lstFiles = new ArrayList<String>();
        File file = new File(dir);
        if (!file.exists()) {
            return lstFiles;
        }
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                lstFiles.addAll(getFiles(f.getAbsolutePath()));
            } else {
                lstFiles.add(f.getAbsolutePath());
            }
        }
        return lstFiles;
    }
}
