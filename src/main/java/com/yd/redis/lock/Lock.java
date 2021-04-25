package com.yd.redis.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author： 叶小东
 * @date： 2021/1/26 10:32
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Lock {

    // 锁名
    String lockName();

    // 锁值
    String lockValue() default "lock";

    // 获取锁的最长等待时间
    long waitingTime();

    // 锁住的最长时间
    long lockingTime();

    // 没有抢到锁的日志记录
    boolean failLog() default true;

    // 没有抢到锁抛出异常
    boolean failThrowException() default false;

    // 没有抢到锁的默认返回类型
    Class<?> failDefaultReturn() default Void.class;

}
