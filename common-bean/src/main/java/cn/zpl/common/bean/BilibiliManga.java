package cn.zpl.common.bean;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author zpl
 * @since 2022-09-16
 */
@ApiModel(value = "BilibiliManga对象", description = "")
public class BilibiliManga implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId
    private String comicId;

    private String title;

    private String savePath;

    private String chapterWaitBuy;

    private String waitFreeAt;

    private Integer allowWaitFree;

    public String getComicId() {
        return comicId;
    }

    public void setComicId(String comicId) {
        this.comicId = comicId;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }
    public String getChapterWaitBuy() {
        return chapterWaitBuy;
    }

    public void setChapterWaitBuy(String chapterWaitBuy) {
        this.chapterWaitBuy = chapterWaitBuy;
    }
    public String getWaitFreeAt() {
        return waitFreeAt;
    }

    public void setWaitFreeAt(String waitFreeAt) {
        this.waitFreeAt = waitFreeAt;
    }
    public Integer getAllowWaitFree() {
        return allowWaitFree;
    }

    public void setAllowWaitFree(Integer allowWaitFree) {
        this.allowWaitFree = allowWaitFree;
    }

    @Override
    public String toString() {
        return "BilibiliManga{" +
            "comicId=" + comicId +
            ", title=" + title +
            ", savePath=" + savePath +
            ", chapterWaitBuy=" + chapterWaitBuy +
            ", waitFreeAt=" + waitFreeAt +
            ", allowWaitFree=" + allowWaitFree +
        "}";
    }
}
