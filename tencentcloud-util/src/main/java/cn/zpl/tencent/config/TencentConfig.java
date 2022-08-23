package cn.zpl.tencent.config;

import cn.zpl.tencent.common.TencentParams;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TencentParams.class)
public class TencentConfig {
}
