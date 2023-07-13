package cn.zpl.spider.on.bika.config;

import cn.zpl.common.bean.BikaList;
import cn.zpl.common.bean.RestResponse;
import cn.zpl.spider.on.bika.thread.BikaComicThread;
import cn.zpl.util.DownloadTools;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RabbitQueueListener {

    @RabbitListener(queues = "bika")
    public void process(String msg) {
        String[] ids = msg.split("\n");
        {
            DownloadTools tool = DownloadTools.getInstance(5);
            tool.setName("漫画");
            tool.setSleepTimes(10000);
            for (String id : ids) {
                tool.ThreadExecutorAdd(new BikaComicThread(id.replaceAll("\\s", ""), true));
            }
            tool.shutdown();
        }
    }
}
