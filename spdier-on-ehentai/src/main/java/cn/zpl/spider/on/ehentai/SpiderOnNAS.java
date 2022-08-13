package cn.zpl.spider.on.ehentai;

import cn.zpl.common.bean.NasPage;
import cn.zpl.common.bean.NasPic;
import cn.zpl.config.UrlConfig;
import cn.zpl.spider.on.ehentai.thread.SpiderOnNASThread;
import cn.zpl.spider.on.ehentai.thread.SpiderOnNASThreadV2;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.CrudTools;
import cn.zpl.util.DownloadTools;
import cn.zpl.util.SaveLogForImages;
import com.google.gson.JsonElement;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collection;
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

class NASDownload {
    public static void main(String[] args) {
        DownloadTools tools = DownloadTools.getInstance(50);
        UrlConfig config = new UrlConfig();
        config.setNothing("*");
        config.setCommonQueryUrl("http://localhost:8080/common/dao/api/query/%1$s?fetchProperties=[%2$s]&condition=[%3$s]&size=%4$s");
        config.setCommonSaveUrl("http://localhost:8080/common/dao/api/save");
        CrudTools<Object> crudTools = CrudTools.getInstance(config);
        List<NasPic> nasPics = crudTools.commonApiQueryBySql(null, NasPic.class);
    }
}
