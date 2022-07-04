package cn.zpl.commondaocenter.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;

@Configuration
public class SpringContext implements ApplicationContextAware {
   private static ApplicationContext applicationContext;
    public static ApplicationContext getApplicationContext() {
       return applicationContext;
}
     public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
     }
    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        SpringContext.applicationContext = applicationContext;
    }

    public static <T> T getBean(Class<T> clazz, String beanId){
        Assert.isTrue(StringUtils.hasText(beanId), "beanId must not null!");
        Assert.isTrue(applicationContext.containsBean(beanId), "beanId:[" + beanId + "] is not exists!");
        return (T) applicationContext.getBean(beanId);
    }
}