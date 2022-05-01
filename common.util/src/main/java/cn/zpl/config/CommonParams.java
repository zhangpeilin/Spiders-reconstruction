package cn.zpl.config;

import cn.zpl.annotation.Value;
import cn.zpl.util.InitConfigFactory;

public class CommonParams {

    @Value("proxy_port")
    public static int proxyPort;
    @Value("hostname")
    public static String hostName;
    @Value("charSet")
    public static String charSet;

    public static String webSite = "bilibili";



    public static String commonHeaders = "User" +
            "-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0" +
            ".3945.130 Safari/537.36\nContent-Type:application/json\nConnection: keep-alive\n";

    static {
        InitConfigFactory.loadPropertyByClass(CommonParams.class,"common-config.properties");
    }
}
