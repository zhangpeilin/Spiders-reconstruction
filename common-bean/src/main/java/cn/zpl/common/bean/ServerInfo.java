package cn.zpl.common.bean;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;

import java.io.Serializable;

/**
 * <p>
 * 记录家用服务器ip
 * </p>
 *
 * @author zpl
 * @since 2023-07-20
 */
@TableName("server_info")
@ApiModel(value = "ServerInfo对象", description = "记录家用服务器ip")
public class ServerInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String ip;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return "ServerInfo{" +
            "id=" + id +
            ", ip=" + ip +
        "}";
    }
}
