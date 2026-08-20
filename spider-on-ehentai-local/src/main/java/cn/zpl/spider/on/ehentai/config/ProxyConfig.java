package cn.zpl.spider.on.ehentai.config;

import cn.zpl.config.CommonParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 启动时把配置的代理写入 CommonParams（common-util 请求工具统一读取）
 */
@Slf4j
@Component
public class ProxyConfig {

    @Resource
    EhentaiConfig ehentaiConfig;

    @PostConstruct
    public void init() {
        if (ehentaiConfig.getProxyHost() != null && !ehentaiConfig.getProxyHost().trim().isEmpty()) {
            CommonParams.hostName = ehentaiConfig.getProxyHost();
            CommonParams.proxyPort = ehentaiConfig.getProxyPort();
            log.info("请求代理已配置：{}:{}", CommonParams.hostName, CommonParams.proxyPort);
        }
    }
}
