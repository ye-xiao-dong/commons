package com.yd.redis.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author： 叶小东
 * @date： 2021/1/26 10:42
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache {

    // 缓存key
    String key();

    // 缓存最长时间
    long cacheTime();

    // redis 操作类型
    RedisType redisType();

    // 获取数据之前读缓存
    boolean readCache() default true;

    // 更新数据之后刷新缓存
    boolean refreshCache() default true;

    // hash key，使用map类型的时候用上
    String hashKey() default "";

}
