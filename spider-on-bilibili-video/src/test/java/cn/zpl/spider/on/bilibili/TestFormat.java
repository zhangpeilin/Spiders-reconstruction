package cn.zpl.spider.on.bilibili;

import cn.zpl.pojo.Data;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.DateHandler;
import cn.zpl.util.URLConnectionTool;
import cn.zpl.util.UrlContainer;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonElement;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Commit;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 *
 * @author zhangpl1
 * @date 2022/8/3
 */
public class TestFormat {
    public static String appId = "wx4ceee2fdc69e0b88";
    public static String secret = "e8e9672fbae177a720db07bc518ed4eb";
    private static final LoadingCache<String, String> cache = CacheBuilder.newBuilder().maximumSize(1).expireAfterWrite(7200,TimeUnit.SECONDS).build(new CacheLoader<String, String>() {
        public @NotNull String load(@NotNull String key){
            return CommonIOUtils.getFromJson2Str(CommonIOUtils.paraseJsonFromStr(getAccessToken()), "access_token");
        }
    });
    @SneakyThrows
    public static void main(String[] args) {
        //获取access_token
        String nothingNew = "gzBoW1dP-uFQkE8qoLkRoh0g5j8PFS0V4S1M9N-mQ-o";
        String newThing = "i6b0nTBk6Y7pOEo8_EuAkCIWPWC1FyVl1D3tvuAl31Y";
        String access_token = cache.get("access_token");
        System.out.printf("中间是替换的字符串[%s]", "a");
        Map<String, Object> map = new HashMap<>();
//        map.put("action", "inc");
        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity("https://springboot-v783-3966-4-1313436517.sh.run.tcloudbase.com/api/count", map, String.class);
//        System.out.println(stringResponseEntity.getBody());
//        {
//            "touser":"OPENID",
//                "msgtype":"text",
//                "text":
//            {
//                "content":"Hello World"
//            }
//        }
//        String token = "60_7PjjGu4Y7ZzDG23P4eoKX3PanXfjKa9f9wvLKU-imMojppRwoozb2WFeERctOj8-6ZmFOtdXzJNUiL7vpajSh4Ke3Fs3lvgrL2GdKNwrmrgjUQNS1UYpDIikZQ4DO_dlwMQ76DbhmWH-zYktVQXaAHADYF";
//        String test_token = "60_rT-6PihkxALdLAN7vvi5rsB7ByIJHUsfKjzGrixrPA4Pnailznq8-duK9UfQL0qiVdNImyuu57LVXDvwOfjaG_mvrJH_skz-zrXlYUgnhtwmFeKE8K0r1l0h8P4QU913ozjpcVIrmJ6lYMNkDPNgAJAWIG";
//        ResponseEntity<JSONObject> forEntity1 = restTemplate.getForEntity("GET https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=wxc24b6509e0229f53&secret=76f1ce5b285a5d42aab0683c0ba8e08b", JSONObject.class);

//        ResponseEntity<String> forEntity = restTemplate.getForEntity("https://api.weixin.qq.com/cgi-bin/user/get?access_token=" +
//                token + "&next_openid=", String.class);
//        System.out.println(forEntity.getBody());

        map.clear();
        map.put("touser", "ow-Q16HyhQF2XZKnDOiq1oK-oIv4");
        map.put("msgtype", "text");
        map.put("text", Collections.singletonMap("content", "hello world"));
//        restTemplate.postForEntity("https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + test_token, map, String.class);

        map.clear();
        map.put("touser", "ow-Q16HyhQF2XZKnDOiq1oK-oIv4");
        map.put("topcolor", "#FF0000");
//        HttpsURLConnection conn = URLConnectionTool.getHttpsURLConnection(new UrlContainer("http://www.szse.cn/api/report/ShowReport/data?SHOWTYPE=JSON&CATALOGID=option_hybg&loading=first"));
//        InputStream inputStream = conn.getInputStream();
//        if (conn.getResponseCode() == 200) {
//
//        }
//        if ("".isEmpty()) {
//            map.put("template_id", "gzBoW1dP-uFQkE8qoLkRoh0g5j8PFS0V4S1M9N-mQ-o");
//        } else {
//            map.put("template_id", "i6b0nTBk6Y7pOEo8_EuAkCIWPWC1FyVl1D3tvuAl31Y");
//        }
//        map.put("data", Collections.singletonMap("value", "撒发送到发"));

        //http://www.szse.cn/option/quotation/contract/contractchange/index.html
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
        headers.set("Referer", "http://www.szse.cn/option/quotation/contract/contractchange/index.html");
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);
//        ResponseEntity<String> exchange = restTemplate.exchange("http://www.szse.cn/api/report/ShowReport/data?SHOWTYPE=JSON&CATALOGID=option_hybg&loading=first", HttpMethod.GET, entity, String.class);
        Calendar instance = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String format = dateFormat.format(instance.getTime());
        int hour = instance.get(Calendar.HOUR_OF_DAY);
        while (hour == 0) {
            hour = instance.get(Calendar.HOUR_OF_DAY);
            ResponseEntity<String> exchange = restTemplate.exchange("http://www.szse.cn/api/report/exchange/onepersistenthour/monthList?v=" + System.currentTimeMillis(), HttpMethod.GET, new HttpEntity<String>(headers), String.class);
            JsonElement nowdate = CommonIOUtils.getFromJson2(exchange.getBody(), "nowdate");
            if (nowdate.getAsString().equals(format)) {
                //获取到的日期跟本地日期一致
                map.put("template_id", nothingNew);
                break;
            }
            TimeUnit.MINUTES.sleep(10);
        }
        //早上八点开始执行
        //发送微信消息
        ResponseEntity<String> forEntity = restTemplate.postForEntity("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + access_token, map, String.class);
        System.out.println(forEntity.getBody());




//        if (exchange.getBody() != null) {
//            map.put("touser", "ow-Q16HyhQF2XZKnDOiq1oK-oIv4");
//            map.put("msgtype", "text");
//            map.put("text", Collections.singletonMap("content", "hello world"));
//            restTemplate.postForEntity("https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + test_token, map, String.class);
//        }
        ResponseEntity<String> exchange = restTemplate.exchange("http://www.szse.cn/api/report/ShowReport/data?SHOWTYPE=JSON&CATALOGID=option_hybg&loading=first", HttpMethod.GET, entity, String.class);
        JsonElement jsonElement = CommonIOUtils.paraseJsonFromStr(exchange.getBody());
        if (jsonElement.isJsonArray()) {
            for (JsonElement element : jsonElement.getAsJsonArray()) {
                JsonElement data = CommonIOUtils.getFromJson2(element, "data");
                if (data.isJsonArray() && data.getAsJsonArray().size() == 0) {
                    map.put("template_id", "gzBoW1dP-uFQkE8qoLkRoh0g5j8PFS0V4S1M9N-mQ-o");
                } else {
                    map.put("template_id", "i6b0nTBk6Y7pOEo8_EuAkCIWPWC1FyVl1D3tvuAl31Y");
                }
            }
        }
//        ResponseEntity<String> forEntity = restTemplate.postForEntity("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + test_token, map, String.class);
//        System.out.println(forEntity.getBody());
    }

    public static String getAccessToken() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> forEntity = restTemplate.getForEntity(String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%1s&secret=%2s", appId, secret), String.class);
        return forEntity.getBody();
    }
}
