package cn.zpl.tencent.bs;

import cn.zpl.pojo.Data;
import cn.zpl.tencent.WeChatMsgSend;
import cn.zpl.tencent.WeChatUrlData;
import cn.zpl.tencent.common.TencentParams;
import cn.zpl.tencent.config.TencentConfig;
import cn.zpl.util.CommonIOUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

@Component
public class GenshinDailyTask {


    public static LoadingCache<String, String> tokenCache;
    @Resource
    TencentParams tencentParams;

    public void doBusiness() {


        try {
            Data data = new Data();
            data.setUrl("https://hk4e-api.mihoyo.com/event/ys_ledger/monthDetail?page=1&month=12&limit=20&type=1&bind_uid=100250088&bind_region=cn_gf01&bbs_presentation_style=fullscreen&bbs_auth_required=true&utm_source=bbs&utm_medium=mys&utm_campaign=GameRecord");
            data.setCookie(" account_id=158753820; cookie_token=3vW3zoBpQ7o8e4n6CKaU2fDRhXygxn7t4QPDm6DZ; aliyungf_tc=287a1bc3affdd6d9cedfb4371191771132a33661529ea84ac7d44f16dde6fecf; _ga=GA1.1.597651362.1681639200; _ga_TNKZGZ607P=GS1.1.1702383283.1.0.1702383283.0.0.0; _gat_gtag_UA_168360861_2=1; _gid=GA1.2.1146173671.1702383267; ltoken=iKoNZ0eWwkWx3POJtDDE3hxSKiyhHI6elDHUH7FG; ltuid=158753820; _MHYUUID=a6b14d31-101c-4d53-b6cf-e015a501a2b0; login_ticket=LLj5il7RNwS6t6xYNZaoLVkl5QFofWwqUuDfhYto; mi18nLang=zh-cn; _gat=1");
//            RestTemplate restTemplate = new RestTemplate();
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
//            headers.set("Referer", "http://www.szse.cn/option/quotation/contract/contractchange/index.html");

//            Calendar instance = Calendar.getInstance();
//            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//            String format = dateFormat.format(instance.getTime());
//            int hour = instance.get(Calendar.HOUR_OF_DAY);
            String postdata = null;
//            hour = instance.get(Calendar.HOUR_OF_DAY);

            if (tencentParams.watchWebsite) {
//                ResponseEntity<String> exchange = restTemplate.exchange("", HttpMethod.GET, new HttpEntity<String>(headers), String.class);
                CommonIOUtils.withTimer(data);
                JsonElement taskResult = CommonIOUtils.paraseJsonFromStr(data.getResult());
                int retCode = CommonIOUtils.getFromJson2Integer(taskResult, "retcode");
                if (retCode == 0) {
                    JsonElement list = CommonIOUtils.getFromJson2(taskResult, "data-list");
                    int total = 0;
                    if (list.isJsonArray() && list.getAsJsonArray().size() > 0) {
                        for (JsonElement item : list.getAsJsonArray()) {
                            if (total == 60) {
                                WeChatMsgSend swx = new WeChatMsgSend();
                                tokenCache = CacheBuilder.newBuilder().maximumSize(1).expireAfterWrite(110, TimeUnit.MINUTES).build(new CacheLoader<String, String>() {
                                    @Override
                                    public @NotNull String load(@NotNull String key) {
                                        try {
                                            return swx.getToken(tencentParams.getCorpId(), tencentParams.getCorpSecret());
                                        } catch (IOException e) {
                                            return "";
                                        }
                                    }
                                });
                                String token = tokenCache.get("token");
                                //如果是当天并且每日委托奖励领取了累积60，则发送微信并且退出循环
                                postdata = swx.createpostdata(tencentParams.getToUser(), "text", tencentParams.getApplicationId(), "content", "当日委托已全部领取");
                                String resp = swx.post("utf-8", WeChatMsgSend.CONTENT_TYPE, (new WeChatUrlData()).getSendMessage_Url(), postdata, token);
                                String errCode = CommonIOUtils.getFromJson2Str(CommonIOUtils.paraseJsonFromStr(resp), "errcode");
                                if ("0".equals(errCode)) {
                                    TencentConfig.cache.put("isNotice", true);
                                    System.out.println("获取到的token======>" + token);
                                    System.out.println("请求数据======>" + postdata);
                                    System.out.println("发送微信的响应数据======>" + resp);
                                    return;
                                }
                            }
                            int num = CommonIOUtils.getFromJson2Integer(item, "num");
                            String time = CommonIOUtils.getFromJson2Str(item, "time");
                            String action = CommonIOUtils.getFromJson2Str(item, "action");
//                            LocalDateTime dateTime = LocalDateTime.of(2023, 12, 15, 18, 37, 51);
                            LocalDateTime dateTime = LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            // 获取当天日期
                            LocalDate today = LocalDate.now();
                            // 判断指定日期时间是否是当天
                            if (dateTime.toLocalDate().equals(today) && "每日委托奖励".equalsIgnoreCase(action)) {
                                total += num;
                            }
                        }
                    }
                }
//                if (jsonElement.isJsonArray()) {
//                    JsonElement element = jsonElement.getAsJsonArray().get(0);
////                JsonElement data = CommonIOUtils.getFromJson2(element, "data");
//                    JsonElement taskResult = CommonIOUtils.getFromJson2(element, "metadata-conditions");
////                    if (conditions.isJsonArray()) {
////                        String defaultValue = conditions.getAsJsonArray().get(0).getAsJsonObject().get("defaultValue").getAsString();
////                        if (defaultValue.equals(format)) {
////                            postdata = swx.createpostdata(tencentParams.getToUser(), "text", tencentParams.getApplicationId(), "content", "网站更新了");
////                        }
//////                        if (data.isJsonArray() && data.getAsJsonArray().size() == 0 && ) {
//////                            postdata = swx.createpostdata("@all", "text", tencentParams.getApplicationId(), "content","网站更新了");
//////                        } else {
//////                            postdata = swx.createpostdata("@all", "text", tencentParams.getApplicationId(), "content","这是一条测试信息");
//////                        }
////                    }
//                }

            }
//            postdata = swx.createpostdata(tencentParams.getToUser(), "text", tencentParams.getApplicationId(), "content", "记得吃药");
//            String resp = swx.post("utf-8", WeChatMsgSend.CONTENT_TYPE, (new WeChatUrlData()).getSendMessage_Url(), postdata, token);


        } catch (Exception e) {
            e.getStackTrace();
        }
    }
}