package com.yd.redis.cache;

import java.lang.annotation.*;

/**
 * @author： 叶小东
 * @date： 2021/1/26 10:42
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Caches.class)
public @interface Cache {

    // 缓存key，SpEL表达式
    String key();

    // redis 操作类型
    RedisType redisType();

    // 缓存最长时间，毫秒
    long cacheTime() default 1;

    // 获取数据之前读缓存
    boolean readCache() default true;

    // 更新数据之后刷新缓存
    boolean refreshCache() default true;

    // hash key，使用map类型的时候用上，SpEL表达式
    String hashKey() default "";

}
