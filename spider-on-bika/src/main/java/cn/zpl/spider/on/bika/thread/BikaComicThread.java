package cn.zpl.spider.on.bika.thread;

import cn.zpl.entities.Bika;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import common.BikaParams;
import dao.DBManager;
import dto.BikaDownloadFailed;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import util.BikaUtils;
import utils.CommonIOUtils;
import utils.io.DownloadTools;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;

public class BikaComicThread implements Runnable {

    private String comicid;
    private boolean isNeedDownload;
    private Bika data = new Bika();
    private static Logger logger = BikaParams.logger;

    public BikaComicThread(String comicid, boolean isNeedDownload) {
        this.comicid = comicid;
        this.isNeedDownload = isNeedDownload;
        data.setId(comicid);
        data.setRetryCount(3);
    }

    public void customize(@NotNull Bika bika) {
        data.setForceDownload(bika.isForceDownload());
    }

    @Override
    public void run() {
        try {
            domain();
        } catch (RuntimeException ee) {
            ee.printStackTrace();
            logger.error(comicid + "下载失败，记录日志");
            BikaDownloadFailed failed = new BikaDownloadFailed();
            failed.setId(comicid);
            failed.setDownloadAt(String.valueOf(System.currentTimeMillis()));
            failed.setError(ee.getMessage());
            if (BikaParams.writeDB)
            DBManager.update(failed);
            if (ee.getMessage().contains("错误代码：400")) {
                return;
            }
            if (data.canDoRetry()) {
                data.doRetry();
                run();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("线程异常：" + comicid + "章节分析出错，重新分析");
            if (data.canDoRetry()) {
                data.doRetry();
                run();
            }
        }
    }

    public void domain() {
        //获取画册信息
        String getComicsInfo = "comics/" + comicid;
        if (!BikaUtils.isNeedUpdate(comicid) && !data.isForceDownload()) {
            //删除错误日志表的记录
            BikaDownloadFailed failed = new BikaDownloadFailed();
            failed.setId(comicid);
            if (BikaParams.writeDB)
            DBManager.delete(failed);
            logger.debug(comicid + "漫画已下载且上次更新日期在7天内，跳过");
            return;
        }
        JsonObject info = BikaUtils.getJsonByUrl(getComicsInfo);
        if (!data.isForceDownload() && BikaUtils.needSkip(info)) {
            logger.debug(comicid + "跳过");
            return;
        }

        if (!isNeedDownload) {
            if (BikaParams.writeDB){
                BikaUtils.dosave(comicid, info, isNeedDownload, "");
            }
            return;
        }

        String title = CommonIOUtils.filterFileName(CommonIOUtils.getFromJson2Str(info, "data-comic-title"));
        Bika exist = BikaUtils.getExists(comicid);
        //判断是否存在id编号相同但文件夹名不同的目录
        if (exist != null && exist.getLocalPath() != null && !"".equals(exist.getLocalPath())) {
            File ex = new File(exist.getLocalPath());
            File tmp = new File(ex.getParent(), BikaUtils.getFolder(exist));
            if (!ex.getPath().equalsIgnoreCase(tmp.getPath())) {
                if (ex.exists()) {
                    if (!ex.renameTo(tmp)) {
                        logger.error("更名失败");
                        logger.error(ex + "--->" + tmp);
                        data.setRetryCount(100);
                        throw new RuntimeException("更名失败");
                    } else {
                        exist.setLocalPath(tmp.getPath());
//                    DBManager.ForceSave(exist);
                        if (!BikaUtils.saveBika(exist)) {
                            logger.error("保存失败" + exist);
                        }                    }
                }
                if (Files.exists(Paths.get(tmp.getPath()), LinkOption.NOFOLLOW_LINKS)) {
                    logger.debug("文件已更名，更新数据库");
                    exist.setLocalPath(tmp.getPath());
//                    DBManager.ForceSave(exist);
                    if (!BikaUtils.saveBika(exist)) {
                        logger.error("保存失败" + exist);
                    }
                    BikaUtils.exists.clear();
                } else {
                    data.setRetryCount(100);
                    throw new RuntimeException("未找到本地文件夹，不再重试，请核实");
                }
            }
        }

        //获取所有章节列表
        int page = 1;
        int maxPage = 1;
        for (; page <= maxPage; page++) {
            String getChapters = "comics/" + comicid + "/eps?page=" + page;
            JsonObject chapters = BikaUtils.getJsonByUrl(getChapters);

            maxPage = CommonIOUtils.getFromJson2(chapters, "data-eps-pages").getAsInt();
            JsonElement chapter_list = CommonIOUtils.getFromJson2(chapters, "data-eps-docs");
            if (chapter_list instanceof JsonArray) {

                DownloadTools tool = DownloadTools.getInstance(2, BikaParams.logger);
                tool.setName(title);
                tool.setSleepTimes(2000);
                for (JsonElement detail : chapter_list.getAsJsonArray()) {
                    tool.ThreadExecutorAdd(new BikaChapterThread(detail.getAsJsonObject().get("order").getAsString(),
                            title, comicid));
                }
                tool.shutdown();
            }
        }
        if (BikaParams.writeDB)
        BikaUtils.dosave(comicid, info, isNeedDownload, BikaUtils.getLocalPath(comicid, title));
    }
}
