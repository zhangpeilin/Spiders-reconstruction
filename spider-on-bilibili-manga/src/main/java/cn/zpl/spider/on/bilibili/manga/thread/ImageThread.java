package cn.zpl.spider.on.bilibili.manga.thread;

import cn.zpl.pojo.DownloadDTO;
import cn.zpl.spider.on.bilibili.manga.util.BilibiliStaticParams;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.URLConnectionTool;
import com.google.gson.JsonElement;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Slf4j
public class ImageThread implements Callable<DownloadDTO> {

    private JsonElement clip;
    private JsonElement pic;
    private JsonElement chapter;

    ImageThread(JsonElement chapter, JsonElement clip, JsonElement pic) {
        this.clip = clip;
        this.pic = pic;
        this.chapter = chapter;
    }

    @Override
    public DownloadDTO call() {
        try {
            return domain();
        } catch (Exception e) {
            log.error("下载出错：\n", e);
            return domain();
        }
    }

    public DownloadDTO domain() {
        //{"urls":"[\"/bfs/manga/a7d742cac9b73dcdd5d9e8bf4e8657f589607b56.jpg\"]"}
        //{"urls":"[\"/bfs/manga/8dd9d08a2fdf684d0fda366c072ee93c98917d17.jpg@748w.jpg\"]"}
        List<String> pathMake = new ArrayList<>();
        //漫画保存位置
        pathMake.add(BilibiliStaticParams.manga_save_path);
        String imgUrl = pic.getAsString();
        String fileType = imgUrl.substring(imgUrl.lastIndexOf("."));
        String width = CommonIOUtils.getFromJson2Str(clip, "r");
        String order = CommonIOUtils.getFromJson2Str(clip, "pic");
        String response = URLConnectionTool.postUrl(BilibiliStaticParams.ImageTokenUrl,
                "{\"urls\":\"[\\\"" + imgUrl +
                        "@" + width + "w" + fileType + "\\\"]\"}", BilibiliStaticParams.commonHeaders);
        JsonElement token = CommonIOUtils.paraseJsonFromStr(response);
        JsonElement[] urlStr = CommonIOUtils.getFromJson3(token, "data-url");
        JsonElement[] tokenStr = CommonIOUtils.getFromJson3(token, "data-token");
        String imgUrlWithToken = urlStr.length == 1 ? tokenStr.length == 1 ?
                urlStr[0].getAsString() + "?token=" + tokenStr[0].getAsString() : null : null;
        assert imgUrlWithToken != null;
        String chapter_order = CommonIOUtils.getFromJson2Str(chapter, "ord");
        String title = CommonIOUtils.getFromJson2Str(chapter, "title");
        int comic_id = CommonIOUtils.getIntegerFromJson(chapter, "comic_id");
        String comic_name = CommonIOUtils.getFromJson2Str(chapter, "comic_name");
        //拼装保存路径
        //漫画名
        pathMake.add(CommonIOUtils.generateComicFolderName(comic_name, comic_id));
        //章节名
        pathMake.add("".equalsIgnoreCase(title) ? chapter_order : CommonIOUtils.generateChapterName(title, chapter_order));
        DownloadDTO dto = new DownloadDTO();
        dto.setUrl(imgUrlWithToken);
        dto.setHeader(BilibiliStaticParams.commonHeaders);
        dto.setSavePath(CommonIOUtils.makeFilePath(pathMake, order + fileType));
        return dto;
    }


}
