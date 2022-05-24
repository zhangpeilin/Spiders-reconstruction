package cn.zpl.common;

import cn.zpl.annotation.Value;
import cn.zpl.util.InitConfigFactory;

public class BaiduAIParams {


    @Value("APP_ID")
    public static String APP_ID;
    @Value("API_KEY")
    public static String API_KEY;
    @Value("SECRET_KEY")
    public static String SECRET_KEY;

    static {
        InitConfigFactory.loadPropertyByClass(BaiduAIParams.class,"baidu-ai-params.properties");
    }
}
