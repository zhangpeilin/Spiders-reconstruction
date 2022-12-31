package cn.zpl.spider.on.bika.thread;

import cn.zpl.common.bean.Bika;
import cn.zpl.common.bean.BikaDownloadFailed;
import cn.zpl.config.SpringContext;
import cn.zpl.spider.on.bika.common.BikaParams;
import cn.zpl.spider.on.bika.utils.BikaUtils;
import cn.zpl.thread.CommonThread;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.CrudTools;
import cn.zpl.util.DownloadTools;
import cn.zpl.util.ZipUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class BikaComicThread extends BikaCommonThread {

    private final String comicId;
    private final boolean isNeedDownload;
    CrudTools bikaCrudTools;


    public BikaComicThread(String comicId, boolean isNeedDownload) {
        this.comicId = comicId;
        this.isNeedDownload = isNeedDownload;
        this.bikaCrudTools = SpringContext.getBeanWithGenerics(CrudTools.class);
    }

    @Override
    public boolean doWhenFailed(Exception e) {
        log.error("下载失败，错误原因：", e);
        log.error(comicId + "下载失败，记录日志");
        BikaDownloadFailed failed = new BikaDownloadFailed();
        failed.setId(comicId);
        failed.setDownloadAt(String.valueOf(System.currentTimeMillis()));
        failed.setError(e.getMessage());
        if (BikaParams.writeDB)
            bikaCrudTools.commonApiSave(failed);
        if (e.getMessage().contains("错误代码：400")) {
            return false;
        }
        return super.doWhenFailed(e);
    }

    public void domain() {
        //获取画册信息
        String getComicsInfo = "comics/" + comicId;
        if (!bikaUtils.isNeedUpdate(comicId) && !BikaParams.isForceDownload) {
            //删除错误日志表的记录
            BikaDownloadFailed failed = new BikaDownloadFailed();
            failed.setId(comicId);
            if (BikaParams.writeDB)
//            DBManager.delete(failed);
                CrudTools.commonApiDelete("", BikaDownloadFailed.class);
            log.debug(comicId + "漫画已下载且上次更新日期在7天内，跳过");
            return;
        }
        JsonObject info = bikaUtils.getJsonByUrl(getComicsInfo);
        if (!BikaParams.isForceDownload && bikaUtils.needSkip(info)) {
            log.debug(comicId + "跳过");
            return;
        }

        if (!isNeedDownload) {
            if (BikaParams.writeDB) {
                bikaUtils.dosave(comicId, info, isNeedDownload, "");
            }
            return;
        }

        String title = CommonIOUtils.filterFileName(CommonIOUtils.getFromJson2Str(info, "data-comic-title"));
        Bika exist = bikaUtils.getExists(comicId);
        //判断是否存在id编号相同但文件夹名不同的目录
        if (exist != null && exist.getLocalPath() != null && !"".equals(exist.getLocalPath())) {
            //数据库记录的文件夹路径
            File ex = new File(exist.getLocalPath());
            File existZip = new File(ex.getPath() + ".zip");
            //新路径
            File newDir = new File(ex.getParent(), BikaUtils.getFolder(comicId, title));
            File newZip = new File(newDir.getPath() +  ".zip");
            //如果新旧压缩包路径不同，则开始修改
            if (!existZip.getPath().equalsIgnoreCase(newZip.getPath())) {
                if (existZip.exists()) {
                    //旧压缩包目录修改为新压缩包目录
                    if (!existZip.renameTo(newZip)) {
                        log.error("更名失败，原路径：{}，新路径：{}", existZip, newZip);
                        getDoRetry().setRetryMaxCount(20);
                        throw new RuntimeException("更名失败");
                    } else {
                        //修改压缩包内目录为新目录
                        ZipUtils.renameFolderInZip(newZip.getPath(), ex.getName(), newDir.getName());
                        exist.setLocalPath(newDir.getPath());
                        //将压缩包中所有list.txt文件解压到目录中
                        try (ZipFile zipFile = new ZipFile(newZip)) {
                            zipFile.setCharset(Charset.forName("gbk"));
                            List<FileHeader> fileHeaders = zipFile.getFileHeaders();
                            for (FileHeader fileHeader : fileHeaders) {
                                if (fileHeader.getFileName().endsWith("list.txt")) {
                                    zipFile.extractFile(fileHeader, newDir.getParentFile().getPath());
                                }
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        if (!bikaCrudTools.commonApiSave(exist).isSuccess()) {
                            log.error("保存失败" + exist);
                        }
                    }
                }
                if (Files.exists(Paths.get(newDir.getPath()), LinkOption.NOFOLLOW_LINKS)) {
                    log.debug("文件已更名，更新数据库");
                    //清空缓存中指定的key值，下次使用时从数据库中重新加载
                    bikaUtils.invalidCache(comicId);
                } else {
                    throw new RuntimeException("未找到本地文件夹，不再重试，请核实");
                }
            }
        }

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
                            title, comicId));
                }
                tool.shutdown();
            }
        }
        if (BikaParams.writeDB) {
            bikaUtils.dosave(comicId, info, isNeedDownload, BikaUtils.getLocalPath(comicId, title));
        }
        //将新下载的内容写入压缩包中
        Bika bika = bikaUtils.getExists(comicId);
        try (ZipFile zipFile = new ZipFile(bika.getLocalPath() + ".zip")) {
            ZipParameters zipParameters = new ZipParameters();
            File file = new File(bika.getLocalPath());
            File[] files = file.listFiles((dir, name) -> dir.isFile());
            assert files != null;
            for (File tmp : files) {
                zipFile.addFile(tmp);
            }
            zipFile.addFolder(new File(bika.getLocalPath()), zipParameters);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
