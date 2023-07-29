package cn.zpl;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import cn.zpl.util.FingerPrint;
import org.junit.Test;

public class TestFingerPrint {

    @Test
    public void testCompare() throws IOException{
        FingerPrint fp1 = new FingerPrint(ImageIO.read(new File("D:\\ehentai\\IMG_20230715_140654.jpg")));
        FingerPrint fp2 =new FingerPrint(ImageIO.read(new File("D:\\ehentai\\IMG_20230715_140702.jpg")));
        System.out.println(fp1.toString(true));
        System.out.printf("sim=%f",fp1.compare(fp2));
    }
}