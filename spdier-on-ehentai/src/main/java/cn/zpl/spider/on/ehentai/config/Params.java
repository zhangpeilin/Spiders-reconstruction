package cn.zpl.spider.on.ehentai.config;

import cn.zpl.annotation.Value;
import cn.zpl.frame.MyJFrame;
import cn.zpl.util.InitConfigFactory;

public class Params {
    @Value("exhentai_cookies")
    public static String exhentai_cookies;

    @Value("unzip")
    public static boolean unzip;

    @Value("save_path")
    public static String save_path;

    public static boolean downloadOrigionalPic = false;

    public static MyJFrame mainFrame;

    @Value("url.saveOrUpdateEhentai")
    public static String saveOrUpdateEhentai;

    static {
        InitConfigFactory.loadPropertyByClass(Params.class,"exhentai-config.properties");
    }
}
