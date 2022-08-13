package cn.zpl.common.bean;

import io.swagger.annotations.ApiModel;

import java.io.Serializable;
import java.sql.Blob;

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
