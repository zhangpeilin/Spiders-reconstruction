package cn.zpl.thread;

import cn.zpl.util.BaiduAITool;

import javax.annotation.Resource;
import java.io.File;

public class PAThread  extends CommonThread {

    File file;
    @Resource
    BaiduAITool tool;

    public PAThread(File file) {
        this.file = file;
        getDoRetry().setRetryMaxCount(2);
    }
    @Override
    public void domain() {
        tool.doBusiness(file);
    }
}
