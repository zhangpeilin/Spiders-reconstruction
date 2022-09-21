package cn.zpl.spider.on.bika.proxy;

import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 *
 * @author zhangpl1
 * @date 2022/8/9
 */
@Component
//@Aspect
@Order(2)
public class MyAopAdvice {


    @Pointcut(value = "execution(* cn.zpl.*.*.*(..))")
    public void pointcut(){
    }
    @Before("pointcut()")
    public void before() {
        System.out.println("这里是MyAopAdvice增强方法...");
    }


}
