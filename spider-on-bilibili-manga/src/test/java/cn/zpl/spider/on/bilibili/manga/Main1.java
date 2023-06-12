package cn.zpl.spider.on.bilibili.manga;

import cn.zpl.common.bean.BilibiliManga;
import cn.zpl.common.bean.RestResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.*;

public class Main1 {
    public static void main(String[] args) {
//        String input = "[comic_id='29318' and title = '爆宠小萌妃' and author='大腿' and target = '小腿' and hello = 'world' and test = tsssss]";
        String input = "comic_id=29318 and title = 爆宠小萌妃 and author='大腿' and test = tsssss";
        Pattern pattern = Pattern.compile("(\\w+)\\s*=\\s*['\"]*([^'\"\\](?:and)*]+)['\"]*");
//        Pattern pattern = Pattern.compile("(\\w+)\\s*=\\s*['\"]?([^'\"\\s]+)['\"]?");
        Matcher matcher = pattern.matcher(input);

        int numMatches = 0;
        Map<String, String> map = new HashMap<>();

        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);
            map.put(key, value);
            numMatches++;
        }

        System.out.println("Number of matches: " + numMatches);
        System.out.println("Map: " + map);
    }
}
class TestQuery{
    public static void main(String[] args) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<RestResponse> forEntity = restTemplate.getForEntity(String.format("http://localhost:2233/common/dao/api/query/%1$s?fetchProperties=[%2$s]&condition=[%3$s]&page=%4$s", BilibiliManga.class.getSimpleName(), "*", "comic_id=29318 and title = 爆宠小萌妃", ""), RestResponse.class);
        System.out.println(forEntity.getBody());
    }
}