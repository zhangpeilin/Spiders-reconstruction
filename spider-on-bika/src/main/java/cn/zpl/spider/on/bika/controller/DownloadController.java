package cn.zpl.spider.on.bika.controller;

import cn.zpl.common.bean.Bika;
import cn.zpl.common.bean.BikaList;
import cn.zpl.common.bean.RestResponse;
import cn.zpl.config.SpringContext;
import cn.zpl.spider.on.bika.bs.BikaBusiness;
import cn.zpl.spider.on.bika.common.BikaProperties;
import cn.zpl.spider.on.bika.thread.BikaComicThread;
import cn.zpl.spider.on.bika.utils.BikaUtils;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.CrudTools;
import cn.zpl.util.DownloadTools;
import cn.zpl.util.SaveLog;
import cn.zpl.util.ZipUtils;
import com.alibaba.fastjson.JSON;
import org.apache.commons.io.FileUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@RestController
public class DownloadController {

    @Resource
    BikaUtils bikaUtils;

    @Resource
    BikaProperties properties;

    @Resource
    CrudTools tools;

    @Resource
    BikaBusiness bikaBusiness;

    @GetMapping("/download/key/{key}")
    public RestResponse downloadByKey(@PathVariable("key") String key) {
        bikaUtils.search(key, true);
        return RestResponse.ok().msg("下载提交成功");
    }

    @GetMapping("download/H24")
    public RestResponse downloadH24() {
        bikaUtils.H24();
        return RestResponse.ok("H24下载提交成功");
    }
    @GetMapping("/updateAllExistBika")
    public RestResponse updateAllExistBika(@RequestParam String time) {
        bikaBusiness.updateAllExistBika(time);
        return RestResponse.ok("更新现存zip文件任务已提交");
    }

    @GetMapping(value = {"/download/id/{id}/{true}", "/download/id/{id}"})
    public RestResponse downloadById(@PathVariable("id") String id, @PathVariable(value = "true", required = false) String force) {
        bikaUtils.downloadById(id, !StringUtils.isEmpty(force) && "force".equalsIgnoreCase(force));
        return RestResponse.ok().msg("下载提交成功");
    }

    @GetMapping("download/Fav")
    public RestResponse Fav() {
        bikaUtils.favourite();
        return RestResponse.ok("H24下载提交成功");
    }

    @GetMapping("/cleanTemp")
    public RestResponse cleanTemp() {
        {
            File base = new File("D:\\bika_temp");
            for (File file : base.listFiles()) {
                String fileId = CommonIOUtils.getFileId(file.getName());
                if (!StringUtils.isEmpty(fileId)) {
                    bikaUtils.downloadById(fileId, true);
                }
            }
        }
        return RestResponse.ok().msg("批量提交成功");
    }

    @PostMapping("/downloadBySql/{count}/{like}")
    public RestResponse downloadBySql(@RequestBody(required = false) String sql, @PathVariable("count") String count, @PathVariable("like") String likeCount) {
        DownloadTools tool = DownloadTools.getInstance(5);
        tool.setName("漫画");
        tool.setSleepTimes(10000);
        sql = StringUtils.isEmpty(sql) ? "select * from bika_list t where likes_count > " + likeCount +
                " and local_path is null and not (categories like '%CG雜圖%' and pages_count > 100 ) and categories not like '%耽美花園%' and categories not like '%生肉%' and not exists(select 1 from bika_download_failed p where p.id = t.id)  order by likes_count desc limit " + count : sql;
        List<BikaList> list = tools.commonApiQueryBySql(sql, BikaList.class);
//        List<Bika> list = tools.commonApiQueryBySql("select * from bika t where likes_count > " + likeCount +
//                " and categories not like '%CG雜圖%' and categories not like '%耽美花園%' and categories not like '%生肉%' and not exists(select 1 from bika_download_failed p where p.id = t.id) and downloaded_at < 1692005906579  order by likes_count desc limit " + count, Bika.class);

        list.forEach(bikaList -> {
            BikaComicThread bikaComicThread = new BikaComicThread(bikaList.getId());
            bikaComicThread.setForceDownload(true);
            tool.ThreadExecutorAdd(bikaComicThread);
        });
        tool.shutdown();
        return RestResponse.ok().msg("更新提交成功");
    }

