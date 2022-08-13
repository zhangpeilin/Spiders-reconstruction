package cn.zpl.tencent;

import cn.zpl.common.bean.PictureAnalyze;
import cn.zpl.common.bean.RestResponse;
import cn.zpl.tencent.thread.PQThread;
import cn.zpl.util.CrudTools;
import cn.zpl.util.DownloadTools;

import java.util.List;

public class PQ {

    public static void main(String[] args) {
        List<PictureAnalyze> list  = CrudTools.getInstance(null).commonApiQueryBySql("quality_result IS NULL", PictureAnalyze.class);
        DownloadTools downloadTools = DownloadTools.getInstance(10);
        list.forEach(pictureAnalyze -> downloadTools.ThreadExecutorAdd(new PQThread(pictureAnalyze)));
        downloadTools.shutdown();
    }
}
