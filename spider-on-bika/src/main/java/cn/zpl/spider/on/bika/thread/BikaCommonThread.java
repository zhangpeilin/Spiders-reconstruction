package cn.zpl.spider.on.bika.thread;

import cn.zpl.config.SpringContext;
import cn.zpl.spider.on.bika.utils.BikaUtils;
import cn.zpl.thread.CommonThread;

public abstract class BikaCommonThread extends CommonThread {

    BikaUtils bikaUtils;
    @Override
    public void init() {
        bikaUtils = (BikaUtils) SpringContext.getBeanWithGenerics(BikaUtils.class);
    }
}
