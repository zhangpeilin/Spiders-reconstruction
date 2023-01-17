package cn.zpl.util;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import net.lingala.zip4j.tasks.AddFolderToZipTask;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.changes.ChangeSet;
import org.apache.commons.compress.changes.ChangeSetPerformer;
import org.apache.commons.io.FileUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {


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

    public static void renameFolderInZip(String zipPath, String oldFolder, String newFolder) {
        try (ZipFile zipFile = new ZipFile(zipPath)) {
            zipFile.setCharset(Charset.forName("gbk"));
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
}

class test1{
    public static void main(String[] args) {
        Path oriTar = Paths.get("E:\\test\\(627ca33a75ab703dacb84cc1)疫情期間的家教生活（更新至30话）.tar");
        try (TarArchiveInputStream inputStream = new TarArchiveInputStream(Files.newInputStream(oriTar))) {
            TarArchiveOutputStream outputStream = new TarArchiveOutputStream(Files.newOutputStream(Paths.get("E:\\test\\22.tar")));
            ChangeSet changeSet = new ChangeSet();
            Collection<File> files = FileUtils.listFiles(new File("E:\\test\\(627ca33a75ab703dacb84cc1)疫情期間的家教生活（更新至30话）"), null, true);
            for (File fileToAdd : files) {
                ZipParameters zipParameters = new ZipParameters();
                zipParameters.setDefaultFolderPath("E:\\test\\");
                String relativeFileName = net.lingala.zip4j.util.FileUtils.getRelativeFileName(fileToAdd, zipParameters);
                ArchiveEntry archiveEntry = outputStream.createArchiveEntry(fileToAdd, relativeFileName);
                changeSet.add(archiveEntry, Files.newInputStream(fileToAdd.toPath()));
            }
            ChangeSetPerformer changeSetPerformer = new ChangeSetPerformer(changeSet);
            changeSetPerformer.perform(inputStream, outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class test2{
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Path pathpath = Paths.get("E:\\test\\(627ca33a75ab703dacb84cc1)疫情期間的家教生活（更新至30话）.zip");
        try (org.apache.commons.compress.archivers.zip.ZipFile zipFile = new org.apache.commons.compress.archivers.zip.ZipFile(pathpath.toFile());) {
            ZipArchiveOutputStream outputStream = new ZipArchiveOutputStream(Files.newOutputStream(Paths.get("E:\\test\\tmp.zip")));

            File file = new File("E:\\test\\(627ca33a75ab703dacb84cc1)疫情期間的家教生活（更新至30话）");
            ChangeSet changeSet = new ChangeSet();
            try (Stream<Path> pathStream = Files.walk(Paths.get("E:\\test\\(627ca33a75ab703dacb84cc1)疫情期間的家教生活（更新至30话）"))) {
                pathStream.filter(Files::isRegularFile).forEach(path -> {
                    File fileToAdd = path.toFile();
                    try {
                        ArchiveEntry archiveEntry = outputStream.createArchiveEntry(fileToAdd, fileToAdd.getPath().replace(file.getParent() + File.separator, ""));
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