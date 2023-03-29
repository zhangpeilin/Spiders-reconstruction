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

    public static boolean isForceDownload = true;
    private String email;
    private String password;
    private List<String> savePath;
    private boolean writeDB = true;
    private String keywords;
}
