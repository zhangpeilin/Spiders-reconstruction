package cn.zpl.spider.on.ehentai;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

public class TestZip {
    public static void main(String[] args) {

        File zip = new File("E:\\bika\\(5ecd53c4db607357fe99d801)1年A班的怪物 1年A組のモンスター【更新到第36话】.zip");
        try (ZipFile zipFile = new ZipFile(zip)) {
            List<ZipArchiveEntry> sortedEntries = Collections.list(zipFile.getEntries());
            Map<String, Map<Integer, List<String>>> resultMap = new HashMap<>();
            List<String> paths = sortedEntries.stream().map(ZipArchiveEntry::getName).collect(Collectors.toList());
            String rootDir = null;
            for (String path : paths) {
                if (path.contains("list.txt")) {
                    continue;
                }
                String[] parts = path.split("/");
                String folder = parts[0];
                int subfolder = parts.length >= 2 ? Integer.parseInt(parts[1]) : 0;
                String fileName = parts[parts.length - 1];
                //如果只有一个/表示只有根目录，如果有两个//表示带章节目录，如果有3个//表示带图片名称
                if (parts.length == 1) {
                    resultMap.putIfAbsent(folder, new HashMap<>());
                    continue;
                }
                resultMap.get(folder).putIfAbsent(0, new ArrayList<>());
                if (parts.length == 2) {
                    resultMap.get(folder).get(0).add(String.valueOf(subfolder));
                }
                resultMap.get(folder).putIfAbsent(subfolder, new ArrayList<>());
                if (parts.length == 3) {
                    resultMap.get(folder).get(subfolder).add(fileName);
                }
            }

            for (Map.Entry<String, Map<Integer, List<String>>> entry : resultMap.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
