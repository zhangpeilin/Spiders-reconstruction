package cn.zpl.spider.on.bika.common;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "spider.bika")
public class BikaParams {

    public static boolean writeDB = true;
    public static boolean isForceDownload = true;

    private String email;
    private String password;
}