    @GetMapping("/search/key/{key}")
    public RestResponse search(@PathVariable("key") String key) {
        String result = bikaUtils.search(key, false);
        return RestResponse.ok().msg(result);
    }

    @GetMapping("/check")
    public RestResponse check() {
        checkZip("");
        return RestResponse.ok("检查完毕");
    }
    @GetMapping("/updateAllKinds")
    public RestResponse updateAllKinds() {
        bikaUtils.updateAllKinds();
        return RestResponse.ok("扫描开始");
    }


    @PostMapping("/check")
    public RestResponse checkPath(@RequestBody String path) {
        checkZip(path);
        return RestResponse.ok("检查完毕");
    }
    @PostMapping("/testBody")
    public RestResponse testBody(@RequestBody String path) {
        return RestResponse.ok(path);
    }


    @GetMapping("/check2")
    public RestResponse check2() {
        //不移动位置
        checkZip2();
        return RestResponse.ok("检查完毕");
    }

    /**
     * 更新记录的存储路径
     */
    @GetMapping("/updateSavePath")
    public RestResponse updateSavePath() {
        List<String> savePath = Collections.singletonList("g:\\bika");
        int batchSize = 200;
        for (String pathStr : savePath) {
            File path = new File(pathStr);
            CopyOnWriteArrayList<Bika> result = new CopyOnWriteArrayList<>();
            if (path.exists()) {
                Collection<File> files = FileUtils.listFiles(path, new String[]{"zip"}, true);
                files.stream().parallel().forEach(file -> {
                    String fileId = CommonIOUtils.getFileId(file);
                    if (StringUtils.isEmpty(fileId)) {
                        return;
                    }
                    Bika exist = bikaUtils.getBikaExist(fileId);
                    if (exist == null || exist.getLocalPath().equalsIgnoreCase(file.getPath())) {
                        return;
                    }
                    exist.setLocalPath(file.getPath());
                    result.add(exist);
                });
                IntStream.iterate(0, n -> n + batchSize)
                        .limit((result.size() + batchSize - 1) / batchSize)
                        .parallel()
                        .forEach(i -> tools.commonApiSave(result.subList(i, Math.min(i + batchSize, result.size()))));
            }
        }
        return RestResponse.ok("路径更新完毕");
    }

    @GetMapping("/echoNotExists")
    public void echoNotExists() {
        CrudTools crudTools = SpringContext.getBeanWithGenerics(CrudTools.class);
        List<Bika> bikas = crudTools.commonApiQueryBySql("select * from bika t", Bika.class);
        CopyOnWriteArrayList<Bika> copyOnWriteArrayList = new CopyOnWriteArrayList<>(bikas);
        Stream<Bika> bikaStream = copyOnWriteArrayList.stream().filter(bika -> !StringUtils.isEmpty(bika.getLocalPath())).filter(bika -> !new File(bika.getLocalPath()).exists());
        bikaStream.sorted((o1, o2) -> o2.getLikesCount() - o1.getLikesCount()).forEach(bika -> {
            File exists = new File(bika.getLocalPath());
            System.out.println(bika.getLocalPath());
            BikaList fromBikaList = bikaUtils.getFromBikaList(bika.getId());
            if (fromBikaList != null) {
                fromBikaList.setLocalPath("99999");
                crudTools.commonApiSave(fromBikaList);
            }
            bika.setLocalPath("99999");
            crudTools.commonApiSave(bika);
            SaveLog.saveLog("h:\\" + exists.getName());
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
        Collection<File> files = FileUtils.listFiles(new File("E:\\bika"), new String[]{"zip"}, true);
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
