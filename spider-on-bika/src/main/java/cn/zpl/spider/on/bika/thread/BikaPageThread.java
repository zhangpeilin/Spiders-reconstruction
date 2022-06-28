package cn.zpl.spider.on.bika.thread;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import common.BikaParams;
import dao.DBManager;
import dto.BikaList;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import util.BikaUtils;
import utils.CommonIOUtils;
import utils.io.DownloadTools;

import java.net.URLEncoder;
import java.util.Vector;

public class BikaPageThread implements Runnable {

    private int page;
    private Logger logger = BikaParams.logger;

    private String keyword;
    private boolean isNeedDownload;

    @Contract(pure = true)
    public BikaPageThread(int page, String keyword, boolean isNeedDownload) {
        this.page = page;
        this.keyword = keyword;
        this.isNeedDownload = isNeedDownload;
    }

    @Override
    public void run() {
        try {
            domain();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("线程异常：第" + page + "页分析出错，重新分析");
            run();
        }
    }

    public void domain() {
        try {
            String part;
            Vector<BikaList> listVector = new Vector<>();
            part = "comics?page=" + page + "&c=" + URLEncoder.encode(keyword, "utf-8") + "&s=ua";
            JsonObject partJson = BikaUtils.getJsonByUrl(part);
            JsonElement comics = CommonIOUtils.getFromJson2(partJson, "data-comics-docs");
            if (comics instanceof JsonArray) {
                DownloadTools tool = DownloadTools.getInstance(20, BikaParams.logger);
                tool.setName(keyword + "第" + page + "页");
                tool.setSleepTimes(2000);
                for (JsonElement detail : comics.getAsJsonArray()) {
                    if (isNeedDownload) {
                        tool.ThreadExecutorAdd(new BikaComicThread(detail.getAsJsonObject().get("_id").getAsString(), isNeedDownload));
                    } else {
                        listVector.add(saveComicInfo(detail));
                    }
                }
                logger.debug(keyword + "第" + page + "页");
                if (BikaParams.writeDB){
                    DBManager.batchInsert(listVector);
                }
                tool.shutdown();
            }
        } catch (Exception e) {
            e.printStackTrace();
            domain();
        }
    }

    @NotNull
    private BikaList saveComicInfo(JsonElement jsonElement) {
        BikaList bikaList = new BikaList();
        bikaList.setId(CommonIOUtils.getFromJson2Str(jsonElement, "id"));
        BikaList exists = DBManager.getDTOById2(BikaList.class, CommonIOUtils.getFromJson2Str(jsonElement, "id"));
        if (exists != null) {
            bikaList = exists;
        }
        bikaList.setTitle(CommonIOUtils.getFromJson2Str(jsonElement, "title"));
        bikaList.setAuthor(CommonIOUtils.getFromJson2Str(jsonElement, "author"));
        bikaList.setPagesCount(CommonIOUtils.getFromJson2Integer(jsonElement, "pagesCount"));
        bikaList.setEpsCount(CommonIOUtils.getFromJson2Integer(jsonElement, "epsCount"));
        bikaList.setFinished(CommonIOUtils.getFromJson2Boolean(jsonElement, "finished") ? 1 : 0);
        bikaList.setCategories(CommonIOUtils.getFromJson2(jsonElement, "categories").toString());
        bikaList.setLikesCount(CommonIOUtils.getFromJson2Integer(jsonElement, "likesCount"));
        return bikaList;
    }
}
