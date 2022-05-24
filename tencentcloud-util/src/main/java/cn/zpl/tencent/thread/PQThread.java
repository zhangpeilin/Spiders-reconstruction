package cn.zpl.tencent.thread;

import cn.zpl.common.bean.PictureAnalyze;
import cn.zpl.tencent.TencentAITool;
import cn.zpl.thread.CommonThread;

/**
 * 图像质量分析线程
 */
public class PQThread extends CommonThread {

    PictureAnalyze pictureAnalyze;

    public PQThread(PictureAnalyze pictureAnalyze) {
        this.pictureAnalyze = pictureAnalyze;
        getDoRetry().setRetryMaxCount(2);
    }

    @Override
    public void domain() {
        TencentAITool.pictureQuality(pictureAnalyze);
    }
}
