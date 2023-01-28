package cn.zpl.spider.on.bika;

import cn.zpl.spider.on.bika.utils.BikaUtils;
import cn.zpl.util.CommonIOUtils;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Check115 {

    @SneakyThrows
    public static void main(String[] args) {
        StringBuffer gbk = CommonIOUtils.readTxt("M:\\BaiduNetdiskDownload\\备份20230122020304_目录树.txt", "unicode");
        String[] files = gbk.toString().split("\n");
        Map<String, String> fileOn115 = Arrays.stream(files).map(CommonIOUtils::getFileId2).filter(s -> !StringUtils.isEmpty(s)).collect(Collectors.toMap(s -> s, s -> s));
        Collection<File> zips = FileUtils.listFiles(new File("J:\\bika"), null, false);
        Map<String, File> zipMap = zips.stream().collect(Collectors.toMap(CommonIOUtils::getFileId, file -> file));
        Set<Map.Entry<String, File>> entries = zipMap.entrySet();
        Iterator<Map.Entry<String, File>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, File> zip = iterator.next();
            String key = zip.getKey();
            if (fileOn115.get(key) != null) {
                iterator.remove();
            }
        }
        System.out.println(zipMap);
    }
}
