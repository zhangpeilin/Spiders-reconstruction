package cn.zpl.spider.on.bika.thread;


import cn.zpl.common.bean.Bika;
import cn.zpl.config.SpringContext;
import cn.zpl.pojo.DownloadDTO;
import cn.zpl.pojo.SynchronizeLock;
import cn.zpl.spider.on.bika.common.BikaProperties;
import cn.zpl.spider.on.bika.utils.BikaUtils;
import cn.zpl.thread.OneFileOneThread;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.DownloadTools;
import cn.zpl.util.SaveLog;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;

@Slf4j
public class BikaChapterThread implements Runnable {

    private final String chapterNum;

    private final String title;

    private final String comicid;

    private final String chapterPath;
    BikaUtils utils;

    BikaProperties properties;

    BikaChapterThread(String chapterNum, String title, String comicId, Path downloadPath) {
        this.chapterNum = chapterNum;
        this.title = title;
        this.comicid = comicId;
        utils = SpringContext.getBeanWithGenerics(BikaUtils.class);
        properties = SpringContext.getBeanWithGenerics(BikaProperties.class);
        //路径不在固定，由数据库记录的路径确定上层目录
        this.chapterPath = downloadPath.resolve(chapterNum).toString();
    }

    @Override
    public void run() {
        try {
            if (SaveLog.isChapterCompelete(chapterPath)) {
                log.debug(chapterPath + "该章节已下载，跳过");
                return;
            }
            domain();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("线程异常：" + title + "章节分析出错，重新分析");
            domain();
        }
    }

    public void domain() {
        int i = 1;
        int count = 1;
        Vector<DownloadDTO> dtoVector = new Vector<>();
        while (true) {

            String getImgs = "comics/" + comicid + "/order/" + chapterNum + "/pages?page=" + i;
            JsonObject imgJson = utils.getJsonByUrl(getImgs);
            int max_age = CommonIOUtils.getFromJson2Integer(imgJson, "data-pages-pages");
            JsonElement img_list = CommonIOUtils.getFromJson2(imgJson, "data-pages-docs");
            for (JsonElement img_detail : img_list.getAsJsonArray()) {
                DownloadDTO dto = new DownloadDTO();
                dto.setId(comicid);
                dto.setProxy(true);
                String imgName = CommonIOUtils.getFromJson2Str(img_detail, "media-originalName");
                String type = imgName.substring(imgName.lastIndexOf("."));
                dto.setSavePath(chapterPath + "\\" + count + type);
                dto.setUrl("https://s3.picacomic.com/static/" +
                        CommonIOUtils.getFromJson2Str(img_detail, "media-path").replace("tobeimg/", ""));
                dto.setFileName(imgName);
                dtoVector.add(dto);
                count++;
            }
            i++;
            if (i > max_age) {
                break;
            }
        }
        //如果章节为空，记录日志并直接返回
        if (dtoVector.isEmpty()) {
            return;
        }
        SynchronizeLock lock = new SynchronizeLock();
        DownloadTools tools = DownloadTools.getInstance(10);
        tools.setSleepTimes(2000);
        tools.setName(title + "的第" + chapterNum + "章");
        tools.removeFromCache();
        dtoVector.forEach(dto -> {
            dto.setSynchronizeLock(lock);
            dto.setProgress(BikaUtils.progress);
            tools.ThreadExecutorAdd(new OneFileOneThread(dto));
        });
        //启动一个线程读取完成的次数
        tools.shutdown();
        //如果dtoList里面的内容有缺失的则章节日志跳过
        if (dtoVector.stream().anyMatch(downloadDTO -> !new File(downloadDTO.getSavePath()).exists())) {
            return;
        }
        SaveLog.saveLog(new File(chapterPath == null ? System.getProperty("user.home") : chapterPath));
    }
}
