package cn.zpl.spider.on.bika.thread;

import cn.zpl.common.bean.Bika;
import cn.zpl.common.bean.BikaList;
import cn.zpl.config.SpringContext;
import cn.zpl.spider.on.bika.common.BikaParams;
import cn.zpl.spider.on.bika.utils.BikaUtils;
import cn.zpl.thread.CommonThread;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.CrudTools;
import cn.zpl.util.DownloadTools;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.activation.DataHandler;
import javax.annotation.Resource;
import java.net.URLEncoder;
import java.util.Vector;

@Slf4j
public class BikaPageThread extends BikaCommonThread {

    private final int page;

    private final String keyword;
    private final boolean isNeedDownload;

    CrudTools tools;


    public BikaPageThread(int page, String keyword, boolean isNeedDownload) {
        this.page = page;
        this.keyword = keyword;
        this.isNeedDownload = isNeedDownload;
    }

    @SneakyThrows
    public void domain() {
        try {
            String part;
            Vector<BikaList> listVector = new Vector<>();
            part = "comics?page=" + page + "&c=" + URLEncoder.encode(keyword, "utf-8") + "&s=ua";
            JsonObject partJson = bikaUtils.getJsonByUrl(part);
            JsonElement comics = CommonIOUtils.getFromJson2(partJson, "data-comics-docs");
            if (comics instanceof JsonArray) {
                DownloadTools tool = DownloadTools.getInstance(20);
                tool.setName(keyword + "第" + page + "页");
                tool.setSleepTimes(2000);
                for (JsonElement detail : comics.getAsJsonArray()) {
                    if (isNeedDownload) {
                        tool.ThreadExecutorAdd(new BikaComicThread(detail.getAsJsonObject().get("_id").getAsString(), isNeedDownload));
                    } else {
                        listVector.add(saveComicInfo(detail));
                    }
                }
                log.debug(keyword + "第" + page + "页");
                if (BikaParams.writeDB){
                    tools.commonApiSave(listVector);
                }
                tool.shutdown();
            }
        } catch (Exception e) {
            log.error("线程异常：第" + page + "页分析出错，重新分析");
            log.error("错误原因", e);
            throw e;
        }
    }

    @NotNull
    private BikaList saveComicInfo(JsonElement jsonElement) {
        BikaList bikaList = new BikaList();
        bikaList.setId(CommonIOUtils.getFromJson2Str(jsonElement, "id"));
        CommonIOUtils.getFromJson2Str(jsonElement, "id");
        bikaList.setTitle(CommonIOUtils.getFromJson2Str(jsonElement, "title"));
        bikaList.setAuthor(CommonIOUtils.getFromJson2Str(jsonElement, "author"));
        bikaList.setPagesCount(CommonIOUtils.getFromJson2Integer(jsonElement, "pagesCount"));
        bikaList.setEpsCount(CommonIOUtils.getFromJson2Integer(jsonElement, "epsCount"));
        bikaList.setUpdateTime(CommonIOUtils.paraseSystemTime13(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss.SSS"));
        bikaList.setFinished(CommonIOUtils.getFromJson2Boolean(jsonElement, "finished") ? 1 : 0);
        bikaList.setCategories(CommonIOUtils.getFromJson2(jsonElement, "categories").toString());
        bikaList.setLikesCount(CommonIOUtils.getFromJson2Integer(jsonElement, "likesCount"));
        return bikaList;
    }
}
