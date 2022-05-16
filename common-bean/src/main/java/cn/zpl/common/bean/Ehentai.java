package cn.zpl.common.bean;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author zpl
 * @since 2022-04-20
 */
@Data
@ApiModel(value = "Ehentai对象", description = "")
public class Ehentai implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String title;

    private String url;

    private String cost;

    private String favcount;

    private String artist;

    private String male;

    private String female;

    private String mixed;

    private String other;

    private String parody;

//    @TableField("`group`")
    private String group;

    private String create_time;
}
