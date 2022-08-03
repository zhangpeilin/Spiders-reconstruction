package cn.zpl.common.bean;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * 
 * </p>
 *
 * @author zpl
 * @since 2022-08-03
 */
@ApiModel(value = "Token对象", description = "")
public class Token implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String token;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "Token{" +
            "id=" + id +
            ", token=" + token +
        "}";
    }
}
