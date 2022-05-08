package cn.zpl.commondaocenter.bean;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author zpl
 * @since 2022-04-02
 */
@TableName("video_info")
@ApiModel(value = "VideoInfo对象", description = "")
public class VideoInfo extends cn.zpl.common.bean.VideoInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private String videoId;
}
