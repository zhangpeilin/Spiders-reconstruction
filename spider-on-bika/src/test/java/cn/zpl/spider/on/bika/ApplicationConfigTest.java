package cn.zpl.spider.on.bika;

import cn.zpl.common.bean.Bika;
import cn.zpl.common.bean.BikaList;
import cn.zpl.config.SpringContext;
import cn.zpl.spider.on.bika.common.BikaParams;
import cn.zpl.spider.on.bika.utils.BikaUtils;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.CrudTools;
import cn.zpl.util.SaveLog;
import cn.zpl.util.ZipUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 *
 * @author zhangpl1
 * @date 2022/8/4
 */
@SpringBootTest
public class ApplicationConfigTest {

    @Resource
    BikaParams params;

    @Test
    public void loadConfig() {
        System.out.println(params.getSavePath());
    }

    @Test
    public void echoNotExists(){
        BikaUtils bikaUtils = SpringContext.getBeanWithGenerics(BikaUtils.class);
        CrudTools crudTools = SpringContext.getBeanWithGenerics(CrudTools.class);
        List<Bika> bikas = crudTools.commonApiQueryBySql("select * from bika where is_deleted = 0", Bika.class);
        bikas.stream().filter(bika -> !StringUtils.isEmpty(bika.getLocalPath())).parallel().forEach(bika -> {
            File exists = new File(bika.getLocalPath());
            if (!exists.exists()) {
                System.out.println(bika.getLocalPath());
                SaveLog.saveLog("M:\\" + exists.getName());

            }
        });
    }

    @Test
    public void domain() {
        BikaUtils bikaUtils = SpringContext.getBeanWithGenerics(BikaUtils.class);
//        bikaUtils.search("異種相姦", false);
        bikaUtils.downloadById("5c40280ad7df306bf6f737a4");
//        bikaUtils.showH24();
//        bikaUtils.favourite();
    }

    @Test
    public void check() {
        //筛选指定目录中的文件夹、压缩包是否在数据库中存在，如果不存在，则将文件夹或压缩包移动到存档点并更新数据库中的记录；如果存在，则比较eps_count（章节）数，章节数多的为最新，并将文件夹或压缩包添加.old后缀
        BikaUtils bikaUtils = SpringContext.getBeanWithGenerics(BikaUtils.class);
        Collection<File> files = FileUtils.listFiles(new File("J:\\zip"), null, false);
        Map<String, List<File>> group = files.stream().collect(Collectors.groupingBy(file -> file.getName().substring(file.getName().indexOf(".") + 1)));

        group.forEach((o, fileList) -> {
            Function<File, List<String>> callback = null;
            BiFunction<File, Path, Path> after = bikaUtils::moveFile;
            switch (o) {
                case "zip":
                    callback = ZipUtils::getZipChapter;
                    break;
                case "rar":
                    callback = ZipUtils::getRarChapter;
                    break;
                case "tar":
                    callback = ZipUtils::getTarChapter;
                    break;
                case "":
                    callback = CommonIOUtils::getDirChapter;
                    break;
            }
            bus(o, fileList, bikaUtils, callback, after);
        });
    }

    public void bus(String type, List<File> files, BikaUtils bikaUtils, Function<File, List<String>> callback, BiFunction<File, Path, Path> after) {
        if (callback == null) {
            throw new RuntimeException("没有传入正确的处理函数");
        }
        CrudTools crudTools = SpringContext.getBeanWithGenerics(CrudTools.class);
        for (File file : files) {
            String fileId = CommonIOUtils.getFileId(file);
            Bika exist = bikaUtils.getExists(fileId);
            //如果数据库记录的位置没有文件，则以该文件为准更新
            if (exist != null) {
                File localFile = new File(exist.getLocalPath());
                if (!localFile.exists()) {
                    exist.setEpsCount(callback.apply(file).size());
                    Path des = after.apply(file, bikaUtils.GetAvailablePath(file.length(), file));
                    exist.setLocalPath(des.toString());
                    crudTools.commonApiSave(exist);
                } else {
                    Integer existsEpsCount = exist.getEpsCount();
                    List<String> chaptersList = callback.apply(file);
                    //如果文件中章节数大于数据库中记录，则文件为最新记录，解压并且打成zip放入存档目录
                    if (chaptersList.size() > existsEpsCount) {
                        after.apply(file, bikaUtils.GetAvailablePath(file.length(), file));
                    }
                }
            }
//            if (exist != null) {
//                Integer existsEpsCount = exist.getEpsCount();
//                List<String> chaptersList = callback.apply(file);
//                //如果文件中章节数大于数据库中记录，则文件为最新记录，解压并且打成tar放入存档；
//                if (chaptersList.size() > existsEpsCount) {
//                    after.apply(file, bikaUtils.GetAvaliablePath(file.getUsableSpace()));
//                }
//            } else {
//                //如果数据库不存在记录，则文件为最新记录
//            }
        }
    }

    @Test
    public void testUrl() {
        CrudTools crudTools = SpringContext.getBeanWithGenerics(CrudTools.class);
        List<Bika> bikas = crudTools.commonApiQueryBySql("select * from bika where id = '5821859d5f6b9a4f93dbf719'", Bika.class);
        System.out.println(bikas);
    }

    @Test
    public void testExists() {
        BikaUtils beanWithGenerics = SpringContext.getBeanWithGenerics(BikaUtils.class);
        beanWithGenerics.getExists("5821859d5f6b9a4f93dbf719");
    }

    @Test
    public void getHighScoreNotExists() {
        BikaUtils bikaUtils = SpringContext.getBeanWithGenerics(BikaUtils.class);
        CrudTools crudTools = SpringContext.getBeanWithGenerics(CrudTools.class);
        List<BikaList> bikaLists = crudTools.commonApiQueryBySql("select * from bika_list where likes_count > 10000", BikaList.class);
        bikaLists.stream().filter(bikaList -> !StringUtils.isEmpty(bikaList.getLocalPath())).parallel().forEach(bikaList -> {
            Bika exists = bikaUtils.getExists(bikaList.getId());
            File file = new File(exists.getLocalPath());
            if (!file.exists()) {
                SaveLog.saveLog("M:\\高评分未下载\\" + file.getName());
            }
        });
    }
}
