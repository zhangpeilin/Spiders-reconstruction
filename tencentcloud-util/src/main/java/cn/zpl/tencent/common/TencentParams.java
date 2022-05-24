package cn.zpl.tencent.common;

import cn.zpl.annotation.Value;
import cn.zpl.util.InitConfigFactory;

public class TencentParams {

    @Value("SecretId")
    public static String SecretId;
    @Value("SecretKey")
    public static String SecretKey;
    @Value("region")
    public static String region;
    static {
        InitConfigFactory.loadPropertyByClass(TencentParams.class,"tencent-ai-params.properties");
    }
}
