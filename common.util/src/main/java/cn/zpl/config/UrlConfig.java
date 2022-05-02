package cn.zpl.config;

import cn.zpl.annotation.Value;
import cn.zpl.util.InitConfigFactory;

public class UrlConfig {

    @Value("url.saveOrUpdateBika")
    public static String saveOrUpdateBika;
    @Value("url.saveOrUpdateVideoInfo")
    public static String saveOrUpdateVideoInfo;
    @Value("url.saveOrUpdateEhentai")
    public static String saveOrUpdateEhentai;
    @Value("url.getVideoInfoById")
    public static String getVideoInfoById;
    @Value("url.saveOrUpdateExceptionList")
    public static String getSaveOrUpdateExceptionList;

    @Value("url.getExceptionListById")
    public static String getExceptionListById;

    static {
        InitConfigFactory.loadPropertyByClass(UrlConfig.class,"common-dao-center-url.properties");
    }
}
