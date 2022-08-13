package cn.zpl.spider.on.ehentai.thread;

import cn.zpl.common.bean.NasPage;
import cn.zpl.common.bean.NasPic;
import cn.zpl.config.UrlConfig;
import cn.zpl.pojo.Data;
import cn.zpl.thread.CommonThread;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.CrudTools;
import cn.zpl.util.SaveLogForImages;
import com.google.gson.JsonElement;
import lombok.SneakyThrows;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SpiderOnNASThreadV2 extends CommonThread {

    private final int offset;
    private static boolean download = false;

    public SpiderOnNASThreadV2(int offset) {
        this.offset = offset;
    }

    @SneakyThrows
    public static void main(String[] args) {
        dobusiness(0);
    }

    public static void dobusiness(int offset) throws IOException {
        String headers = "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36\n" +
                "X-SYNO-SHARING: pKQcitKUk\n" +
                "Cookie: sharing_sid=8HoM3CssjugSIfjO8mFjPfFCQ5wDlLuN; _SSID=wyfTjApLCEBKopZrHjj8SyDvz1yB5tpFHOOZ9zYHjmI; arp_scroll_position=1400.800048828125\n";
        UrlConfig config = new UrlConfig();
        config.setCommonQueryUrl("http://localhost:8080/common/dao/api/query/%1$s?fetchProperties=[%2$s]&condition=[%3$s]&size=%4$s");
        config.setCommonSaveUrl("http://localhost:8080/common/dao/api/save");
        CrudTools<Object> crudTools = CrudTools.getInstance(config);
        List<NasPage> nasPages = crudTools.commonApiQueryBySql("offset=" + offset, NasPage.class);
        String json = new String(nasPages.get(0).getResult());
        JsonElement list = CommonIOUtils.getFromJson2(json, "data-list");
        if (list.isJsonArray()) {
            for (JsonElement jsonElement : list.getAsJsonArray()) {
                String type = CommonIOUtils.getFromJson2Str(jsonElement, "type");
                if (type.equalsIgnoreCase("video")) {
                    NasPic pic = new NasPic();
                    pic.setId(CommonIOUtils.getFromJson2Str(jsonElement, "id"));
                    pic.setUrl(CommonIOUtils.format("http://www.ariess.info:5000/mo/sharing/webapi/entry.cgi?item_id=%5B{}%5D&passphrase=%22pKQcitKUk%22&api=%22SYNO.Foto.Download%22&method=%22download%22&version=1&_sharing_id=%22pKQcitKUk%22", pic.getId()));
                    pic.setType(type);
                    CrudTools.commonApiSave(pic);
                    continue;
                }
                Data d1 = new Data();
                d1.setUrl("http://www.ariess.info:5000/mo/sharing/webapi/entry.cgi/SYNO.Foto.Browse.Item");
                d1.setHeader(headers);
                d1.setParams(String.format("api=SYNO.Foto.Browse.Item&method=get&version=2&id=[%1$s]&additional=[\"description\",\"tag\",\"exif\",\"resolution\",\"orientation\",\"gps\",\"video_meta\",\"video_convert\",\"thumbnail\",\"address\",\"geocoding_id\",\"rating\",\"provider_user_id\"]&passphrase=\"pKQcitKUk\"", CommonIOUtils.getFromJson2Str(jsonElement, "id")));
                String pifInfo = CommonIOUtils.postUrl(d1);
                JsonElement l1 = CommonIOUtils.getFromJson2(pifInfo, "data-list");
                JsonElement thumbnail = CommonIOUtils.getFromJson2(l1.getAsJsonArray().get(0), "additional-thumbnail");
                String filename = CommonIOUtils.getFromJson2Str(l1.getAsJsonArray().get(0), "filename");
                String unit_id = CommonIOUtils.getFromJson2Str(thumbnail, "unit_id");
                String cache_key = CommonIOUtils.getFromJson2Str(thumbnail, "cache_key");
                Data d2 = new Data();
                d2.setHeader(headers);
                //http://www.ariess.info:5000/mo/sharing/webapi/entry.cgi/IMG_0145.JPG?id=76681&cache_key="76681_1660056246"&type="unit"&size="xl"&passphrase="pKQcitKUk"&api="SYNO.Foto.Thumbnail"&method="get"&version=2&_sharing_id="pKQcitKUk"
                NameValuePair pair1 = new BasicNameValuePair("id", unit_id);
                NameValuePair pair2 = new BasicNameValuePair("cache_key", String.format("\"%1$s\"", cache_key));
                NameValuePair pair3 = new BasicNameValuePair("type", String.format("\"%1$s\"", "unit"));
                NameValuePair pair4 = new BasicNameValuePair("size", String.format("\"%1$s\"", "xl"));
                NameValuePair pair5 = new BasicNameValuePair("passphrase", String.format("\"%1$s\"", "pKQcitKUk"));
                NameValuePair pair6 = new BasicNameValuePair("api", String.format("\"%1$s\"", "SYNO.Foto.Thumbnail"));
                NameValuePair pair7 = new BasicNameValuePair("method", String.format("\"%1$s\"", "get"));
                NameValuePair pair8 = new BasicNameValuePair("version", "2");
                NameValuePair pair9 = new BasicNameValuePair("_sharing_id", String.format("\"%1$s\"", "pKQcitKUk"));
                List<NameValuePair> valuePairList = new ArrayList<>();
                valuePairList.add(pair1);
                valuePairList.add(pair2);
                valuePairList.add(pair3);
                valuePairList.add(pair4);
                valuePairList.add(pair5);
                valuePairList.add(pair6);
                valuePairList.add(pair7);
                valuePairList.add(pair8);
                valuePairList.add(pair9);
                String format = URLEncodedUtils.format(valuePairList, StandardCharsets.UTF_8);
                d2.setUrl(String.format("http://www.ariess.info:5000/mo/sharing/webapi/entry.cgi/%1$s?", URLEncoder.encode(filename, "UTF-8")) + format);
                d2.setType("byte");
                NasPic pic = new NasPic();
                pic.setId(unit_id);
                pic.setCacheKey(String.format("\"%1$s\"", cache_key));
                pic.setType(String.format("\"%1$s\"", "unit"));
                pic.setSize(String.format("\"%1$s\"", "xl"));
                pic.setPassphrase(String.format("\"%1$s\"", "pKQcitKUk"));
                pic.setApi(String.format("\"%1$s\"", "SYNO.Foto.Thumbnail"));
                pic.setMethod(String.format("\"%1$s\"", "get"));
                pic.setVersion("2");
                pic.setSharingId(String.format("\"%1$s\"", "pKQcitKUk"));
                pic.setUrl(d2.getUrl());
                CrudTools.commonApiSave(pic);
                if (!download) {
                    //不下载时，保存图片地址信息后结束
                    continue;
                }
                CommonIOUtils.withTimer(d2);
                Object image = d2.getResObject();
                try (FileOutputStream fileOutputStream = new FileOutputStream("D:\\nas\\" + filename)) {
                    fileOutputStream.write((byte[]) image);
                } catch (Exception e) {
                    e.printStackTrace();
                    SaveLogForImages.saveLog(new File("D:\\nas\\" + filename));
                }
            }
        }

    }

    @Override
    public void domain() throws Exception {
        dobusiness(this.offset);
    }
}
