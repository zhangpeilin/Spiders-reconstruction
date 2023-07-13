package cn.zpl.spider.on.bika;

import java.io.IOException;
import java.nio.file.Paths;

public class TestZip {

    public static void main(String[] args) throws IOException {
//        ZipUtils.unzipFile2Dir("E:\\bika\\(5c06b2b874482467996aa169)P站每周日排鉴赏【周更】.zip", "D:\\bika_temp", "list.txt");
        String str = "D:\\bika_temp\\(5c06b2b874482467996aa169)P站每周日排鉴赏【周更】\\1\\1.txt";
        System.out.println(Paths.get("D:\\bika_temp").relativize(Paths.get(str)).getRoot());

    }
}
