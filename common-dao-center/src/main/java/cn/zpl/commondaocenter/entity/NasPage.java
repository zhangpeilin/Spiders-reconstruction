package cn.zpl.commondaocenter.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import org.apache.ibatis.type.BlobTypeHandler;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author zpl
 * @since 2022-08-13
 */
@ApiModel(value = "NasPage对象", description = "")
public class NasPage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String offset;

    @TableField(typeHandler = BlobTypeHandler.class)
    private byte[] result;

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }
    public byte[] getResult() {
        return result;
    }

    public void setResult(byte[] result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "NasPage{" +
            "offset=" + offset +
            ", result=" + result +
        "}";
    }
}
