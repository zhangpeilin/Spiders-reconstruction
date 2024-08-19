package cn.zpl.config;


import cn.zpl.util.CommonProperties;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;

@Configuration
@EnableConfigurationProperties(CommonProperties.class)
public class SpringContext implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }
    @SuppressWarnings("unchecked")
    public  static <T,R> R getBeanWithGenerics(Class<R> rClass, Class<?>... tClass) {
        ResolvableType resolvableType = ResolvableType.forClassWithGenerics(rClass, tClass);
        ObjectProvider<T> beanProvider = applicationContext.getBeanProvider(resolvableType);
        return (R) beanProvider.getIfAvailable();
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        SpringContext.applicationContext = applicationContext;
    }
}