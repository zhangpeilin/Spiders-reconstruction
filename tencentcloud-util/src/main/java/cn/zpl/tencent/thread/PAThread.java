package cn.zpl.tencent.thread;

import cn.zpl.tencent.TencentAITool;
import cn.zpl.thread.CommonThread;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class PAThread extends CommonThread {

    File file;

    public PAThread(File file) {
        this.file = file;
        getDoRetry().setRetryMaxCount(2);
    }
    @Override
    public void domain() {

        try {
            TimeUnit.SECONDS.sleep(1);
            TencentAITool.dobusiness(file);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
