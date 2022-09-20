package cn.zpl.spider.on.bika.utils;

import cn.zpl.common.bean.Bika;
import cn.zpl.spider.on.bika.common.BikaParams;
import cn.zpl.spider.on.bika.thread.BikaPageThread;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.CrudTools;
import cn.zpl.util.DownloadTools;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.util.Arrays;

@Slf4j
@SpringBootTest

public class ScanComics {

    @Resource
    CrudTools tools;

    @Resource
    BikaUtils bikaUtils;

    @Test
    public void main(){

        String keyword = "Cosplay|歐美|禁書目錄|WEBTOON|Fate|東方|SAO 刀劍神域|Love Live|艦隊收藏|非人類|強暴|NTR|人妻|重口地帶|足の恋|性轉換|SM|妹妹系|姐姐系|單行本|扶他樂園|後宮閃光|偽娘哲學|百合花園|純愛|英語 ENG|CG雜圖|碧藍幻想|圓神領域|短篇|同人|長篇|全彩|嗶咔漢化|";
        DownloadTools tool = DownloadTools.getInstance(30);
        Arrays.stream(keyword.split("\\|")).forEach(key -> {
            tool.setName("页面");
            tool.setSleepTimes(2000);
            int currentPage = 1;
            int maxpage = getMaxPage(key);
            do {
                tool.ThreadExecutorAdd(new BikaPageThread(currentPage, key, false));
                currentPage++;
            } while (currentPage <= maxpage);
        });
        tool.shutdown();
    }

    private int getMaxPage(String keyword) {
        int maxPage;
        try {
            String part;
            part = "comics?page=1&c=" + URLEncoder.encode(keyword, "utf-8") + "&s=ua";
            JsonObject partJson = bikaUtils.getJsonByUrl(part);
            maxPage = CommonIOUtils.getFromJson2Integer(partJson, "data-comics-pages");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取最大页数失败，重新获取");
            CommonIOUtils.waitSeconds(1);
            return getMaxPage(keyword);
        }
        return maxPage;
    }

    @Test
    public void test() {
        getMaxPage("NTR");
    }
}
