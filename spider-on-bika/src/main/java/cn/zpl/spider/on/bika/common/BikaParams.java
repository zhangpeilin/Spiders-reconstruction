package cn.zpl.spider.on.bika.common;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "spider.bika")
public class BikaParams {

    public static boolean isForceDownload = true;
    private String email;
    private String password;
    private List<String> savePath;
    private boolean writeDB = true;
}
