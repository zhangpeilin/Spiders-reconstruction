package cn.zpl.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class FileCacheToInputStreamExample {
    public static void main(String[] args) {
        // 读取文件到缓存
        byte[] fileData = readFileToByteArray("path/to/file.txt");

        // 将缓存数据转换为输入流
        InputStream inputStream = new ByteArrayInputStream(fileData);

        // 在这里可以使用inputStream进行操作，比如读取文件内容

        // 关闭输入流
        try {
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] readFileToByteArray(String filePath) {
        try {
            File file = new File(filePath);

            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];

            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            fis.close();
            bos.close();

            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}