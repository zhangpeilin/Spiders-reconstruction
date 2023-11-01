package cn.zpl.util;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class UnZipUtils {


    /**
     * @param zipFile  原始文件路径
     * @param dest     解压路径
     * @param password 解压文件密码(可以为空)
     */
    @NotNull
    public static String unZip(File zipFile, String dest, String password) throws ZipException {

        ZipFile zFile = new ZipFile(zipFile); // 首先创建ZipFile指向磁盘上的.zip文件


//        zFile.setFileNameCharset("GBK");

        if (dest == null || "".equalsIgnoreCase(dest)) {
            dest = zipFile.getPath().replace(".zip", "");
        }

        File destDir = new File(dest); // 解压目录
        if (!destDir.exists()) {// 目标目录不存在时，创建该文件夹
            destDir.mkdirs();
        }
        if (zFile.isEncrypted()) {


            zFile.setPassword(password.toCharArray()); // 设置密码


        }

        List<FileHeader> headerList = zFile.getFileHeaders();
        if (headerList.isEmpty()) {
            throw new RuntimeException("文集列表为空，密码错误");
        }
        zFile.extractAll(dest); // 将文件抽出到解压目录(解压)


        List<File> extractedFileList = new ArrayList<>();


        for (FileHeader fileHeader : headerList) {


            if (!fileHeader.isDirectory()) {


                extractedFileList.add(new File(destDir, fileHeader.getFileName()));

            }

        }

        File[] extractedFiles = new File[extractedFileList.size()];
        extractedFileList.toArray(extractedFiles);
        return dest;
    }
}