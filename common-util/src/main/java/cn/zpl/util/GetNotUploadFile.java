package cn.zpl.util;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GetNotUploadFile {

    @SneakyThrows
    public static void main(String[] args) {
        File base = new File("C:\\视频爬虫\\nas\\视频");
        String[] extensions = new String[]{};
        StringBuffer stringBuffer = CommonIOUtils.readTxt("E:\\115\\备份20220817002031_目录树.txt", "UTF-16");
        Collection<File> list = FileUtils.listFiles(base, extensions.length == 0 ? null : extensions, false);
        Map<String, List<File>> listMap = list.stream().collect(Collectors.groupingBy(File::getName));
        String[] fileNames = stringBuffer.toString().split("\n");
        for (String fileName : fileNames) {
            List<File> files = listMap.get(fileName);
            if (!CollectionUtils.isEmpty(files)) {
                for (File file : files) {
                    file.renameTo(new File("C:\\视频爬虫\\nas\\视频\\已上传", file.getName()));
                }
            }
        }
    }
}
