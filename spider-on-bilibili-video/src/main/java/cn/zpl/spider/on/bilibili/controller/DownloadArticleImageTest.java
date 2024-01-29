package cn.zpl.spider.on.bilibili.controller;

import cn.zpl.pojo.Data;
import cn.zpl.pojo.DownloadDTO;
import cn.zpl.thread.OneFileOneThread;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.DownloadTools;
import com.google.gson.JsonElement;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class DownloadArticleImageTest {

    public List<DownloadDTO> download(String articleId) {
//        Data data = new Data();
//        data.setUrl();
//        CommonIOUtils.withTimer(data);
        Document article = CommonIOUtils.getDocumentFromUrl("https://www.bilibili.com/read/cv" + articleId);
        Elements images = article.select("div#article-content figure.img-box img");
        Element title = article.selectFirst("h1.title");
        List<String> path = new ArrayList<>();
        path.add("d:\\朝雾星弦");
        assert title != null;
        path.add(title.text());
        List<DownloadDTO> list = new ArrayList<>();
//        DownloadTools tools = DownloadTools.getInstance(5);
        for (Element image : images) {
            DownloadDTO dto = new DownloadDTO();
            Element element = image.nextElementSibling();
            String url = "https:" + image.attr("data-src");
            dto.setUrl(url);
            dto.setSavePath(CommonIOUtils.makeFilePath(path, element.text() + ".jpg"));
            list.add(dto);
//            tools.ThreadExecutorAdd(new OneFileOneThread(dto));
        }
        return list;
//        tools.shutdown();
    }

    public void analysisArticle(String uid) {
        String pattern = "https://api.bilibili.com/x/space/wbi/article?mid=%1$s&pn=%2$s&ps=12&sort=publish_time&platform=web";
        int currentPage = 1;
        DownloadTools tools = DownloadTools.getInstance(10);
        while (true) {
            JsonElement json = CommonIOUtils.paraseJsonFromURL(String.format(pattern, uid, currentPage), null);
            JsonElement articleList = CommonIOUtils.getFromJson2(json, "data-articles");
            if (articleList.isJsonNull()) {
                break;
            }
            currentPage++;
            for (JsonElement jsonElement : articleList.getAsJsonArray()) {
                List<DownloadDTO> tmp = download(CommonIOUtils.getFromJson2Str(jsonElement, "id"));
                tmp.forEach(dto -> tools.ThreadExecutorAdd(new OneFileOneThread(dto)));
            }
        }
        tools.shutdown();

    }

    public void test() {
//        https://api.bilibili.com/x/space/wbi/article?mid=6751172&pn=1&ps=12&sort=publish_time&platform=web
        analysisArticle("6751172");
    }
}
