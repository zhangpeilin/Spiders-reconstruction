package cn.zpl.spider.on.ehentai.config;

import cn.zpl.config.SpringContext;
import org.springframework.cloud.bus.event.AckRemoteApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class ZDBListener implements ApplicationListener<AckRemoteApplicationEvent>, Ordered {
    @Override
    public void onApplicationEvent(AckRemoteApplicationEvent event) {
        EhentaiConfig beanWithGenerics = SpringContext.getBeanWithGenerics(EhentaiConfig.class);
        DataSource dataSource = SpringContext.getBeanWithGenerics(DataSource.class);
        System.out.println(dataSource);
        System.out.println(beanWithGenerics.getSavePath());
        System.out.println("监听到配置刷新" + event);
    }

    @Override
    public int getOrder() {
       return Ordered.LOWEST_PRECEDENCE;
    }
}
