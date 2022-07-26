package cn.zpl.thread;

import cn.zpl.util.BaiduAITool;

import java.io.File;

public class PAThread  extends CommonThread {

    File file;

    public PAThread(File file) {
        this.file = file;
        getDoRetry().setRetryMaxCount(2);
    }
    @Override
    public void domain() {
        BaiduAITool.dobusiness(file);
    }
}
