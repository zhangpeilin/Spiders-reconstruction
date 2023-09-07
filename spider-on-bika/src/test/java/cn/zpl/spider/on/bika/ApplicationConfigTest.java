package cn.zpl.spider.on.bika;

import cn.zpl.common.bean.Bika;
import cn.zpl.common.bean.BikaList;
import cn.zpl.common.bean.RestResponse;
import cn.zpl.config.SpringContext;
import cn.zpl.spider.on.bika.common.BikaProperties;
import cn.zpl.spider.on.bika.thread.BikaComicThread;
import cn.zpl.spider.on.bika.utils.BikaUtils;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.CrudTools;
import cn.zpl.util.DownloadTools;
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
    BikaProperties params;

    @Test
    public void loadConfig() {
        System.out.println(params.getSavePath());
        {
            DownloadTools tool = DownloadTools.getInstance(5);
            tool.setName("漫画");
            tool.setSleepTimes(10000);
            String str = "5821871d5f6b9a4f93dd0f6d,\n" +
                    "58218fc75f6b9a4f93e341b9,\n" +
                    "58218fc75f6b9a4f93e341b9,\n" +
                    "58218fcd5f6b9a4f93e346ef,\n" +
                    "58218ffa5f6b9a4f93e36b9d,\n" +
                    "582194245f6b9a4f93e68b31,\n" +
                    "5821a1cb5f6b9a4f93ef4385,\n" +
                    "5821a5315f6b9a4f93f1532d,\n" +
                    "5821adaf5f6b9a4f93f5fc07,\n" +
                    "587e542ebb673b69bc8bcff9,\n" +
                    "587f85bbed084469d6c457f9,\n" +
                    "5885ee953f65ce7fcdd5d218,\n" +
                    "58da7dd3dc3eda279e4a411a,\n" +
                    "58dcda66da056e7f97b70a2c,\n" +
                    "590e9d34b21192073e8c9091,\n" +
                    "5936bf8be1f5381f6e37c3ec,\n" +
                    "594b313927970c3a21237bbc,\n" +
                    "5957bcb721e277475e6f56b2,\n" +
                    "5968384acc8ec80cd59bdafb,\n" +
                    "596b15146f10593244ceaab9,\n" +
                    "596ed7536f10593244cec2e8,\n" +
                    "59e4c9053fc27312a09939fe,\n" +
                    "5ac0e889d5f5652f2ad71858,\n" +
                    "5aca0391b81c0a161180d6e1,\n" +
                    "5afbc62f3035530aef2e5f43,\n" +
                    "5b114e8c87bb266ca9ba6e5f,\n" +
                    "5b3e38d7199aa92ca9914787,\n" +
                    "5b77d091836a6151858cc12b,\n" +
                    "5c06995cdcb1c3176437f17b,\n" +
                    "5c166d6ebafe76224d84aaac,\n" +
                    "5c2b39ef9063e71bb9ba0597,\n" +
                    "5c2dd367a660eb4466f206a7,\n" +
                    "5c73a66f939410787059ffea,\n" +
                    "5c943d05099a0a452e115897,\n" +
                    "5ca8b88ac9809970d39f0a32,\n" +
                    "5cb88f31ca60d64e2687ca8d,\n" +
                    "5cbf2b0490707c7edc5e2dc7,\n" +
                    "5ced643352c88a706444b10e,\n" +
                    "5d3332f45be4130a396b1573,\n" +
                    "5d4586dd6ec29d1707865a68,\n" +
                    "5d4daf804721ec029d4c40a8,\n" +
                    "5d52cf9b5422a75212c3f292,\n" +
                    "5d73b1e3140d327c4093441c,";
            for (String s : str.split(",\n")) {
                tool.ThreadExecutorAdd(new BikaComicThread(s));
            }
            tool.shutdown();
        }
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
        System.out.println(bikaUtils.getBikaExist("582185a15f6b9a4f93dbf957"));
        System.out.println(bikaUtils.getBikaListExist("582185ab5f6b9a4f93dbff9b"));
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
            Bika exist = bikaUtils.getBikaExist(fileId);
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
        beanWithGenerics.getBikaExist("5821859d5f6b9a4f93dbf719");
    }

    @Test
    public void getHighScoreNotExists() {
        BikaUtils bikaUtils = SpringContext.getBeanWithGenerics(BikaUtils.class);
        CrudTools crudTools = SpringContext.getBeanWithGenerics(CrudTools.class);
        List<BikaList> bikaLists = crudTools.commonApiQueryBySql("select * from bika_list where likes_count > 10000 and  `categories` NOT LIKE '%CG%'", BikaList.class);
//        bikaLists.stream().filter(bikaList -> !StringUtils.isEmpty(bikaList.getLocalPath())).parallel().forEach(bikaList -> {
//            Bika exists = bikaUtils.getExists(bikaList.getId());
//            File file = new File(exists.getLocalPath());
//            if (!file.exists()) {
//                SaveLog.saveLog("M:\\高评分未下载\\" + file.getName());
//            }
//        });
        List<String> notExists = bikaLists.stream().filter(bikaList -> !StringUtils.isEmpty(bikaList.getLocalPath())).parallel().filter(bikaList -> {
            Bika exists = bikaUtils.getBikaExist(bikaList.getId());
            File file = new File(exists.getLocalPath());
            return !file.exists();
        }).map(BikaList::getId).collect(Collectors.toList());
        bikaUtils.downloadByIds(notExists);
    }
}
