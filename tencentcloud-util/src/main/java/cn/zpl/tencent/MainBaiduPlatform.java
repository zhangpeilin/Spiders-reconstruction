package cn.zpl.tencent;

import cn.zpl.tencent.thread.PAThread;
import cn.zpl.util.DownloadTools;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collection;

public class MainBaiduPlatform {

    public static void main(String[] args) {
        File base = new File("");
        Collection<File> files = FileUtils.listFiles(base, null, true);
        DownloadTools tools = DownloadTools.getInstance(3);
        for (File file : files) {
            tools.ThreadExecutorAdd(new PAThread(file));
        }
        tools.shutdown();
    }
}
