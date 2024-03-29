package cn.zpl.spider.on.ehentai;

import cn.zpl.common.bean.NasPage;
import cn.zpl.common.bean.NasPic;
import cn.zpl.common.bean.Page;
import cn.zpl.config.UrlConfig;
import cn.zpl.pojo.DownloadDTO;
import cn.zpl.pojo.MultiPartInfoHolder;
import cn.zpl.spider.on.ehentai.thread.SpiderOnNASThread;
import cn.zpl.spider.on.ehentai.thread.SpiderOnNASThreadV2;
import cn.zpl.thread.OneFileOneThread;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.CrudTools;
import cn.zpl.util.DownloadTools;
import cn.zpl.util.SaveLogForImages;
import cn.zpl.util.URLConnectionTool;
import com.google.gson.JsonElement;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class SpiderOnNAS {

    public static void main(String[] args) {
        DownloadTools tools = DownloadTools.getInstance(50);
        int count = 35451;
        for (int i = 0; i < count; ) {
            tools.ThreadExecutorAdd(new SpiderOnNASThread(i));
            i = i + 100;
        }
        tools.shutdown();
    }
}

class saveLog {
    public static void main(String[] args) {
        File base = new File("C:\\nas");
        Collection<File> list = FileUtils.listFiles(base, null, true);
        for (File file : list) {
            SaveLogForImages.saveLog(file);
        }
    }
}

class SpiderOnNASV2 {
    public static void main(String[] args) {
        DownloadTools tools = DownloadTools.getInstance(50);
        int count = 35451;
        for (int i = 0; i < count; ) {
            tools.ThreadExecutorAdd(new SpiderOnNASThreadV2(i));
            i = i + 100;
        }
        tools.shutdown();
    }
}

@Slf4j
class NASDownload {
    public static void main(String[] args) {
        DownloadTools tools = DownloadTools.getInstance(50);
        UrlConfig config = new UrlConfig();
        config.setNothing("*");
        config.setCommonQueryUrl("http://localhost:8080/common/dao/api/query/%1$s?fetchProperties=[%2$s]&condition=[%3$s]&page=%4$s");
        config.setCommonSaveUrl("http://localhost:8080/common/dao/api/save");
        CrudTools crudTools = new CrudTools();
        crudTools.setConfig(config);
        String headers = "Accept: */*\n" +
                "Accept-Encoding: gzip, deflate\n" +
                "Accept-Language: zh-CN,zh;q=0.9\n" +
                "Connection: keep-alive\n" +
//                "Content-Length: 281\n" +
//                "Content-Type: application/x-www-form-urlencoded; charset=UTF-8\n" +
                "Cookie: sharing_sid=uzo-AmyC8gr1bNxj5tz38vjAvSpLxxIZ\n" +
                "Host: www.ariess.info:5000\n" +
                "Origin: http://www.ariess.info:5000\n" +
                "Referer: http://www.ariess.info:5000/mo/sharing/pKQcitKUk\n" +
                "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36\n" +
                "X-SYNO-SHARING: pKQcitKUk";
        List<NasPic> nasPics;
        int i = 1;
        for (;i < 400; i++) {
            nasPics = crudTools.commonApiQuery(null, null, NasPic.class, new Page(i, 100));
            if (nasPics.isEmpty()) {
                log.debug("查询第{}页结果为空", i);
                return;
            }
            dubusiness(tools, headers, nasPics, i);
        }
    }

    private static void dubusiness(DownloadTools tools, String headers, List<NasPic> nasPics, int i) {
        tools.restart(50);
        List<MultiPartInfoHolder> holders = new ArrayList<>();
        log.debug("当前下载：page->{}", i);
        for (NasPic nasPic : nasPics) {
            DownloadDTO dto = new DownloadDTO();
            dto.setUrl(nasPic.getUrl());
            dto.setFileName(nasPic.getId());
            dto.setHeader(headers);
            List<String> path = new ArrayList<>();
            path.add("C:\\视频爬虫\\nas2");
            dto.setReferer(dto.getUrl());
            dto.setSavePath(CommonIOUtils.makeFilePath(path, dto.getFileName()));
            dto.setAlwaysRetry();
            if (new File(dto.getSavePath()).exists() && SaveLogForImages.isCompelete(dto)) {
                log.debug(dto.getSavePath() + "已下载，跳过");
                continue;
            }
            if ("video".equalsIgnoreCase(nasPic.getType())) {
                URLConnectionTool.getDataLength(dto);
                tools.MultipleThreadWithLog(dto);
                holders.add(dto.getInfoHolder());
            } else {
                tools.ThreadExecutorAdd(new OneFileOneThread(dto));
            }
        }
        tools.shutdown();
//        for (MultiPartInfoHolder holder : holders) {
//            if (holder.isComplete()) {
//                SaveLogForImages.saveLog(holder.getDownloadDTO());
//            }
//        }
    }
}
