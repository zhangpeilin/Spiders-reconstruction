package cn.zpl.spider.on.bilibili.manga;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestGPT {
    public static void main(String[] args) {
        String pattern = "^\\[\\s*(\\w+)\\s*=\\s*'(.+?)'\\s+and\\s+(\\w+)\\s*=\\s*'(.+?)'\\s*\\]$";
        String input = "[ comic_id = '29318' and title = '爆宠小萌妃' and test = '啊色的发斯蒂芬']";

        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(input);

        if (m.matches()) {
            String key1 = m.group(1);
            String value1 = m.group(2);
            String key2 = m.group(3);
            String value2 = m.group(4);
            String key3 = m.group(5);
            String value3 = m.group(6);

            System.out.println(key1 + "=" + value1); // comic_id=29318
            System.out.println(key2 + "=" + value2); // title=爆宠小萌妃
            System.out.println(key3 + "=" + value3);
        }
    }
}
