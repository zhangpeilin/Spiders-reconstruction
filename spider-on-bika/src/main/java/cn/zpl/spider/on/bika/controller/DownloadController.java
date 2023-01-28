package cn.zpl.spider.on.bika.controller;

import cn.zpl.common.bean.Bika;
import cn.zpl.common.bean.BikaList;
import cn.zpl.common.bean.RestResponse;
import cn.zpl.config.SpringContext;
import cn.zpl.spider.on.bika.utils.BikaUtils;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.CrudTools;
import cn.zpl.util.SaveLog;
import cn.zpl.util.ZipUtils;
import com.alibaba.fastjson.JSON;
import org.apache.commons.io.FileUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
public class DownloadController {

    @Resource
    BikaUtils bikaUtils;

    @GetMapping("/download/key/{key}")
    public RestResponse downloadByKey(@PathVariable("key") String key) {
        bikaUtils.search(key, true);
        return RestResponse.ok().msg("下载提交成功");
    }

    @GetMapping("/download/id/{id}")
    public RestResponse downloadById(@PathVariable("id") String id) {
        bikaUtils.downloadById(id);
        return RestResponse.ok().msg("下载提交成功");
    }

    @GetMapping("/check")
    public RestResponse check() {
        checkZip("");
        return RestResponse.ok("检查完毕");
    }

    @PostMapping("/check")
    public RestResponse checkPath(@RequestBody String path) {
        checkZip(path);
        return RestResponse.ok("检查完毕");
    }

    @GetMapping("/check2")
    public RestResponse check2() {
        //不移动位置
        checkZip2();
        return RestResponse.ok("检查完毕");
    }

    @GetMapping("/echoNotExists")
    public void echoNotExists() {
        CrudTools crudTools = SpringContext.getBeanWithGenerics(CrudTools.class);
        List<Bika> bikas = crudTools.commonApiQueryBySql("select * from bika where is_deleted = 0 order by  likes_count desc", Bika.class);
        CopyOnWriteArrayList<Bika> copyOnWriteArrayList = new CopyOnWriteArrayList<>(bikas);
        Stream<Bika> bikaStream = copyOnWriteArrayList.stream().filter(bika -> !StringUtils.isEmpty(bika.getLocalPath())).filter(bika -> !new File(bika.getLocalPath()).exists());
        bikaStream.sorted((o1, o2) -> o2.getLikesCount() - o1.getLikesCount()).forEach(bika -> {
            File exists = new File(bika.getLocalPath());
            System.out.println(bika.getLocalPath());
            SaveLog.saveLog("M:\\" + exists.getName());
        });
    }

    public void checkZip3() {
        //筛选指定目录中的文件夹、压缩包是否在数据库中存在，如果不存在，则将文件夹或压缩包移动到存档点并更新数据库中的记录；如果存在，则比较eps_count（章节）数，章节数多的为最新，并将文件夹或压缩包添加.old后缀
        BikaUtils bikaUtils = SpringContext.getBeanWithGenerics(BikaUtils.class);
        Collection<File> files = FileUtils.listFiles(new File("J:\\bika"), new String[]{"zip"}, true);
        Map<String, List<File>> group = files.stream().collect(Collectors.groupingBy(file -> file.getName().substring(file.getName().indexOf(".") + 1)));
        for (Map.Entry<String, List<File>> entry : group.entrySet()) {
            Function<File, List<String>> callback = null;
//            BiFunction<File, Path, Path> after = bikaUtils::moveFile;
            BiFunction<File, Path, Path> after = (file, des) -> Paths.get(file.getPath());
            String o = entry.getKey();
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
            for (File file : entry.getValue()) {
                bikaUtils.bus(o, file, bikaUtils, callback, after);
            }
        }
    }

    public void checkZip2() {
        BikaUtils bikaUtils = SpringContext.getBeanWithGenerics(BikaUtils.class);
        Collection<File> files = FileUtils.listFiles(new File("N:\\bika"), new String[]{"zip"}, true);
        Map<String, List<File>> group = files.stream().collect(Collectors.groupingBy(file -> file.getName().substring(file.getName().lastIndexOf(".") + 1)));
        for (Map.Entry<String, List<File>> entry : group.entrySet()) {
            Function<File, List<String>> callback = null;
//            BiFunction<File, Path, Path> after = bikaUtils::moveFile;
            BiFunction<File, Path, Path> after = (file, des) -> Paths.get(file.getPath());
            String o = entry.getKey();
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
            for (File file : entry.getValue()) {
                bikaUtils.bus(o, file, bikaUtils, callback, after);
            }
        }
    }

    public void checkZip(String path) {
        //筛选指定目录中的文件夹、压缩包是否在数据库中存在，如果不存在，则将文件夹或压缩包移动到存档点并更新数据库中的记录；如果存在，则比较eps_count（章节）数，章节数多的为最新，并将文件夹或压缩包添加.old后缀
        BikaUtils bikaUtils = SpringContext.getBeanWithGenerics(BikaUtils.class);
        Collection<File> files = FileUtils.listFiles(new File(path), new String[]{"zip"}, false);
        Map<String, List<File>> group = files.stream().collect(Collectors.groupingBy(file -> file.getName().substring(file.getName().lastIndexOf(".") + 1)));
        for (Map.Entry<String, List<File>> entry : group.entrySet()) {
            Function<File, List<String>> callback = null;
            BiFunction<File, Path, Path> after = bikaUtils::moveFile;
            String o = entry.getKey();
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
            for (File file : entry.getValue()) {
                bikaUtils.bus(o, file, bikaUtils, callback, after);
            }
        }
//        group.forEach((o, fileList) -> {
//            Function<File, List<String>> callback = null;
//            BiFunction<File, Path, Path> after = bikaUtils::moveFile;
//            switch (o) {
//                case "zip":
//                    callback = ZipUtils::getZipChapter;
//                    break;
//                case "rar":
//                    callback = ZipUtils::getRarChapter;
//                    break;
//                case "tar":
//                    callback = ZipUtils::getTarChapter;
//                    break;
//                case "":
//                    callback = CommonIOUtils::getDirChapter;
//                    break;
//            }
//            bikaUtils.bus(o, fileList, bikaUtils, callback, after);
//        });
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
            } else {
                //如果数据库中没有，则查询bika_list表，如果有的话将记录复制到bika表中，并更新保存记录
                BikaList bikaList = bikaUtils.getFromBikaList(fileId);
                Bika bika = JSON.parseObject(JSON.toJSONString(bikaList), Bika.class);
                bika.setEpsCount(callback.apply(file).size());
                Path des = after.apply(file, bikaUtils.GetAvailablePath(file.length(), file));
                bika.setLocalPath(des.toString());
                crudTools.commonApiSave(bika);
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
}
