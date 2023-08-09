package cn.zpl;

import cn.zpl.util.CommonStringUtil;
import cn.zpl.util.ZipUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collection;

@Slf4j
public class CheckSimulateFile {
    public static void main(String[] args) {
        Collection<File> files = FileUtils.listFiles(new File("G:\\ehentai"), new String[]{"zip"}, true);
//        LinkedHashMultimap<File, File> already = LinkedHashMultimap.create();
        Multimap<File, File> already = Multimaps.synchronizedMultimap(ArrayListMultimap.create());
        files.forEach(file1 -> files.parallelStream().forEach(file2 -> {
            if (file1.getPath().equalsIgnoreCase(file2.getPath())) {
                return;
            }
            if (CommonStringUtil.simulate(file1.getName(), file2.getName()) < 0.5) {
                return;
            }
//            System.out.printf("当前对比：%1$s\n和%2$s\n", file1.getName(), file2.getName());
            //如果key包含存在k-v键值对，或者v-k键值对，则返回
            if (already.containsEntry(file1, file2) || already.containsEntry(file2, file1)) {
                return;
            }
            already.put(file1, file2);
            log.debug("当前对比：\n{}\n{}\n", file1, file2);
            if (ZipUtils.checkZipSimulate(file1.toString(), file2.toString())) {
//                System.out.printf("%1$s\n和%2$s\n文件相似%n\n", file1, file2);
                log.debug("{}与{}相似", file1.getName(), file2.getName());
            }
        }));
    }
}
