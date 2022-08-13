package cn.zpl.common.bean;

import io.swagger.annotations.ApiModel;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author zpl
 * @since 2022-08-13
 */
@ApiModel(value = "NasPic对象", description = "")
public class NasPic implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String unitId;

    private String cacheKey;

    private String type;

    private String url;

    private String size;

    private String passphrase;

    private String api;

    private String method;

    private String version;

    private String sharingId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }
    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
    public String getSharingId() {
        return sharingId;
    }

    public void setSharingId(String sharingId) {
        this.sharingId = sharingId;
    }

    @Override
    public String toString() {
        return "NasPic{" +
            "id=" + id +
            ", cacheKey=" + cacheKey +
            ", type=" + type +
            ", size=" + size +
            ", passphrase=" + passphrase +
            ", api=" + api +
            ", method=" + method +
            ", version=" + version +
            ", sharingId=" + sharingId +
        "}";
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }
}
