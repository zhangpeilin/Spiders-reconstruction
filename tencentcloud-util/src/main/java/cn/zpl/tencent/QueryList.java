package cn.zpl.tencent;

import cn.zpl.common.bean.PictureAnalyze;
import cn.zpl.common.bean.RestResponse;
import cn.zpl.util.CrudTools;

import java.util.List;

public class QueryList {

    public static void main(String[] args) {
        List<PictureAnalyze> list = CrudTools.getInstance(null).commonApiQueryBySql("截图", PictureAnalyze.class);
        System.out.println(list.size());
    }
}
