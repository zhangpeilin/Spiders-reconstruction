package cn.zpl.util.m3u8;

import cn.zpl.common.bean.VideoInfo;
import cn.zpl.config.UtilSpringConfig;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.DownloadTools;
import cn.zpl.util.FFMEPGToolsPatch;
import cn.zpl.util.m3u8.thread.M3u8FileDownloadThread;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Download {
    public static void main(String[] args) throws IOException, InterruptedException {
        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(UtilSpringConfig.class);
        M3u8FileDownloadThread thread = annotationConfigApplicationContext.getBean(M3u8FileDownloadThread.class);
        m3u8Core core = new m3u8Core();
        String[] extensions = new String[]{"m3u8"};
        File base = new File("F:\\新建文件夹");
        Collection<File> list = FileUtils.listFiles(base, extensions, true);
//        core.setHost("https://play.bo588588.com");
        core.setDirectory("m:\\m3u8\\");
//        core.setFileName("ph601ec79ccbbbd" + ".ts");
//        core.downloadCore("https://jdvv009.microclassonline.com/20220726/Ql6PS69p/index.m3u8");
        for (File file : list) {
            thread.downloadByM3U8(file.toString());
        }

    }
}

