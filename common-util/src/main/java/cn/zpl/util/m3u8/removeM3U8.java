package cn.zpl.util.m3u8;

import cn.zpl.util.m3u8.thread.M3u8FileDownloadThread;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class removeM3U8 {
    @SneakyThrows
    public static void main(String[] args) {
        m3u8Core core = new m3u8Core();
        String[] extensions = new String[]{"m3u8"};
        File base = new File("E:\\m3u8\\2\\新建文件夹");
        Collection<File> list = FileUtils.listFiles(base, extensions.length == 0 ? null : extensions, true);
        Map<String, List<File>> collect = list.stream().collect(Collectors.groupingBy(file -> {
            try {
                M3U8 m3U8 = M3u8FileDownloadThread.parseIndex(file.getParent(), file.getName(), "http://test.com");
                return m3U8.getKeyUrl();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
        for (Map.Entry<String, List<File>> stringListEntry : collect.entrySet()) {
            if (stringListEntry.getValue().size() == 1) {
                stringListEntry.getValue().stream().filter(file -> file.getName().contains("index")).forEach(file -> {
                    try {
                        FileUtils.moveFileToDirectory(file, new File("E:\\m3u8\\2"), true);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }
}
