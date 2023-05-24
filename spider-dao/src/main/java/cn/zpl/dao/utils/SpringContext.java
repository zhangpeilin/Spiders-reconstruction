package cn.zpl.dao.utils;

import cn.zpl.common.bean.Bika;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import javax.annotation.Nonnull;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Configuration("dao.SpringContext")
public class SpringContext implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    private static final Set<Class<?>> classSet = new HashSet<>();

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    @SuppressWarnings("unchecked")
    public static <T> IService<T> getBeanWithGenerics(Class<T> tClass) {
        ResolvableType resolvableType = ResolvableType.forClassWithGenerics(IService.class, tClass);
        ObjectProvider<T> beanProvider = applicationContext.getBeanProvider(resolvableType);
        return (IService<T>) beanProvider.getIfAvailable();
    }

    public static Object getBeanDefinitionName(String entityName) {
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        if (classSet.isEmpty()) {
            Arrays.stream(beanDefinitionNames).forEach(s -> {
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
        ResolvableType resolvableType = ResolvableType.forClassWithGenerics(IService.class, clazz);
        ObjectProvider<Object> beanProvider = applicationContext.getBeanProvider(resolvableType);
        Object ifAvailable = beanProvider.getIfAvailable();
//        Map<String, IService> beansOfType = applicationContext.getBeansOfType(IService.class);
        Assert.isTrue(StringUtils.hasText(beanId), "beanId must not null!");
        Assert.isTrue(applicationContext.containsBean(beanId), "beanId:[" + beanId + "] is not exists!");
        return (T) applicationContext.getBean(beanId);
    }
}