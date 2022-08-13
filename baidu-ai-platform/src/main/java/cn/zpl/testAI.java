package cn.zpl;

import cn.zpl.thread.PAThread;
import cn.zpl.util.DownloadTools;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Objects;

public class testAI {

    public static void main(String[] args) {
        File base = new File("C:\\云分析\\第二批");
        File[] files = base.listFiles((FilenameFilter) TrueFileFilter.TRUE);
        DownloadTools downloadTools = DownloadTools.getInstance(1);
        for (File file : Objects.requireNonNull(files)) {
            downloadTools.ThreadExecutorAdd(new PAThread(file));
        }
        downloadTools.shutdown();
    }
}
