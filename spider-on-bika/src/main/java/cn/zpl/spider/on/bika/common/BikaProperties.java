package cn.zpl.spider.on.bika.common;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author zpl
 */
@Data
@ConfigurationProperties(prefix = "spider.bika")
public class BikaProperties {

    public static boolean isForceDownload = false;
    private String email;
    private String password;
    private List<String> savePath;
    private boolean writeDb;
    private String keywords;
    private String tempPath;
}
