package com.yd.redis.lock;

import com.yd.redis.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author： 叶小东
 * @date： 2021/1/26 10:33
 */
@Aspect
@Slf4j
@Component
public class LockAOP {

    @Autowired
    private RedisTemplate redisTemplate;

    private String keyPrefix;

    public LockAOP(@Value("${redis.prefix}") String keyPrefix) {
        this.keyPrefix = keyPrefix + "_lock";
    }

    /**
     * 切入点
     *
     */
    @Pointcut("@annotation(Lock)")
    public void pointCut() {
        // 这里直接切入
    }

    /**
     * 环绕方法
     *
     * @param joinPoint
     * @param lock
     */
    @Around("pointCut() && @annotation(lock)")
    public Object around(ProceedingJoinPoint joinPoint, Lock lock) {
        String lockName = RedisUtil.formatKey(keyPrefix, lock.lockName());

        try {
            Object result = null;

            // 这里先获取分布式锁
            if (RedisUtil.getLock(redisTemplate, lockName, lock.lockingTime(), lock.waitingTime())){
                result = joinPoint.proceed();
            }
            else {

                // 没获取到锁根据配置进行处理
                if (lock.failLog()){
                    log.info("{}，未抢到分布式锁，参数：{}", lock.lockName(),joinPoint.getArgs());
                }

                if (lock.failThrowException()){
                    throw new RuntimeException("未抢到分布式锁");
                }

                if (!Void.class.equals(lock.failDefaultReturn())){
                    result = lock.failDefaultReturn().newInstance();
                }
            }

            return result;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        } finally {
            RedisUtil.delete(redisTemplate, lockName);
        }
    }
}
