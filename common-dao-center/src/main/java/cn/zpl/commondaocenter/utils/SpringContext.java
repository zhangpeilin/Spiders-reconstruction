package cn.zpl.commondaocenter.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

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
}