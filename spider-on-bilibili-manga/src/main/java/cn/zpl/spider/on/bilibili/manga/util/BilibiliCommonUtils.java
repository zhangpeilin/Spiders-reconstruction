package cn.zpl.spider.on.bilibili.manga.util;

import cn.zpl.common.bean.BilibiliManga;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.CrudTools;
import cn.zpl.util.URLConnectionTool;
import cn.zpl.util.UrlContainer;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


@Component
public class BilibiliCommonUtils {

    @Resource
    BilibiliMangaProperties bilibiliMangaProperties;
    @Resource
    CrudTools tools;

    public static String getUserInfo(String uid) throws JsonIOException, JsonSyntaxException {

        String url = "https://api.bilibili.com/x/space/acc/info?mid=" + uid + "&jsonp=jsonp";
        UrlContainer container = new UrlContainer(url);
        JsonElement json = CommonIOUtils.paraseJsonFromURL(container);
        return CommonIOUtils.getFromJson2Str(json, "data-name");
    }

    public static JsonElement decryptIndexFile(byte[] result, int comic_id, int chapter_id) {

        result = Arrays.copyOfRange(result, 9, result.length);
        byte[] key = new byte[8];
        key[0] = (byte) chapter_id;
        key[1] = (byte) (chapter_id >> 8);
        key[2] = (byte) (chapter_id >> 16);
        key[3] = (byte) (chapter_id >> 24);
        key[4] = (byte) comic_id;
        key[5] = (byte) (comic_id >> 8);
        key[6] = (byte) (comic_id >> 16);
        key[7] = (byte) (comic_id >> 24);
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (result[i] ^ key[i % 8]);
        }
        try {
            ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(result));
            ZipEntry zipEntry;
            String line;
            JsonElement jsonElement = null;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(zipInputStream));
                while ((line = reader.readLine()) != null) {
                    jsonElement = CommonIOUtils.paraseJsonFromStr(line);
                }
            }
            zipInputStream.close();
            return jsonElement;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String postUrl(String path, String params, String headers) {
        if (headers == null)
            headers = bilibiliMangaProperties.commonHeaders;
        return URLConnectionTool.postUrl(path, params, headers);
    }

    public BilibiliManga getComicById(String comicId) {
        List<BilibiliManga> bilibiliMangas = tools.commonApiQuery(String.format(" comic_id = %1$s", comicId), BilibiliManga.class);
        if (bilibiliMangas.isEmpty()) {
            return null;
        } else {
            return bilibiliMangas.get(0);
        }
    }

    public List<BilibiliManga> getWaitCompleteList(int size) {
        return tools.commonApiQueryBySql("select ifnull(TIMESTAMPDIFF(SECOND,wait_free_at,now()),100) as diff,t.* from bilibili_manga t where allow_wait_free = 1 and chapter_wait_buy <> 0 order by diff desc limit " + size, BilibiliManga.class);
    }

    public List<BilibiliManga> getWaitList() {
        return tools.commonApiQueryBySql("select ifnull(TIMESTAMPDIFF(SECOND,wait_free_at,now()),100) as diff,t.* from bilibili_manga t where allow_wait_free = 1 and chapter_wait_buy <> 0 order by diff desc", BilibiliManga.class);
    }
}
