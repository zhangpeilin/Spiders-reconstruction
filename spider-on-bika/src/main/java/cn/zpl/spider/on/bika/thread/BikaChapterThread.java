package cn.zpl.spider.on.bika.thread;


import cn.zpl.common.bean.Bika;
import cn.zpl.pojo.DownloadDTO;
import cn.zpl.pojo.SynchronizeLock;
import cn.zpl.spider.on.bika.utils.BikaUtils;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.DownloadTools;
import cn.zpl.util.SaveLog;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Vector;

@Slf4j
public class BikaChapterThread implements Runnable {

    private final String chapternum;

    private final String title;

    private final String comicid;

    private final String chapterPath;

    BikaChapterThread(String chapternum, String title, String comicid) {
        this.chapternum = chapternum;
        this.title = title;
        this.comicid = comicid;
        //路径不在固定，由数据库记录的路径确定上层目录
        Bika exist = BikaUtils.getExists(comicid);
        if (exist != null && exist.getLocalPath() != null && !"".equals(exist.getLocalPath())) {
            this.chapterPath = exist.getLocalPath() + "\\" + chapternum;
        } else {
            this.chapterPath = BikaUtils.defaultSavePath + "\\(" + comicid + ")" + title + "\\" + chapternum;
        }
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

            String getImgs = "comics/" + comicid + "/order/" + chapternum + "/pages?page=" + i;
            JsonObject imgJson = BikaUtils.getJsonByUrl(getImgs);
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
//                if (dtoVector.isEmpty()) {
//                    BikaEmptyChapterRecordsEntity entity = new BikaEmptyChapterRecordsEntity();
//                    entity.setId(comicid);
//                    entity.setUrl(getImgs);
//                    entity.setReason("章节图片数为0，跳过待检");
//                    if (BikaParams.writeDB)
//                    DBManager.ForceSave(entity);
//                }
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
        tools.setLock(lock);
        tools.setName(title + "的images");
        dtoVector.forEach(dto -> {
            dto.setSynchronizeLock(lock);
            dto.setProgress(BikaUtils.progress);
//            tools.ThreadExecutorAdd(new OneFileOneThread2(dto));
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
