package cn.zpl.commondaocenter.utils;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import javax.annotation.Nonnull;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Configuration
public class SpringContext implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    private static final Set<Class<?>> classSet = new HashSet<>();

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    public static Object getBeanDefinitionName(String entityName) {
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        if (classSet.isEmpty()) {
            Arrays.stream(beanDefinitionNames).forEach(s -> {
//                Object bean = getBean(s);
//                String beanClassName = ((AnnotationConfigServletWebServerApplicationContext) applicationContext).getBeanFactory().getBeanDefinition(s).getBeanClassName();
//                ((AnnotationConfigServletWebServerApplicationContext) applicationContext).getBeanFactory().getBeanDefinition(s);
                try {
                    Class<?> beanClass = ((AbstractBeanDefinition) ((AnnotationConfigServletWebServerApplicationContext) applicationContext).getBeanFactory().getBeanDefinition(s)).getBeanClass();
                    classSet.add(beanClass);
                } catch (Exception ignored) {
                }

            });
        }
        Optional<Class<?>> first = classSet.stream().filter(aClass -> !aClass.isInterface() && (aClass.getGenericSuperclass() instanceof ParameterizedType) && ((ParameterizedTypeImpl) aClass.getGenericSuperclass()).getActualTypeArguments().length > 1 && ((Class<?>) ((ParameterizedTypeImpl) aClass.getGenericSuperclass()).getActualTypeArguments()[1]).getSimpleName().equalsIgnoreCase(entityName)).findFirst();
        return first.map(aClass -> applicationContext.getBean(aClass)).orElse(null);
    }

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        SpringContext.applicationContext = applicationContext;
    }

    public static <T> T getBean(Class<T> clazz, String beanId) {
        Assert.isTrue(StringUtils.hasText(beanId), "beanId must not null!");
        Assert.isTrue(applicationContext.containsBean(beanId), "beanId:[" + beanId + "] is not exists!");
        return (T) applicationContext.getBean(beanId);
    }
}