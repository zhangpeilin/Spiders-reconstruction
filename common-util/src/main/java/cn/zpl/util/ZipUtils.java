package cn.zpl.util;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.changes.ChangeSet;
import org.apache.commons.compress.changes.ChangeSetPerformer;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class ZipUtils {


    public static final ConcurrentHashMap<File, List<FingerPrint>> cache = new ConcurrentHashMap<>();

    public static boolean checkZipSimulate(String file1, String file2) {
        int count = 10;
        //读取压缩包中前10张图片进行比较，如果存在相似度>0.8的，连续3个，判定压缩包为相似
        try (org.apache.commons.compress.archivers.zip.ZipFile zipFile1 = new org.apache.commons.compress.archivers.zip.ZipFile(file1); org.apache.commons.compress.archivers.zip.ZipFile zipFile2 = new org.apache.commons.compress.archivers.zip.ZipFile(file2)) {
            List<FingerPrint> first = cache.get(new File(file1)) != null ? new ArrayList<>(cache.get(new File(file1))) : new ArrayList<>();
            List<FingerPrint> second = cache.get(new File(file2)) != null ? new ArrayList<>(cache.get(new File(file2))) : new ArrayList<>();
            if (first.isEmpty()) {
                first.addAll(getArchive2FingerPrint(zipFile1, count));
                cache.put(new File(file1), first);
            }
            if (second.isEmpty()) {
                second.addAll(getArchive2FingerPrint(zipFile2, count));
                cache.put(new File(file2), second);
            }
            List<FingerPrint> matchList = first.stream().filter(f1 -> {
                if (f1 == null) {
                    return false;
                }
                Optional<FingerPrint> any = second.parallelStream().filter(f2 -> {
                    if (f2 == null) {
                        return false;
                    }
                    return f1.compare(f2) > 0.8;
                }).findAny();
                return any.isPresent();
            }).collect(Collectors.toList());
            System.out.println(matchList.size());
            System.out.println(matchList.size() > first.size() * 0.6);
            //60%图片相似，认为压缩包相同
            return matchList.size() > first.size() * 0.6;
//            List<byte[]> first = new ArrayList<>(getArchive2Byte(zipFile1, 5));
//            List<byte[]> second = new ArrayList<>(getArchive2Byte(zipFile2, 5));
//            List<byte[]> matchList = first.parallelStream().filter(bytes1 -> {
//                Optional<byte[]> any = second.parallelStream().filter(bytes2 -> {
//                    try {
//                        FingerPrint f1 = new FingerPrint(ImageIO.read(new ByteArrayInputStream(bytes1)));
//                        FingerPrint f2 = new FingerPrint(ImageIO.read(new ByteArrayInputStream(bytes2)));
//                        return f1.compare(f2) > 0.8;
//                    } catch (IOException e) {
//                        return false;
//                    }
//                }).findAny();
//                return any.isPresent();
//            }).collect(Collectors.toList());
//            //60%图片相似，认为压缩包相同
//            return matchList.size() > first.size() * 0.6;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
//            throw new RuntimeException(e);
        }
    }

    public static List<FingerPrint> getArchive2FingerPrint(org.apache.commons.compress.archivers.zip.ZipFile zipFile, Integer count) throws IOException {
        Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
        List<byte[]> bytes = new ArrayList<>();
        while (bytes.size() < count && entries.hasMoreElements()) {
            ZipArchiveEntry zipArchiveEntry = entries.nextElement();
            if (!zipArchiveEntry.isDirectory()) {
                InputStream inputStream = zipFile.getInputStream(zipArchiveEntry);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
                inputStream.close();
                bos.close();
                bytes.add(bos.toByteArray());
            }
        }
        return bytes.parallelStream().map(b -> {
            try {
                return new FingerPrint(ImageIO.read(new ByteArrayInputStream(b)));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).collect(Collectors.toList());
    }

    public boolean isZero(String path) {
        try (org.apache.commons.compress.archivers.zip.ZipFile zipFile = new org.apache.commons.compress.archivers.zip.ZipFile(path)) {
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry zipArchiveEntry = entries.nextElement();
                if (!zipArchiveEntry.isDirectory() && !zipArchiveEntry.getName().endsWith(".txt")) {
                    InputStream inputStream = zipFile.getInputStream(zipArchiveEntry);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        bos.write(buffer, 0, bytesRead);
                    }
                    inputStream.close();
                    bos.close();
                    long compressedSize = zipArchiveEntry.getCompressedSize();
                    if (bos.toByteArray().length < compressedSize) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public static byte[] getArchive2Byte(ZipArchiveEntry archiveEntry, org.apache.commons.compress.archivers.zip.ZipFile zipFile) throws IOException {
        InputStream inputStream = zipFile.getInputStream(archiveEntry);
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

    public static List<byte[]> getArchive2Byte(org.apache.commons.compress.archivers.zip.ZipFile zipFile, Integer count) throws IOException {
        Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
        List<byte[]> result = new ArrayList<>();
        while (result.size() < count && entries.hasMoreElements()) {
            ZipArchiveEntry zipArchiveEntry = entries.nextElement();
            if (!zipArchiveEntry.isDirectory()) {
                InputStream inputStream = zipFile.getInputStream(zipArchiveEntry);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
                inputStream.close();
                bos.close();
                result.add(bos.toByteArray());
            }
        }
        return result;
    }

    @SneakyThrows
    public static boolean checkZipStatus(String zipFilePath) {

        boolean result = true;
        //打开压缩包
        org.apache.commons.compress.archivers.zip.ZipFile zipFile;
        try {
            zipFile = new org.apache.commons.compress.archivers.zip.ZipFile(new File(zipFilePath));
//            zipFile = new java.util.zip.ZipFile(zipFilePath);
        } catch (IOException e) {
            //压缩包格式错误
            log.error("{}, The zip file is corrupted", zipFilePath);
            return false;
        }
//遍历每个条目
        Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
        while (entries.hasMoreElements()) {
            ZipArchiveEntry entry = entries.nextElement();
            //获取条目输入流
            InputStream is;
            try {
                is = zipFile.getInputStream(entry);
            } catch (IOException e) {
                //条目无法读取
                zipFile.close();
                log.error("The entry {} is corrupted", entry.getName());
                return false;
            }
            //读取数据并校验
            try {
                byte[] buffer = new byte[1024];
                int len;
                //假设使用CRC32算法计算校验和
                CRC32 crc = new CRC32();
                while ((len = is.read(buffer)) != -1) {
                    crc.update(buffer, 0, len);
                }
                //比较校验和和条目元数据
                if (crc.getValue() != entry.getCrc()) {
                    //校验和不一致
                    log.error("校验和不一致，The entry {} is corrupted", entry.getName());
                    result = false;
                    return false;
                }
            } catch (IOException e) {
                //数据读取错误
                log.error("数据读取错误，The entry {} is corrupted", entry.getName());
                result = false;
                return false;
            } finally {
                //关闭输入流
                try {
                    is.close();
                    if (!result) {
                        zipFile.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
//关闭压缩包
        try {
            zipFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static Path unzipFileWithCorruptErr(String zipFilePath, String outputFolder) throws IOException {
        Path unzipFolder = null;
        List<File> corruptFileList = new ArrayList<>();
        try (org.apache.commons.compress.archivers.zip.ZipFile zipFile = new org.apache.commons.compress.archivers.zip.ZipFile(new File(zipFilePath))) {
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();

            Path path = Paths.get(outputFolder);
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                String entryName = entry.getName();
                File outputFile = new File(outputFolder + "/" + entryName);
                if (unzipFolder == null) {
                    unzipFolder = path.relativize(outputFile.toPath()).getName(0);
                }
                if (entry.isDirectory()) {
                    outputFile.mkdirs();
                } else {
                    InputStream inputStream = zipFile.getInputStream(entry);
                    FileOutputStream outputStream = new FileOutputStream(outputFile);
                    try {
                        IOUtils.copy(inputStream, outputStream);
                    } catch (IOException e) {
                        log.debug("文件损坏：" + outputFile);
                        corruptFileList.add(outputFile);
                    }
                    inputStream.close();
                    outputStream.close();
                }
            }
            //定位损坏文件的list.txt文件，删除里面的损坏文件成功记录，同时删除上一层list.txt的章节下载成功记录
            for (File file : corruptFileList) {
                Path listFile = file.getParentFile().toPath().resolve("list.txt");
                Path chapterListFile = listFile.getParent().getParent().resolve("list.txt");
                deleteStrInFile(chapterListFile.toString(), listFile.getParent().getFileName().toString());
                deleteStrInFile(listFile.toString(), file.getName());
            }
        }
        return unzipFolder;
    }

    public static void deleteStrInFile(String filePath, String delStr) throws IOException {
        {
            if (!new File(filePath).exists()) {
                return;
            }
            StringBuffer readTxt = CommonIOUtils.readTxt(filePath, null);
            List<String> list = Arrays.stream(readTxt.toString().split("\n")).collect(Collectors.toList());
            Iterator<String> iterator = list.iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                if (next.equalsIgnoreCase(delStr)) {
                    iterator.remove();
                }
            }
            String joined = String.join("\n", list) + "\n";
            CommonIOUtils.writeToFile(joined, filePath);
        }
    }

    public static void zipDirectory(String path, String zipPath) {
        File file = new File(path);
        File zipFile = new File(zipPath);
        if (zipFile.exists()) {
            return;
        }
        try {
            if (!zipFile.getParentFile().exists()) {
                zipFile.getParentFile().mkdirs();
            }
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
            compress(zipOutputStream, file, file.getName());
            zipOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Path unzipFile2Dir(String zipFileName, String outputFolder, String fileExtension) throws IOException {
        try (org.apache.commons.compress.archivers.zip.ZipFile zipFile = new org.apache.commons.compress.archivers.zip.ZipFile(new File(zipFileName))) {
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            Path path = Paths.get(outputFolder);
            Path unzipFolder = null;
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (!org.apache.commons.lang3.StringUtils.isEmpty(fileExtension) && entryName.endsWith(fileExtension)) {
                    File outputFile = new File(outputFolder + "/" + entryName);
                    if (unzipFolder == null) {
                        unzipFolder = path.relativize(outputFile.toPath()).getName(0);
                    }
                    if (!outputFile.getParentFile().exists()) {
                        outputFile.getParentFile().mkdirs();
                    }
                    InputStream inputStream = zipFile.getInputStream(entry);
                    FileOutputStream outputStream = new FileOutputStream(outputFile);
                    IOUtils.copy(inputStream, outputStream);
                    inputStream.close();
                    outputStream.close();
                }
            }
            return unzipFolder;
        }
    }

    static void compress(ZipOutputStream zipOutputStream, File sourceFile, String currentPath) {
        try {

            if (sourceFile.isDirectory()) {
                for (File listFile : Objects.requireNonNull(sourceFile.listFiles())) {
                    compress(zipOutputStream, listFile, currentPath + "/" + listFile.getName());
                }
            } else {
                zipOutputStream.putNextEntry(new ZipEntry(currentPath));
                InputStream inputStream = new FileInputStream(sourceFile);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                int tag;
                byte[] tmp = new byte[1024];
                while ((tag = bufferedInputStream.read(tmp)) != -1) {
                    zipOutputStream.write(tmp, 0, tag);
                }
                inputStream.close();
                bufferedInputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    /**
     * @param sourceFolder 需要压缩的目录（不支持文件）
     * @param desFile      保存路径（path/filename.zip)
     * @param password     压缩密码
     */
    public static void ZipFolder(String sourceFolder, String desFile, String password) {
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setEncryptFiles(!StringUtils.isEmpty(password));
        zipParameters.setEncryptionMethod(EncryptionMethod.AES);
// Below line is optional. AES 256 is used by default. You can override it to use AES 128. AES 192 is supported only for extracting.
        zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);

        List<File> filesToAdd = Arrays.asList(
                new File("K:\\pixiv-原神-甘雨\\1337.(85775219)无题.jpg"),
                new File("K:\\pixiv-原神-甘雨\\1393.(86272172)甘雨.png")
        );

        File folder = new File(sourceFolder);
        try (ZipFile zipFile = new ZipFile(desFile, password == null ? null : password.toCharArray())) {
            zipFile.addFolder(folder, zipParameters);
            zipFile.setComment(password);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        ZipFile zipFile = new ZipFile(desFile, password.toCharArray());
////        zipFile.setCharset(Charset.forName("GBK"));
//        try {
//            zipFile.addFolder(folder, zipParameters);
//            zipFile.setComment(password);
//        } catch (ZipException e) {
//            e.printStackTrace();
//        }
    }

    public static void renameFolderInZip(String zipPath, String oldFolder, String newFolder, Charset charset) {
        try (ZipFile zipFile = new ZipFile(zipPath)) {
            zipFile.setCharset(charset);
            System.out.println(zipFile.getFileHeaders());
            FileHeader fileHeader = zipFile.getFileHeader(oldFolder + "/");
            zipFile.renameFile(fileHeader, newFolder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main() {
        //627ca33a75ab703dacb84cc1
//        ZipFolder("E:\\bika\\(624dbd774268ff6cf400aa46)抖M女僕 抖M女仆（搬運更新至55話）", "E:\\bika\\(624dbd774268ff6cf400aa46)抖M女僕 抖M女仆（搬運更新至55話）.zip", "");
        try (ZipFile zipFile = new ZipFile("E:\\test\\(627ca33a75ab703dacb84cc1)疫情期間的家教生活（更新至34话）.zip")) {
//            zipFile.addFile("E:\\更正（调库）通知书数据库设计.docx");
//            ZipParameters zipParameters = new ZipParameters();
            zipFile.setCharset(Charset.forName("gbk"));
            ZipParameters zipParameters = new ZipParameters();
            File file = new File("E:\\test\\(627ca33a75ab703dacb84cc1)疫情期間的家教生活（更新至34话）");
            zipFile.addFolder(file);
//            for (FileHeader fileHeader : zipFile.getFileHeaders()) {
//                System.out.println(fileHeader.getFileName());
//            }
//            zipParameters.setFileNameInZip("新目录2/add.txt");
//            zipFile.addFile("E:\\账单.xlsx", zipParameters);
//            zipFile.removeFile("list.txt");
//            List<FileHeader> fileHeaders = zipFile.getFileHeaders();
//            for (FileHeader fileHeader : fileHeaders) {
//                if (fileHeader.getFileName().endsWith("list.txt")) {
//                    zipFile.extractFile(fileHeader, "E:\\test\\");
//                }
//            }
            //将目录 添加到压缩包中
//            zipFile.addFolder(new File("E:\\bika\\temp\\啊色的发斯蒂芬"));
            //将压缩包中目录修改为新名字
//            FileHeader fileHeader = zipFile.getFileHeader("啊色的发斯蒂芬/");
//            zipFile.renameFile(fileHeader, "新目录名称");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        //627ca33a75ab703dacb84cc1
//        ZipFolder("E:\\bika\\(624dbd774268ff6cf400aa46)抖M女僕 抖M女仆（搬運更新至55話）", "E:\\bika\\(624dbd774268ff6cf400aa46)抖M女僕 抖M女仆（搬運更新至55話）.zip", "");
        try (ZipFile zipFile = new ZipFile("E:\\test\\(627ca33a75ab703dacb84cc1)疫情期間的家教生活（更新至30话）.zip")) {
//            zipFile.addFile("E:\\更正（调库）通知书数据库设计.docx");
//            ZipParameters zipParameters = new ZipParameters();
            zipFile.setCharset(Charset.forName("gbk"));
            ZipParameters zipParameters = new ZipParameters();
            File file = new File("E:\\test\\(627ca33a75ab703dacb84cc1)疫情期間的家教生活（更新至30话）");
            try (Stream<Path> pathStream = Files.walk(Paths.get("E:\\test\\(627ca33a75ab703dacb84cc1)疫情期間的家教生活（更新至30话）"))) {
                pathStream.filter(Files::isRegularFile).forEach(path -> {
                    File fileToAdd = path.toFile();
                    try {
                        zipParameters.setFileNameInZip(fileToAdd.getPath().replace(file.getParent() + File.separator, ""));
                        zipFile.addFile(fileToAdd, zipParameters);
//                        zipFile.addfile
                    } catch (ZipException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            long end = System.currentTimeMillis();
            System.out.println("耗时：" + (end - start));
//            for (FileHeader fileHeader : zipFile.getFileHeaders()) {
//                System.out.println(fileHeader.getFileName());
//            }
//            zipParameters.setFileNameInZip("新目录2/add.txt");
//            zipFile.addFile("E:\\账单.xlsx", zipParameters);
//            zipFile.removeFile("list.txt");
//            List<FileHeader> fileHeaders = zipFile.getFileHeaders();
//            for (FileHeader fileHeader : fileHeaders) {
//                if (fileHeader.getFileName().endsWith("list.txt")) {
//                    zipFile.extractFile(fileHeader, "E:\\test\\");
//                }
//            }
            //将目录 添加到压缩包中
//            zipFile.addFolder(new File("E:\\bika\\temp\\啊色的发斯蒂芬"));
            //将压缩包中目录修改为新名字
//            FileHeader fileHeader = zipFile.getFileHeader("啊色的发斯蒂芬/");
//            zipFile.renameFile(fileHeader, "新目录名称");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void domain() {
        ZipFolder("E:\\bika\\(625fefc772548619f35ee3a8)トラップヒロイン 女主陷阱（全30话已完结）", "E:\\bika\\(625fefc772548619f35ee3a8)トラップヒロイン 女主陷阱（全30话已完结）.zip", null);

    }


    public static List<String> getTarChapter(String tarPath) {
        return getChapters(Paths.get(tarPath));
    }

    public static List<String> getTarChapter(File tarPath) {
        return getChapters(Paths.get(tarPath.getPath()));
    }

    public static <T> List<String> getChapters(String tarPath, Class<T> tClass) {
        return getChapters(Paths.get(tarPath), tClass);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <T, R> List<String> getChapters(Path path, Class<T> tClass) {
        List<String> chapters = new ArrayList<>();
        Pattern pattern = Pattern.compile(".*/(\\d+)/$");
        T instance;
        Constructor<T> declaredConstructor;
        if (tClass.getName().endsWith("ZipFile")) {
            declaredConstructor = tClass.getDeclaredConstructor(String.class, String.class);
            instance = BeanUtils.instantiateClass(declaredConstructor, path.toFile().getPath(), "gbk");
        } else {
            declaredConstructor = tClass.getDeclaredConstructor(Path.class);
            instance = BeanUtils.instantiateClass(declaredConstructor, path);
        }
        Method getEntries = ClassUtils.getMethod(tClass, "getEntries");
        List<? extends ArchiveEntry> entries = (List<? extends ArchiveEntry>) getEntries.invoke(instance);
        entries.forEach(tarArchiveEntry -> {
            Matcher matcher = pattern.matcher(tarArchiveEntry.getName());
            if (matcher.find()) {
                chapters.add(matcher.group(1));
            }
        });
        chapters.sort(Comparator.comparingInt(Integer::parseInt));
        return chapters;
    }

    /**
     * m
     * 获取章节列表
     *
     * @param tarPath
     * @return
     */
    public static List<String> getChapters(Path tarPath) {
        List<String> chapters = new ArrayList<>();
        try (TarFile tarFile = new TarFile(tarPath)) {
            List<TarArchiveEntry> entries = tarFile.getEntries();
            Pattern pattern = Pattern.compile(".*/(\\d+)/$");
            entries.forEach(tarArchiveEntry -> {
                Matcher matcher = pattern.matcher(tarArchiveEntry.getName());
                if (matcher.find()) {
                    chapters.add(matcher.group(1));
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        chapters.sort(Comparator.comparingInt(Integer::parseInt));
        return chapters;
    }

    public static List<String> getRarChapter(File file) {
        return getZipChapter(Paths.get(file.getPath()));
    }

    public static List<String> getZipChapter(File file) {
        return getZipChapter(Paths.get(file.getPath()));
    }

    public static int getFileCount(File file) {
        int fileCount = 0;
        {
            try (ZipFile zipFile = new ZipFile(file.toString())) {
                zipFile.setCharset(Charset.forName("gbk"));
                List<FileHeader> fileHeaders = zipFile.getFileHeaders();
                for (FileHeader fileHeader : fileHeaders) {
                    if (!fileHeader.isDirectory() && !fileHeader.getFileName().endsWith(".txt")) {
                        fileCount++;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return fileCount;
    }

    public static List<String> getZipChapter(String file) {
        return getZipChapter(Paths.get(file));
    }

    public static void moveZip(File zipFile, Path des) {

    }

    /**
     * m
     * 获取章节列表
     *
     * @param tarPath
     * @return
     */
    public static List<String> getZipChapter(Path tarPath) {
        List<String> chapters = new ArrayList<>();
        try (ZipFile zipFile = new ZipFile(tarPath.toString())) {
            zipFile.setCharset(Charset.forName("gbk"));
            List<FileHeader> fileHeaders = zipFile.getFileHeaders();
            Pattern pattern = Pattern.compile(".*/(\\d+)/$");
            fileHeaders.forEach(fileHeader -> {
                Matcher matcher = pattern.matcher(fileHeader.getFileName());
                if (matcher.find()) {
                    chapters.add(matcher.group(1));
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        chapters.sort(Comparator.comparingInt(Integer::parseInt));
        return chapters;
    }

    /**
     * m
     * 将zip转换成tar
     *
     * @param zipFile
     * @param tarFile
     */
    public static void zip2Tar(String zipFile, String tarFile) {
        System.out.println(zipFile);
        Path zipPath = Paths.get(zipFile);
        Path tarPath = Paths.get(tarFile);
        try (ZipArchiveInputStream inputStream = new ZipArchiveInputStream(Files.newInputStream(zipPath), "gbk"); TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(Files.newOutputStream(tarPath), "utf-8")) {
            tarArchiveOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            ZipArchiveEntry zipArchiveEntry;
            while ((zipArchiveEntry = inputStream.getNextZipEntry()) != null) {
                TarArchiveEntry entry = new TarArchiveEntry(zipArchiveEntry.getName());
                entry.setSize(zipArchiveEntry.getSize());
                tarArchiveOutputStream.putArchiveEntry(entry);
                int read;
                byte[] buffer = new byte[1024];
                while ((read = inputStream.read(buffer)) != -1) {
                    tarArchiveOutputStream.write(buffer, 0, read);
                }
                tarArchiveOutputStream.closeArchiveEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 追加文件到zip
     *
     * @param folder2Add 要添加的新内容所在目录
     * @param zipPath    目标压缩包
     * @param replace    需要替换的内容，用于目录结构更新，key是要替换的旧值，value是新值
     */
    @SneakyThrows
    public synchronized static void append2Zip(String folder2Add, String zipPath, Map<String, String> replace) {
        //耗时：1261
        long start = System.currentTimeMillis();
        Path zipFilePath = Paths.get(zipPath);
        String tempName = CruxIdGenerator.generate() + ".zip";
        Path start1 = Paths.get(folder2Add);
        Path tmpZip = start1.getParent().resolve(tempName);
        try (org.apache.commons.compress.archivers.zip.ZipFile zipFile = new org.apache.commons.compress.archivers.zip.ZipFile(zipFilePath.toFile())) {
            ZipArchiveOutputStream outputStream = new ZipArchiveOutputStream(Files.newOutputStream(tmpZip));
            outputStream.setLevel(ZipEntry.STORED);
            ChangeSet changeSet = new ChangeSet();
            try (Stream<Path> pathStream = Files.walk(start1)) {
                pathStream.forEach(path -> {
                    File fileToAdd = path.toFile();
                    try {
                        ZipParameters zipParameters = new ZipParameters();
                        zipParameters.setDefaultFolderPath(start1.getParent().toString());
                        String relativeFileName = net.lingala.zip4j.util.FileUtils.getRelativeFileName(fileToAdd, zipParameters);
                        ArchiveEntry archiveEntry = outputStream.createArchiveEntry(fileToAdd, relativeFileName);
                        changeSet.add(archiveEntry, archiveEntry.isDirectory() ? null : Files.newInputStream(fileToAdd.toPath()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            ChangeSetPerformer changeSetPerformer = replace == null ? new ChangeSetPerformer(changeSet) : new ChangeSetPerformer(changeSet, replace);
            changeSetPerformer.perform(zipFile, outputStream);
            outputStream.close();
            long end = System.currentTimeMillis();
            System.out.println("耗时：" + (end - start));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        FileUtils.delete(zipFilePath.toFile());
        tmpZip.toFile().renameTo(zipFilePath.toFile());
    }

    public synchronized static void compressFolder2Zip(String folder2Add, String zipPath) {
        long start = System.currentTimeMillis();
        Path oriTar = Paths.get(folder2Add);
        Path zip = Paths.get(zipPath);
        FileHolder fileHolder = new FileHolder(folder2Add);
        try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(Files.newOutputStream(zip))) {
            Collection<File> files = FileUtils.listFilesAndDirs(oriTar.toFile(), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            zipArchiveOutputStream.setLevel(ZipEntry.STORED);
            for (File file : files) {
                String fileName = fileHolder.getRelativeFileName(file);
                if (file.isDirectory()) {
                    fileName = fileName.replace('\\', '/');
                }
                ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(file, fileName);
                zipArchiveEntry.setSize(file.length());
                zipArchiveOutputStream.putArchiveEntry(zipArchiveEntry);
                if (!file.isDirectory()) {
                    byte[] buffer = new byte[1024];
                    try (BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
                        int read;
                        while ((read = bufferedInputStream.read(buffer)) != -1) {
                            zipArchiveOutputStream.write(buffer, 0, read);
                        }
                    }
                }
                zipArchiveOutputStream.closeArchiveEntry();
            }
            long end = System.currentTimeMillis();
            log.debug("耗时：{}", (end - start));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

@Data
class FileHolder {
    ZipParameters zipParameters = new ZipParameters();

    public FileHolder(String folder2Add) {
        zipParameters.setDefaultFolderPath(Paths.get(folder2Add).getParent().toString());
    }

    @SneakyThrows
    public String getRelativeFileName(File file) {
        return net.lingala.zip4j.util.FileUtils.getRelativeFileName(file, zipParameters);
    }
}

class test9 {
    public static void main(String[] args) {
        String fileName = "8562....(5853ba23946b938521bf89b3)Hinata NTRism";
//        Pattern compile = Pattern.compile("(?:(\\d+\\.{4})?)\\(\\w+\\)");
        Pattern compile = Pattern.compile("(\\d+\\.{4})?\\((?=5853ba23946b938521bf89b3)5853ba23946b938521bf89b3\\)");
        Matcher matcher = compile.matcher(fileName);
        if (matcher.find()) {
            System.out.println(fileName.substring(matcher.start(), matcher.end()));
        } else {
            System.out.println(false);
        }
    }
}

class test10 {
    public static void main(String[] args) {
        String fileName = "industry abc";
//        Pattern compile = Pattern.compile("(?:(\\d+\\.{4})?)\\(\\w+\\)");
        Pattern compile = Pattern.compile("industr(?=y)");
        Matcher matcher = compile.matcher(fileName);
        if (matcher.find()) {
            System.out.println(fileName.substring(matcher.start(), matcher.end()));
        } else {
            System.out.println(false);
        }
    }
}

/**
 * 压缩成zip
 */
class test8 {
    public static void main(String[] args) {
//        不压缩耗时：892 压缩耗时：6703
        long start = System.currentTimeMillis();

        Path oriTar = Paths.get("c:\\test\\(627ca33a75ab703dacb84cc1)疫情期間的家教生活（更新至30话）");
        try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(Files.newOutputStream(Paths.get("c:\\test", "(627ca33a75ab703dacb84cc1)疫情期間的家教生活（更新至30话）.zip")))) {
//            Collection<File> files = FileUtils.listFiles(oriTar.toFile(), null, true);
            Collection<File> files = FileUtils.listFilesAndDirs(oriTar.toFile(), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            zipArchiveOutputStream.setLevel(ZipEntry.STORED);
            for (File file1 : files) {
                String fileName = file1.getPath().replace("c:\\test\\", "");
                if (file1.isDirectory()) {
                    fileName = fileName.replace('\\', '/');
                }
                ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(file1, fileName);
                zipArchiveEntry.setSize(file1.length());
                zipArchiveOutputStream.putArchiveEntry(zipArchiveEntry);
                if (!file1.isDirectory()) {
                    byte[] buffer = new byte[1024];
                    try (BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(file1.toPath()));) {
                        int read;
                        while ((read = bufferedInputStream.read(buffer)) != -1) {
                            zipArchiveOutputStream.write(buffer, 0, read);
                        }
                    }
                }
                zipArchiveOutputStream.closeArchiveEntry();
            }
            long end = System.currentTimeMillis();
            System.out.println("耗时：" + (end - start));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class test7 {

    public static final ThreadLocal<ArrayList<File>> dirsCache = ThreadLocal.withInitial(ArrayList::new);
    public static final Pattern pattern = Pattern.compile("\\(\\w+\\)");

    public static void main(String[] args) {

//        ZipUtils.compressFolder2Zip("G:\\bika\\(5e5b0cb41f7673759b6d585d)[Tumblr] sq003", "G:\\bika\\(5e5b0cb41f7673759b6d585d)[Tumblr] sq003.zip");
        File base = new File("E:\\性转");
//        File[] files = base.listFiles();
        listDirs(base);
        Collection<File> dirs = dirsCache.get();
        for (File file : dirs) {
            if (file.isDirectory()) {
                ZipUtils.compressFolder2Zip(file.toString(), file + ".zip");
                try {
                    FileUtils.deleteDirectory(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(file);
            }
        }
//        List<String> zipChapter = ZipUtils.getZipChapter("C:\\\\test\\\\(627ca33a75ab703dacb84cc1)疫情期間的家教生活（更新至30话）.zip");
//        System.out.println(zipChapter);
//        HashMap<String, String> hashMap = new HashMap<String, String>() {{
//            put("(627ca33a75ab703dacb84cc1)疫情期間的家教生活（更新至30话）", "新目录222");
//        }};
//        ZipUtils.append2Zip("C:\\test\\(627ca33a75ab703dacb84cc1)疫情期間的家教生活（更新至30话）", "C:\\test\\(627ca33a75ab703dacb84cc1)疫情期間的家教生活（更新至30话）.zip", hashMap);
//        ZipUtils.renameFolderInZip("J:\\bika\\(5cac8106e859a07091d8baf5)达尔文游戏.zip", "(5cac8106e859a07091d8baf5)", "(5cac8106e859a07091d8baf5)达尔文游戏", Charset.forName("gbk"));
    }

    public static void listDirs(File file) {
//        File[] files = file.listFiles(pathname -> pathname.isDirectory() && Pattern.compile(regex).matcher(pathname.getName()).find());
        File[] dirs = file.listFiles(File::isDirectory);
        assert dirs != null;
        Arrays.stream(dirs).forEach(file1 -> {
            if (pattern.matcher(file1.getName()).find()) {
                dirsCache.get().add(file1);
            } else {
                listDirs(file1);
            }
        });
    }
}

/**
 * 将zip转成tar
 */
class test5 {
    public static void main(String[] args) {

        Path zipPath = Paths.get("J:\\bika_zip\\(5aa20f3778f1cc4fc59c0f75)あやかし館へようこそ！ 1-10話.zip");
        Path tarPath = Paths.get("c:\\test", "5aa20f3778f1cc4fc59c0f75.tar");
        try (ZipArchiveInputStream inputStream = new ZipArchiveInputStream(Files.newInputStream(zipPath), "gbk"); TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(Files.newOutputStream(tarPath))) {
            ZipArchiveEntry zipArchiveEntry;
            while ((zipArchiveEntry = inputStream.getNextZipEntry()) != null) {
                TarArchiveEntry entry = new TarArchiveEntry(zipArchiveEntry.getName());
                entry.setSize(zipArchiveEntry.getSize());
                tarArchiveOutputStream.putArchiveEntry(entry);
                int read = -1;
                byte[] buffer = new byte[1024];
                while ((read = inputStream.read(buffer)) != -1) {
                    tarArchiveOutputStream.write(buffer, 0, read);
                }
                tarArchiveOutputStream.closeArchiveEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

/**
 * 将rar转成zip
 */
class test11 {
    public static void main(String[] args) {

        Path zipPath = Paths.get("J:\\bika_zip\\(5aa20f3778f1cc4fc59c0f75)あやかし館へようこそ！ 1-10話.zip");
        Path tarPath = Paths.get("c:\\test", "5aa20f3778f1cc4fc59c0f75.tar");
        try (ZipArchiveInputStream inputStream = new ZipArchiveInputStream(Files.newInputStream(zipPath), "gbk"); TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(Files.newOutputStream(tarPath))) {
            ZipArchiveEntry zipArchiveEntry;
            while ((zipArchiveEntry = inputStream.getNextZipEntry()) != null) {
                TarArchiveEntry entry = new TarArchiveEntry(zipArchiveEntry.getName());
                entry.setSize(zipArchiveEntry.getSize());
                tarArchiveOutputStream.putArchiveEntry(entry);
                int read = -1;
                byte[] buffer = new byte[1024];
                while ((read = inputStream.read(buffer)) != -1) {
                    tarArchiveOutputStream.write(buffer, 0, read);
                }
                tarArchiveOutputStream.closeArchiveEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class test6 {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
//        耗时：1726
        Path oriTar = Paths.get("c:\\test\\(627ca33a75ab703dacb84cc1)疫情期間的家教生活（更新至30话）");
        try (TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(Files.newOutputStream(Paths.get("c:\\test", "test.tar")))) {
            Collection<File> files = FileUtils.listFiles(oriTar.toFile(), null, true);
            for (File file1 : files) {
                String fileName = file1.getPath().replace("c:\\test\\", "");
                TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(fileName);
                tarArchiveEntry.setSize(file1.length());
                tarArchiveOutputStream.putArchiveEntry(tarArchiveEntry);
                byte[] buffer = new byte[1024];
                try (BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(file1.toPath()));) {
                    int read;
                    while ((read = bufferedInputStream.read(buffer)) != -1) {
                        tarArchiveOutputStream.write(buffer, 0, read);
                    }
                }
                tarArchiveOutputStream.closeArchiveEntry();
            }
            long end = System.currentTimeMillis();
            System.out.println("耗时：" + (end - start));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class test4 {
    public static void main(String[] args) {
        Path oriTar = Paths.get("c:\\test\\(627ca33a75ab703dacb84cc1)疫情期間的家教生活（更新至30话）.tar");
        try (TarArchiveInputStream inputStream = new TarArchiveInputStream(Files.newInputStream(oriTar))) {
            TarArchiveEntry tarArchiveEntry;
            while ((tarArchiveEntry = inputStream.getNextTarEntry()) != null) {
                String fileName = tarArchiveEntry.getName();
                File exFile = new File(oriTar.toFile().getParent(), fileName);
                if (tarArchiveEntry.isDirectory()) {
                    if (!exFile.exists()) {
                        exFile.mkdirs();
                    }
                    continue;
                }
                try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(exFile.toPath()))) {
                    int read = -1;
                    byte[] buffer = new byte[1024];
                    while ((read = inputStream.read(buffer)) != -1) {
                        bos.write(buffer, 0, read);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (TarFile tarFile = new TarFile(oriTar)) {
            List<TarArchiveEntry> entries = tarFile.getEntries();
            for (TarArchiveEntry entry : entries) {
                System.out.println(entry.getPath());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class test2 {
    public static void main(String[] args) {
        //耗时：1261
        long start = System.currentTimeMillis();
        Path pathpath = Paths.get("c:\\test\\test.zip");
        try (org.apache.commons.compress.archivers.zip.ZipFile zipFile = new org.apache.commons.compress.archivers.zip.ZipFile(pathpath.toFile());) {
            ZipArchiveOutputStream outputStream = new ZipArchiveOutputStream(Files.newOutputStream(Paths.get("c:\\test\\new.zip")));
            outputStream.setLevel(ZipEntry.STORED);
            File file = new File("c:\\test\\(627ca33a75ab703dacb84cc1)疫情期間的家教生活（更新至30话）");
            ChangeSet changeSet = new ChangeSet();
            try (Stream<Path> pathStream = Files.walk(Paths.get("c:\\test\\(627ca33a75ab703dacb84cc1)疫情期間的家教生活（更新至30话）"))) {
                pathStream.filter(Files::isRegularFile).forEach(path -> {
                    File fileToAdd = path.toFile();
                    try {
                        ZipParameters zipParameters = new ZipParameters();
                        zipParameters.setDefaultFolderPath("c:\\test\\");
                        String relativeFileName = net.lingala.zip4j.util.FileUtils.getRelativeFileName(fileToAdd, zipParameters);
                        ArchiveEntry archiveEntry = outputStream.createArchiveEntry(fileToAdd, relativeFileName);
                        changeSet.add(archiveEntry, Files.newInputStream(fileToAdd.toPath()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
//            File fileToAdd = new File("E:\\test\\(627ca33a75ab703dacb84cc1)疫情期間的家教生活（更新至30话）\\17\\1.jpg");
//            ArchiveEntry archiveEntry = outputStream.createArchiveEntry(fileToAdd, "111/" + fileToAdd.getName());

//            changeSet.add(archiveEntry, Files.newInputStream(fileToAdd.toPath()));
            ChangeSetPerformer changeSetPerformer = new ChangeSetPerformer(changeSet);
            changeSetPerformer.perform(zipFile, outputStream);
            long end = System.currentTimeMillis();
            System.out.println("耗时：" + (end - start));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class Test13 {
    public static void main(String[] args) throws Exception {
        String source = "E:\\bika\\(627ca33a75ab703dacb84cc1)疫情期間的家教生活（至60话）.zip";

        try (InputStream is = Files.newInputStream(Paths.get(source));
             ZipArchiveInputStream zis = new ZipArchiveInputStream(is)) {
            ArchiveEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry instanceof ZipArchiveEntry) {
                    ZipArchiveEntry zipEntry = (ZipArchiveEntry) entry;
                    Charset charset = zipEntry.getGeneralPurposeBit().usesUTF8ForNames() ? StandardCharsets.UTF_8 : Charset.forName("GBK");
                    String fileName = zipEntry.getName();
                    String decodedFileName = new String(fileName.getBytes(charset), "ISO-8859-1");
                    System.out.println("Original: " + fileName);
                    System.out.println("Decoded : " + decodedFileName);
                }
            }
        }
    }
}
