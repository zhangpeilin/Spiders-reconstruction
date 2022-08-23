package cn.zpl.tencent;

import cn.zpl.tencent.common.TencentParams;
import cn.zpl.tencent.config.TencentConfig;
import cn.zpl.util.CommonIOUtils;
import com.google.gson.JsonElement;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

@Component
public class TestCorpWx {

    @Resource
    TencentParams tencentParams;

    public void doBusiness() {

        WeChatMsgSend swx = new WeChatMsgSend();
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
            headers.set("Referer", "http://www.szse.cn/option/quotation/contract/contractchange/index.html");

            Calendar instance = Calendar.getInstance();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String format = dateFormat.format(instance.getTime());
            int hour = instance.get(Calendar.HOUR_OF_DAY);
            String postdata = null;
            hour = instance.get(Calendar.HOUR_OF_DAY);
            ResponseEntity<String> exchange = restTemplate.exchange("http://www.szse.cn/api/report/ShowReport/data?SHOWTYPE=JSON&CATALOGID=option_hybg&loading=first", HttpMethod.GET, new HttpEntity<String>(headers), String.class);
            JsonElement jsonElement = CommonIOUtils.paraseJsonFromStr(exchange.getBody());
            if (jsonElement.isJsonArray()) {
                JsonElement element = jsonElement.getAsJsonArray().get(0);
                JsonElement data = CommonIOUtils.getFromJson2(element, "data");
                JsonElement conditions = CommonIOUtils.getFromJson2(element, "metadata-conditions");
                if (conditions.isJsonArray()) {
                    String defaultValue = conditions.getAsJsonArray().get(0).getAsJsonObject().get("defaultValue").getAsString();
                    if (defaultValue.equals(format)) {
                        postdata = swx.createpostdata(tencentParams.getToUser(), "text", tencentParams.getApplicationId(), "content", "网站更新了");
                    }
//                        if (data.isJsonArray() && data.getAsJsonArray().size() == 0 && ) {
//                            postdata = swx.createpostdata("@all", "text", tencentParams.getApplicationId(), "content","网站更新了");
//                        } else {
//                            postdata = swx.createpostdata("@all", "text", tencentParams.getApplicationId(), "content","这是一条测试信息");
//                        }
                }
            }
            if (postdata == null) {
                return;
            }
            String token = swx.getToken(tencentParams.getCorpId(), tencentParams.getCorpSecret());
            swx.post("utf-8", WeChatMsgSend.CONTENT_TYPE, (new WeChatUrlData()).getSendMessage_Url(), postdata, token);
            TimeUnit.SECONDS.sleep(3);
            postdata = swx.createpostdata(tencentParams.getToUser(), "text", tencentParams.getApplicationId(), "content", "记得吃药");
            String resp = swx.post("utf-8", WeChatMsgSend.CONTENT_TYPE, (new WeChatUrlData()).getSendMessage_Url(), postdata, token);
            String errcode = CommonIOUtils.getFromJson2Str(CommonIOUtils.paraseJsonFromStr(resp), "errcode");
            if ("0".equals(errcode)) {
                TencentConfig.cache.put("isNotice", true);
            }
            System.out.println("获取到的token======>" + token);
            System.out.println("请求数据======>" + postdata);
            System.out.println("发送微信的响应数据======>" + resp);
        } catch (Exception e) {
            e.getStackTrace();
        }
    }
}