package cn.zpl.commondaocenter.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@Data
@PropertySource({"classpath:common-dao-center-url.properties"})
@ConfigurationProperties("url")
public class UrlConfig {
    private String saveBika;
}
