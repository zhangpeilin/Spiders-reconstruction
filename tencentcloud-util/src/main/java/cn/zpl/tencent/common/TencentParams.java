package cn.zpl.tencent.common;

import cn.zpl.annotation.Value;
import cn.zpl.util.InitConfigFactory;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spider.tencent")
public class TencentParams {

    public static String SecretId;
    public static String SecretKey;
    public static String region;

    public String corpId;

    public String corpSecret;

    public int applicationId;

    public String toUser;

    public boolean watchWebsite;

    public String weiyunCookie;

    String dst_ppdir_key;

    String dst_pdir_key;
}
