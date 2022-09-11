package cn.zpl.util;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.springframework.util.StringUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    public void test() {
        zipDirectory("T:\\mh160\\异世界精灵的奴隶酱", "z:\\本子\\异世界精灵的奴隶酱.zip");
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
        try (ZipFile zipFile = new ZipFile(desFile, password == null ? null : password.toCharArray())){
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

    public static void main(String[] args) {
        ZipFolder("E:\\bika\\(625fefc772548619f35ee3a8)トラップヒロイン 女主陷阱（全30话已完结）", "E:\\bika\\(625fefc772548619f35ee3a8)トラップヒロイン 女主陷阱（全30话已完结）.zip", "");

    }
    public void domain() {
        ZipFolder("E:\\bika\\(625fefc772548619f35ee3a8)トラップヒロイン 女主陷阱（全30话已完结）", "E:\\bika\\(625fefc772548619f35ee3a8)トラップヒロイン 女主陷阱（全30话已完结）.zip", null);

    }
}
