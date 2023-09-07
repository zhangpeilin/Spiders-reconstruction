package cn.zpl.spider.on.bilibili.manga.thread;

import cn.zpl.spider.on.bilibili.manga.util.BilibiliCommonUtils;
import cn.zpl.spider.on.bilibili.manga.util.BilibiliMangaProperties;
import cn.zpl.spider.on.bilibili.manga.util.BilibiliProperties;
import cn.zpl.util.CommonIOUtils;
import com.google.gson.JsonElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * {"buy_method":1,"ep_id":"1188472","comic_id":"31329"}，普通购买是1
 */
@Slf4j
@Scope("prototype")
@Component
public class BatchBuyEpisodeThread implements Callable<Map<String, Map<String, Integer>>> {

    @Resource
    BilibiliCommonUtils utils;
    @Resource
    BilibiliMangaProperties properties;

    @Resource
    BilibiliProperties bilibiliProperties;
    String epId;

    public void setEpId(String epId) {
        this.epId = epId;
    }

    private Map<String, Integer> doBusiness(String ep_id) {
        if (StringUtils.isEmpty(ep_id)) {
            log.error("未传入漫画信息，方法返回");
            return Collections.emptyMap();
        }
        String param = "{\"ep_id\":" + ep_id + "}";
        String result = utils.postUrl(properties.getGetEpisodeBuyInfoUrl(), param, properties.getCommonHeaders() + bilibiliProperties.getCookies());
        JsonElement resultJson = CommonIOUtils.paraseJsonFromStr(result);
        if (CommonIOUtils.getFromJson2Str(resultJson, "code").equalsIgnoreCase("unauthenticated")) {
            log.error("需要重新登录");
            return Collections.emptyMap();
        }
        int remainSilver = CommonIOUtils.getFromJson2Integer(resultJson, "data-remain_silver");
        boolean is_locked = CommonIOUtils.getFromJson2Boolean(resultJson, "data-is_locked");
        String comic_id = CommonIOUtils.getFromJson2Str(resultJson, "data-comic_id");
        //需要解锁的解锁，已经解锁的跳过
        if (is_locked && remainSilver > 0) {
            //满足条件，调用解锁方法BuyEpisode
            param = "{\"buy_method\":1,\"ep_id\":" + ep_id + ",\"comic_id\":" + comic_id + "}";
            String buyResult = utils.postUrl(properties.getBuyEpisodeUrl(), param,
                    properties.getCommonHeaders() + bilibiliProperties.getCookies());
            log.debug(buyResult);
            if (CommonIOUtils.getIntegerFromJson(CommonIOUtils.paraseJsonFromStr(buyResult), "code") == 0) {
                //购买完成，调用漫画下载进程
                log.warn("章节" + ep_id + "购买完成");
            } else {
                log.error("购买失败，直接返回");
                return Collections.emptyMap();
            }
        }
        resultJson = CommonIOUtils.paraseJsonFromStr(result);
        if (CommonIOUtils.getFromJson2Str(resultJson, "code").equalsIgnoreCase("unauthenticated")) {
            //需要登录，那么直接退出系统
            log.error("需要重新登录");
            return Collections.emptyMap();
        }
        remainSilver = CommonIOUtils.getFromJson2Integer(resultJson, "data-remain_silver");
        return Collections.singletonMap(ep_id, remainSilver);
    }

    @Override
    public Map<String, Map<String, Integer>> call() {
        return Collections.singletonMap(epId, doBusiness(epId));
    }
}
