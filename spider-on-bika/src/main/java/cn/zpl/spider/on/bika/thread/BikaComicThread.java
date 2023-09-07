package cn.zpl.spider.on.bika.thread;

import cn.zpl.common.bean.Bika;
import cn.zpl.common.bean.BikaDownloadFailed;
import cn.zpl.config.SpringContext;
import cn.zpl.spider.on.bika.common.BikaProperties;
import cn.zpl.spider.on.bika.utils.BikaUtils;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.CrudTools;
import cn.zpl.util.CruxIdGenerator;
import cn.zpl.util.DownloadTools;
import cn.zpl.util.ZipUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Data
public class BikaComicThread extends BikaCommonThread {

    private final String comicId;
//    private final boolean isNeedDownload;

    private boolean forceDownload = false;
    CrudTools crudTools;
    BikaProperties bikaProperties;


    public BikaComicThread(String comicId) {
        this.comicId = comicId;
        this.crudTools = SpringContext.getBeanWithGenerics(CrudTools.class);
        this.bikaProperties = SpringContext.getBeanWithGenerics(BikaProperties.class);
    }

    @Override
    public boolean doWhenFailed(Exception e) {
        log.error("下载失败，错误原因：", e);
        log.error(comicId + "下载失败，记录日志");
        BikaDownloadFailed failed = new BikaDownloadFailed();
        failed.setId(comicId);
        failed.setDownloadAt(String.valueOf(System.currentTimeMillis()));
        failed.setError(e.getMessage());
        if (bikaProperties.isWriteDb()){
            Bika exists = bikaUtils.getBikaExist(comicId);
            if (exists != null) {
                exists.setDownloadedAt(failed.getDownloadAt());
                crudTools.commonApiSave(exists);
            }
            crudTools.commonApiSave(failed);
        }
        if (e.getMessage().contains("错误代码：400")) {
            return false;
        }
        return super.doWhenFailed(e);
    }

