package cn.zpl.dao.bean;

import com.baomidou.mybatisplus.annotation.TableField;
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
public class Ehentai extends cn.zpl.common.bean.Ehentai implements Serializable {

    @TableField("`group`")
    private String group;
}
