package cn.zpl.dao.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("spider.dao")
public class MyProperties {

    String createTablePath;
}
