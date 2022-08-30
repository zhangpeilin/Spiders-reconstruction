package cn.zpl.util.m3u8;

import cn.zpl.common.bean.VideoInfo;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.DownloadTools;
import cn.zpl.util.FFMEPGToolsPatch;
import cn.zpl.util.m3u8.thread.M3u8FileDownloadThread;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Download {
    public static void main(String[] args) throws IOException, InterruptedException {
        m3u8Core core = new m3u8Core();
        String[] extensions = new String[]{"m3u8"};
        File base = new File("E:\\m3u8\\1\\新建文件夹");
        Collection<File> list = FileUtils.listFiles(base, extensions.length == 0 ? null : extensions, true);
//        core.setHost("https://play.bo588588.com");
        core.setDirectory("E:\\m3u8\\");
//        core.setFileName("ph601ec79ccbbbd" + ".ts");
//        core.downloadCore("https://jdvv009.microclassonline.com/20220726/Ql6PS69p/index.m3u8");
        DownloadTools tools = DownloadTools.getInstance(5);
        for (File file : list) {
            tools.ThreadExecutorAdd(new M3u8FileDownloadThread(file.getPath()));
        }
        tools.shutdown();

    }
}
