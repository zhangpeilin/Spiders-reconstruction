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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import javax.annotation.Resource;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class BikaComicThread extends BikaCommonThread {

    private final String comicId;
    private final boolean isNeedDownload;
    CrudTools bikaCrudTools;
    BikaParams bikaParams;


    public BikaComicThread(String comicId, boolean isNeedDownload) {
        this.comicId = comicId;
        this.isNeedDownload = isNeedDownload;
        this.bikaCrudTools = SpringContext.getBeanWithGenerics(CrudTools.class);
        this.bikaParams = SpringContext.getBeanWithGenerics(BikaParams.class);
    }

    @Override
    public boolean doWhenFailed(Exception e) {
        log.error("下载失败，错误原因：", e);
        log.error(comicId + "下载失败，记录日志");
        BikaDownloadFailed failed = new BikaDownloadFailed();
        failed.setId(comicId);
        failed.setDownloadAt(String.valueOf(System.currentTimeMillis()));
        failed.setError(e.getMessage());
        if (bikaParams.isWriteDB()){
            bikaCrudTools.commonApiSave(failed);
        }
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
            if (bikaParams.isWriteDB())
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
            if (bikaParams.isWriteDB()) {
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
            File existZip = new File(ex.getPath());
            //新路径
            File newDir = new File(ex.getParent(), BikaUtils.getFolder(comicId, title));
            File newZip = new File(newDir.getPath() + ".zip");
            //如果存档文件存在，则解压list.txt:
            //则将压缩包中list.txt文件解压到存档目录中，并将该位置设为下载路径;
            //下载完成后，将新增内容和源压缩包中内容拷贝到新的临时zip中，临时zip中使用新的目录存放，拷贝完成后旧zip删除解压路径删除，临时zip重命名为新名称，更新数据库
            if (existZip.exists()) {
                //将压缩包中所有list.txt文件解压到目录中
                try (ZipFile zipFile = new ZipFile(existZip)) {
                    zipFile.setCharset(Charset.forName("gbk"));
                    List<FileHeader> fileHeaders = zipFile.getFileHeaders();
                    for (FileHeader fileHeader : fileHeaders) {
                        if (fileHeader.getFileName().endsWith("list.txt")) {
                            zipFile.extractFile(fileHeader, existZip.getParentFile().getPath());
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (exist == null || !new File(exist.getLocalPath()).exists()){
            //如果数据库中没有记录，或者记录位置没有文件，保存记录并且预置保存位置
            bikaUtils.dosave(comicId, info, isNeedDownload, bikaUtils.GetAvailablePath(0, null) + File.separator + BikaUtils.getFolder(comicId, title) + ".zip");
            bikaUtils.invalidCache(comicId);
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
//        if (bikaParams.isWriteDB()) {
//            bikaUtils.dosave(comicId, info, isNeedDownload, BikaUtils.getLocalPath(comicId, title));
//        }
        //将新下载的内容写入压缩包中
        long start;
        Bika bika = bikaUtils.getExists(comicId);
        File existZip = new File(bika.getLocalPath());
        File downloadDir = new File(existZip.getPath().replace(".zip", ""));
        Collection<File> files = FileUtils.listFiles(downloadDir, null, true);
        //遍历目录，如果有新增的目录，则解压所有文件，然后重新压缩
        File any = files.stream().filter(file -> file.isFile() && !file.getName().endsWith(".txt")).findFirst().orElse(null);
        start = System.currentTimeMillis();
        if (any != null) {
            //如果原路径不包含新目录，则新zip中使用新目录结构
            Map<String, String> replace = null;
            if (!bika.getLocalPath().contains(BikaUtils.getFolder(comicId, title))) {
                replace = new HashMap<String, String>(){{
                    put(existZip.getName().replace(".zip", ""), BikaUtils.getFolder(comicId, title));
                }};
            }
            //如果压缩包存在，则追加，否则新建压缩包
            if (existZip.exists()) {
                //将新下载的文件放入压缩包
                ZipUtils.append2Zip(bika.getLocalPath().replace(".zip", ""), bika.getLocalPath(), replace);
            } else {
                ZipUtils.compressFolder2Zip(downloadDir.toString(), bika.getLocalPath());
            }
            //重命名压缩包
            File newZip = new File(existZip.getParent(), BikaUtils.getFolder(comicId, title) + ".zip");
            boolean renameTo = existZip.renameTo(newZip);
            if (!renameTo) {
                log.error("重命名失败，保存原名称");
            }  //                bika.setLocalPath(newZip.getPath());
            //                bika.setTitle(title);
            //                bikaCrudTools.commonApiSave(bika);

        }
        try {
            FileUtils.deleteDirectory(downloadDir);
        } catch (IOException e) {
            log.error("删除下载目录失败");
            throw new RuntimeException(e);
        }
        if (bikaParams.isWriteDB()) {
            bikaUtils.dosave(comicId, info, isNeedDownload, bika.getLocalPath());
        }
        //如果压缩包已存在，则将新下载的和原有zip合并到新zip中
//        if (existZip.exists()) {
//            //如果原路径不包含新目录，则新zip中使用新目录结构
//            if (!bika.getLocalPath().contains(BikaUtils.getFolder(comicId, title))) {
//                ZipUtils.append2Zip(bika.getLocalPath().replace(".zip", ""), bika.getLocalPath(), new HashMap<String, String>(){{
//                    put(existZip.getName().replace(".zip", ""), BikaUtils.getFolder(comicId, title));
//                }});
//            }
//        }
//        try (ZipFile zipFile = new ZipFile(bika.getLocalPath() + ".zip")) {
//            zipFile.setCharset(Charset.forName("gbk"));
//            ZipParameters zipParameters = new ZipParameters();
//            File file = new File(bika.getLocalPath());
//            File[] dirs = file.listFiles((dir, name) -> dir.isDirectory() && !name.endsWith("txt"));
//            //遍历目录，如果有新增的目录，则解压所有文件，然后重新压缩
//            assert dirs != null;
//            Optional<File> any = Arrays.stream(dirs).filter(dir -> {
//                File imageFile = FileUtils.listFiles(dir, null, true).stream().filter(tmp -> tmp.isFile() && !tmp.getName().endsWith(".txt")).findFirst().orElse(null);
//                return imageFile != null;
//            }).findAny();
//            start = System.currentTimeMillis();
//            if (any.isPresent()) {
//                //将压缩包释放
//                zipFile.extractAll(new File(bika.getLocalPath()).getParent());
//            }
////            FileUtils.listFiles(file, null, true);
////            Collection<File> files = FileUtils.listFiles(file, null, true);
////            遍历添加文件速度太慢
////            for (File tmp : files) {
////                zipFile.addFile(tmp);
////            }
//            //不删除原压缩包耗时：19400
//            //删除原压缩包耗时：14480
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }
}
