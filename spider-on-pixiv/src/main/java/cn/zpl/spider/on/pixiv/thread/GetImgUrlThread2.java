package cn.zpl.spider.on.pixiv.thread;

import cn.zpl.common.bean.PixivPictures;
import cn.zpl.config.SpringContext;
import cn.zpl.pojo.DownloadDTO;
import cn.zpl.spider.on.pixiv.util.CommonUtils;
import org.jetbrains.annotations.Contract;

import java.util.Vector;

public class GetImgUrlThread2 implements Runnable {

    private final PixivPictures picture;
    private final Vector<DownloadDTO> list;
    CommonUtils commonUtils;

    @Contract(pure = true)
    public GetImgUrlThread2(PixivPictures picture, Vector<DownloadDTO> list) {
        this.picture = picture;
        this.list = list;
        commonUtils = SpringContext.getBeanWithGenerics(CommonUtils.class);
    }

    public void run() {
        try {
//            if (CommonMethods.isExists(picture)) {
//                PixivParams.logger.debug(picture.getId() + "已存在，路径：" + CommonMethods.exists.get(picture.getId()).getSavePath());
//                return;
//            }
            commonUtils.getImgUrl(picture, list);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("重新解析图片，illust_id = ：" + picture.getId());
            commonUtils.getImgUrl(picture, list);
        }
    }
}
