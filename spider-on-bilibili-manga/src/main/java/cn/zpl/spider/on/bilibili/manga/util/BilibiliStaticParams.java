package cn.zpl.spider.on.bilibili.manga.util;


import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.InitConfigFactory;
import cn.zpl.util.SaveLog;
import cn.zpl.util.URLConnectionTool;
import org.springframework.beans.factory.annotation.Value;

public class BilibiliStaticParams {


    public static String getComicDetailUrl = "https://manga.bilibili.com/twirp/comic.v2.Comic/ComicDetail?device=pc&platform=web";
    public static String getImageIndexUrl = "https://manga.bilibili.com/twirp/comic.v1.Comic/GetImageIndex?device=pc&platform=web";
    public static String ImageTokenUrl = "https://manga.bilibili.com/twirp/comic.v1.Comic/ImageToken?device=pc&platform=web";
    public static String FreeMangaListUrl_GetClassPageSixComics = "https://manga.bilibili.com/twirp/comic.v1" +
            ".Comic/GetClassPageSixComics?device=pc&platform=web";
    public static String FreeMangaListUrl_GetClassPageHomeBanner = "https://manga.bilibili.com/twirp/comic.v1" +
            ".Comic/GetClassPageHomeBanner?device=pc&platform=web";
    public static String waitForFreeListUrl = "https://manga.bilibili.com/twirp/comic.v1.Comic/ClassPage?device=pc&platform=web";
    public static String GetEpisodeBuyInfoUrl = "https://manga.bilibili.com/twirp/comic.v1" +
            ".Comic/GetEpisodeBuyInfo?device=pc&platform=web";
    public static String BuyEpisodeUrl = "https://manga.bilibili.com/twirp/comic.v1" +
            ".Comic/BuyEpisode?device=pc&platform=web";
    public static String SearchUrl = "https://manga.bilibili.com/twirp/comic.v1.Comic/Search?device=pc&platform=web";

    /**
     * 获取页面布局的id，以便得到限免模块的id请求数据
     */
    public static String GetClassPageAllTabs = "https://manga.bilibili.com/twirp/comic.v1" +
            ".Comic/GetClassPageAllTabs?device=pc&platform=web";

    /**
     * 根据布局id获取限免模块的id
     */
    public static String GetClassPageLayout = "https://manga.bilibili.com/twirp/comic.v1" +
            ".Comic/GetClassPageLayout?device=pc&platform=web";

    public static String Bookshelf = "https://manga.bilibili.com/twirp/bookshelf.v1.Bookshelf/ListFavorite?device=pc&platform=web";

    public static String commonHeaders = "User" +
            "-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0" +
            ".3945.130 Safari/537.36\nContent-Type:application/json\n";

    public static String pageHeaders = "Content-Type: application/x-www-form-urlencoded;charset=UTF-8";

    @Value("bilibili_user_cookies")
    public static String bilibiliCookies;

    @Value("tmp_save_path")
    public static String tmp_save_path;
    @Value("video_save_path")
    public static String video_save_path;
    @Value("manga_save_path")
    public static String manga_save_path;
    @Value("favourite_save_path")
    public static String favourite_save_path;

    static {
        InitConfigFactory.loadPropertyByClass(BilibiliStaticParams.class, "bilibili-config.properties");
    }
}
