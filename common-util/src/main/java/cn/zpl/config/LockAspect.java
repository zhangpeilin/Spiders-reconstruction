package cn.zpl.config;

import cn.zpl.annotation.DistributeLock;
import cn.zpl.annotation.DistributedLockKey;
import cn.zpl.exception.LockFailException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@Aspect
public class LockAspect {

    @Resource
    private RedissonClient redissonClient;

    @Around("@annotation(cn.zpl.annotation.DistributeLock)")
    public Object aroundAdvice(ProceedingJoinPoint pjp) throws Throwable  {
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        Method method = methodSignature.getMethod();
        DistributeLock distributeLock = method.getAnnotation(DistributeLock.class);
        StringBuilder lockKey = new StringBuilder(distributeLock.value());
        long waitTime = distributeLock.waitTime();
        long holdTime = distributeLock.holdTime();
        Object target = pjp.getTarget();
        Class<?> clazz = target.getClass();
        Field[] fields = clazz.getFields();
        Parameter[] parameters = method.getParameters();
        if (ArrayUtils.isNotEmpty(parameters)) {
            for (int i = 0; i < parameters.length; i++) {
                DistributedLockKey annotation = parameters[i].getAnnotation(DistributedLockKey.class);
                if (annotation != null) {
                    Object[] args = pjp.getArgs();
                    String param = String.valueOf(args[i]);
                    if (StringUtils.isNotBlank(param)) {
                        lockKey.append(":").append(param);
                        break;
                    }
                }
            }
        }
        if (ArrayUtils.isNotEmpty(fields)) {
            for (int i = 0; i < fields.length; i++) {
                DistributedLockKey annotation = fields[i].getAnnotation(DistributedLockKey.class);
                if (annotation != null) {
                    fields[i].setAccessible(true);
                    Object filedValue = fields[i].get(target);
                    if (StringUtils.isNotBlank(String.valueOf(filedValue))) {
                        lockKey.append(":").append(filedValue);
                    }
                }
            }
        }
        log.info("lockKey:{}", lockKey);
        RLock fairLock = redissonClient.getFairLock(lockKey.toString());
        boolean lock = fairLock.tryLock(waitTime, holdTime, TimeUnit.SECONDS);
        if (!lock) {
            log.debug("获取锁失败{}", lockKey);
            throw new LockFailException("获取锁失败");
        }
        Object result = null;
        try {
            result = pjp.proceed();
        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            if (fairLock.isHeldByCurrentThread()) {
                fairLock.unlock();
            }
        }
        return result;
    }
}
