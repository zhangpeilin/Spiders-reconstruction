package cn.zpl.commondaocenter.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Configuration
public class SpringContext implements ApplicationContextAware {
    private static ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContext.applicationContext = applicationContext;
    }

    public static Object getBean(String beanId) {
        return applicationContext.getBean(beanId);
    }
    public static <T> T getBean(Class<T> clazz, String beanId){
        Assert.isTrue(StringUtils.hasText(beanId), "beanId must not null!");
        Assert.isTrue(applicationContext.containsBean(beanId), "beanId:[" + beanId + "] is not exists!");
        return (T) applicationContext.getBean(beanId);
    }
}
