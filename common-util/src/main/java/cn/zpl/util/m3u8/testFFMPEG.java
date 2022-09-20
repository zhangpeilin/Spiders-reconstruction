package cn.zpl.util.m3u8;

import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;

import java.io.File;

public class testFFMPEG {
    public static void main(String[] args) throws EncoderException {
        Encoder encoder = new Encoder();
        long duration = encoder.getInfo(new File("E:\\视频爬虫\\BV17Y4y1P71s(av643963823).mp4")).getDuration();
        System.out.println(duration);
    }
}
