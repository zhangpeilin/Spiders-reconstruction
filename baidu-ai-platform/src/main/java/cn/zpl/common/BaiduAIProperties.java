package cn.zpl.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Data
@RefreshScope
@ConfigurationProperties(prefix = "spider.baidu")
public class BaiduAIProperties {


    String app_id;
    String api_key;
    String secret_key;
}