    public void domain() {
        Thread.currentThread().setName(comicId);
        //获取画册信息
        String getComicsInfo = "comics/" + comicId;
        if (!bikaUtils.isNeedUpdate(comicId) && !forceDownload) {
            log.debug(comicId + "漫画已下载且上次更新日期在7天内，跳过");
            Bika bikaExist = bikaUtils.getBikaExist(comicId);
            bikaExist.setDownloadedAt(String.valueOf(System.currentTimeMillis()));
            crudTools.commonApiSave(bikaExist);
            return;
        }
        JsonObject info = bikaUtils.getJsonByUrl(getComicsInfo);
        if (!BikaProperties.isForceDownload && bikaUtils.needSkip(info)) {
            log.debug(comicId + "跳过");
            return;
        }

        String title = CommonIOUtils.filterFileName(CommonIOUtils.getFromJson2Str(info, "data-comic-title"));
        Bika exist = bikaUtils.getBikaExist(comicId);
        //默认目录，如果存在zip文件则替换为解压目录
        Path downloadPath = Paths.get(bikaProperties.getTempPath()).resolve(Paths.get(BikaUtils.getFolder(comicId, title)));
        //判断是否存在id编号相同但文件夹名不同的目录
        if (exist != null && exist.getLocalPath() != null && !"".equals(exist.getLocalPath())) {
            //数据库记录的文件夹路径
            File ex = new File(exist.getLocalPath());
            File existZip = new File(ex.getPath());
            //新路径
            //如果存档文件存在，则解压list.txt:
            //则将压缩包中list.txt文件解压到存档目录中，并将该位置设为下载路径;
            //下载完成后，将新增内容和源压缩包中内容拷贝到新的临时zip中，临时zip中使用新的目录存放，拷贝完成后旧zip删除解压路径删除，临时zip重命名为新名称，更新数据库
            if (existZip.exists()) {
                //如果压缩包校验不通过，则全部解压
                try {
                    Path unzipped;
                    if (!ZipUtils.checkZipStatus(existZip.getPath())) {
                        unzipped = ZipUtils.unzipFileWithCorruptErr(existZip.getPath(), bikaProperties.getTempPath());
                        FileUtils.delete(existZip);
                    } else {
                        unzipped = ZipUtils.unzipFile2Dir(ex.getPath(), bikaProperties.getTempPath(), "list.txt");
                    }
                    downloadPath = Paths.get(bikaProperties.getTempPath()).resolve(unzipped == null ? Paths.get(BikaUtils.getFolder(comicId, title)) : unzipped);
                } catch (IOException e) {
                    log.error("解压出错", e);
                    if (existZip.length() < 314572800) {
                        log.error("文件解压失败，文件小于300MB，删除后重新下载");
                        try {
                            FileUtils.delete(existZip);
                        } catch (IOException exc) {
                            throw new RuntimeException(exc);
                        }
                    } else {
                        log.error("文件解压失败，文件大于300MB，需要人工介入");
                        return;
                    }
                }
            }
        }
        if (exist == null || !new File(exist.getLocalPath()).exists()) {
            //如果数据库中没有记录，或者记录位置没有文件，保存记录并且预置保存位置
            bikaUtils.dosave(comicId, info, bikaUtils.GetAvailablePath(0, null) + File.separator + BikaUtils.getFolder(comicId, title) + ".zip");
            bikaUtils.invalidCache(comicId);
        }
        log.debug("当前保存目录：{}", downloadPath);

        //获取所有章节列表
        int page = 1;
        int maxPage = 1;
        for (; page <= maxPage; page++) {
            String getChapters = "comics/" + comicId + "/eps?page=" + page;
            JsonObject chapters = bikaUtils.getJsonByUrl(getChapters);

            maxPage = CommonIOUtils.getFromJson2(chapters, "data-eps-pages").getAsInt();
            JsonElement chapter_list = CommonIOUtils.getFromJson2(chapters, "data-eps-docs");
            if (chapter_list instanceof JsonArray) {

                DownloadTools tool = DownloadTools.getInstance(5);
                tool.setName(title);
                tool.setSleepTimes(2000);
                for (JsonElement detail : chapter_list.getAsJsonArray()) {
                    tool.ThreadExecutorAdd(new BikaChapterThread(detail.getAsJsonObject().get("order").getAsString(),
                            title, comicId, downloadPath));
                }
                tool.shutdown();
            }
        }
        //将新下载的内容写入压缩包中
        Bika bika = bikaUtils.getBikaExist(comicId);
        File existZip = new File(bika.getLocalPath());
        File downloadDir = downloadPath.toFile();
        if (!downloadDir.exists()) {
            log.warn("下载目录{}不存在，直接返回", downloadDir);
            return;
        }
        Collection<File> files = FileUtils.listFiles(downloadDir, null, true);
        //遍历目录，如果有新增的目录，则解压所有文件，然后重新压缩
        File any = files.stream().filter(file -> file.isFile() && !file.getName().endsWith(".txt")).findFirst().orElse(null);
        if (any != null) {
            //如果原路径不包含新目录，则新zip中使用新目录结构
            Map<String, String> replace = null;
            if (!bika.getLocalPath().contains(BikaUtils.getFolder(comicId, title))) {
                replace = new HashMap<String, String>() {{
                    put(existZip.getName().replace(".zip", ""), BikaUtils.getFolder(comicId, title));
                }};
            }
            //如果压缩包存在，则追加，否则新建压缩包
            if (existZip.exists()) {
                //将新下载的文件放入压缩包
                try {
                    ZipUtils.append2Zip(downloadDir.toString(), bika.getLocalPath(), replace);
                } catch (Exception e) {
                    log.error("目标压缩包无法追加文件，创建增量压缩包");
                    compressFolder2ZipAndMove(downloadDir, new File(existZip.getParentFile(), "增量压缩-" + existZip.getName()));
                }
                //如果追加文件失败，则走创建
            } else {
                compressFolder2ZipAndMove(downloadDir, existZip);
            }
            //重命名压缩包
            File newZip = new File(existZip.getParent(), BikaUtils.getFolder(comicId, title) + ".zip");
            boolean renameTo = existZip.renameTo(newZip);
            bika.setLocalPath(newZip.getPath());
            if (!renameTo) {
                log.error("重命名失败，保存原名称");
            }

        }
        try {
            FileUtils.deleteDirectory(downloadDir);
        } catch (IOException e) {
            log.error("删除下载目录失败");
            throw new RuntimeException(e);
        }
        if (bikaProperties.isWriteDb()) {
            bikaUtils.dosave(comicId, info, bika.getLocalPath());
        }
    }

    private void compressFolder2ZipAndMove(File downloadDir, File desFile) {
        //新创建压缩包在临时目录中
        String tempZip = Paths.get(bikaProperties.getTempPath()).resolve(CruxIdGenerator.generate() + ".zip").toString();
        ZipUtils.compressFolder2Zip(downloadDir.toString(), tempZip);
        try {
            if (desFile.exists()) {
                FileUtils.forceDelete(desFile);
            }
            FileUtils.moveFile(new File(tempZip), desFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
