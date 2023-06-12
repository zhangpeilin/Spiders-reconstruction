package cn.zpl.spider.on.bilibili.manga;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
  public static void main(String[] args) {
    String text = "[comic_id='29318' and title = '爆宠小萌妃' and author='大腿' and target = '小腿' and hello = 'world' and test = tsssss]";

    List<String[]> matches = new ArrayList<>();
    Pattern pattern = Pattern.compile("(\\w+)\\s*=\\s*['\"]([^'\"]*?)['\"]");
    Matcher matcher = pattern.matcher(text);
    while (matcher.find()) {
      String[] kv = new String[] { matcher.group(1), matcher.group(2) };
      matches.add(kv);
    }

    System.out.println(matches);
  }
}