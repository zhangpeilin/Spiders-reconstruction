package cn.zpl.spider.on.bika.thread;

import cn.zpl.common.bean.Bika;
import cn.zpl.common.bean.BikaDownloadFailed;
import cn.zpl.spider.on.bika.common.BikaParams;
import cn.zpl.spider.on.bika.utils.BikaUtils;
import cn.zpl.thread.CommonThread;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.CrudTools;
import cn.zpl.util.DownloadTools;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;

@Slf4j
public class BikaComicThread extends CommonThread {

    private final String comicId;
    private final boolean isNeedDownload;
    private final Bika data = new Bika();

    public BikaComicThread(String comicId, boolean isNeedDownload) {
        this.comicId = comicId;
        this.isNeedDownload = isNeedDownload;
        data.setId(comicId);
    }

//    @Override
//    public void run() {
//        try {
//            domain();
//        } catch (RuntimeException runtimeException) {
//
//            if (data.canDoRetry()) {
//                data.doRetry();
//                run();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error("线程异常：" + comicId + "章节分析出错，重新分析");
//            if (data.canDoRetry()) {
//                data.doRetry();
//                run();
//            }
//        }
//    }

    @Override
    public boolean doWhenFailed(Exception e) {
        e.printStackTrace();
        log.error(comicId + "下载失败，记录日志");
        BikaDownloadFailed failed = new BikaDownloadFailed();
        failed.setId(comicId);
        failed.setDownloadAt(String.valueOf(System.currentTimeMillis()));
        failed.setError(e.getMessage());
        if (BikaParams.writeDB)
            CrudTools.commonApiSave(failed);
        if (e.getMessage().contains("错误代码：400")) {
            return false;
        }
        return super.doWhenFailed(e);
    }

    public void domain() {
        //获取画册信息
        String getComicsInfo = "comics/" + comicId;
        if (!BikaUtils.isNeedUpdate(comicId) && !BikaParams.isForceDownload) {
            //删除错误日志表的记录
            BikaDownloadFailed failed = new BikaDownloadFailed();
            failed.setId(comicId);
            if (BikaParams.writeDB)
//            DBManager.delete(failed);
            CrudTools.commonApiDelete("", BikaDownloadFailed.class);
            log.debug(comicId + "漫画已下载且上次更新日期在7天内，跳过");
            return;
        }
        JsonObject info = BikaUtils.getJsonByUrl(getComicsInfo);
        if (!BikaParams.isForceDownload && BikaUtils.needSkip(info)) {
            log.debug(comicId + "跳过");
            return;
        }

        if (!isNeedDownload) {
            if (BikaParams.writeDB){
                BikaUtils.dosave(comicId, info, isNeedDownload, "");
            }
            return;
        }

        String title = CommonIOUtils.filterFileName(CommonIOUtils.getFromJson2Str(info, "data-comic-title"));
        Bika exist = BikaUtils.getExists(comicId);
        //判断是否存在id编号相同但文件夹名不同的目录
        if (exist != null && exist.getLocalPath() != null && !"".equals(exist.getLocalPath())) {
            File ex = new File(exist.getLocalPath());
            File tmp = new File(ex.getParent(), BikaUtils.getFolder(exist));
            if (!ex.getPath().equalsIgnoreCase(tmp.getPath())) {
                if (ex.exists()) {
                    if (!ex.renameTo(tmp)) {
                        log.error("更名失败");
                        log.error(ex + "--->" + tmp);
                        getDoRetry().setRetryMaxCount(20);
                        throw new RuntimeException("更名失败");
                    } else {
                        exist.setLocalPath(tmp.getPath());
                        if (!CrudTools.commonApiSave(exist).isSuccess()) {
                            log.error("保存失败" + exist);
                        }
                    }
                }
                if (Files.exists(Paths.get(tmp.getPath()), LinkOption.NOFOLLOW_LINKS)) {
                    log.debug("文件已更名，更新数据库");
                    exist.setLocalPath(tmp.getPath());
//                    DBManager.ForceSave(exist);
                    if (!CrudTools.commonApiSave(exist).isSuccess()) {
                        log.error("保存失败" + exist);
                    }
                    BikaUtils.exists.clear();
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
            JsonObject chapters = BikaUtils.getJsonByUrl(getChapters);

            maxPage = CommonIOUtils.getFromJson2(chapters, "data-eps-pages").getAsInt();
            JsonElement chapter_list = CommonIOUtils.getFromJson2(chapters, "data-eps-docs");
            if (chapter_list instanceof JsonArray) {

                DownloadTools tool = DownloadTools.getInstance(2);
                tool.setName(title);
                tool.setSleepTimes(2000);
                for (JsonElement detail : chapter_list.getAsJsonArray()) {
                    tool.ThreadExecutorAdd(new BikaChapterThread(detail.getAsJsonObject().get("order").getAsString(),
                            title, comicId));
                }
                tool.shutdown();
            }
        }
        if (BikaParams.writeDB)
        BikaUtils.dosave(comicId, info, isNeedDownload, BikaUtils.getLocalPath(comicId, title));
    }
}
