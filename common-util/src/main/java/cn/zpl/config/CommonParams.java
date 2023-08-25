package cn.zpl.config;

import cn.zpl.annotation.Value;
import cn.zpl.util.InitConfigFactory;

public class CommonParams {

    @Value("proxy_port")
    public static int proxyPort;
    @Value("hostname")
    public static String hostName;
    static {
        InitConfigFactory.loadPropertyByClass(CommonParams.class,"common-config.properties");
    }
}
