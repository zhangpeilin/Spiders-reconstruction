package cn.zpl.dao.bean;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author zpl
 * @since 2022-05-02
 */
@TableName("exception_list")
@ApiModel(value = "ExceptionList对象", description = "")
public class ExceptionList extends cn.zpl.common.bean.ExceptionList implements Serializable {

    private static final long serialVersionUID = 1L;
}
