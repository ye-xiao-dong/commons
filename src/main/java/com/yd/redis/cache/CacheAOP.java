package com.yd.redis.cache;

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

import java.util.Collection;
import java.util.Map;

/**
 * @author： 叶小东
 * @date： 2021/1/26 10:43
 */
@Aspect
@Slf4j
@Component
public class CacheAOP {

    @Autowired
    private RedisTemplate redisTemplate;

    private String keyPrefix;

    public CacheAOP(@Value("${redis.prefix}") String keyPrefix) {
        this.keyPrefix = keyPrefix + "_cache";
    }

    /**
     * 切入点
     *
     */
    @Pointcut("@annotation(Cache)")
    public void pointCut() {
        // 这里直接切入
    }

    /**
     * 环绕方法
     *
     * @param joinPoint
     * @param cache
     */
    @Around("pointCut() && @annotation(cache)")
    public Object around(ProceedingJoinPoint joinPoint, Cache cache) {
        try {
            Object result = null;

            // 这里先读缓存
            if (cache.readCache()){
                result = readCache(cache);
            }

            if (result != null){
                return result;
            }

            result = joinPoint.proceed();

            // 这里判断是否要刷新缓存
            if (cache.refreshCache() && result != null){
                refreshCache(cache, result);
            }

            return result;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    /**
     * 读缓存操作
     *
     * @param cache
     * @return
     */
    private Object readCache(Cache cache){
        Object result = null;
        String formatKey = RedisUtil.formatKey(keyPrefix, cache.key());
        switch (cache.redisType()){
            case HASH: {
                if (cache.hashKey().isEmpty()){
                    result = RedisUtil.mapGetAll(redisTemplate, formatKey);
                }
                else {
                    result = RedisUtil.mapGet(redisTemplate, formatKey, cache.hashKey());
                }
                break;
            }
            case LIST: {
                result = RedisUtil.listGetAll(redisTemplate, formatKey);
                break;
            }
            case STRING: {
                result = RedisUtil.getString(redisTemplate, formatKey);
                break;
            }
        }

        return result;
    }

    /**
     * 刷新缓存操作
     *
     * @param cache
     * @param object
     */
    private void refreshCache(Cache cache, Object object){
        String formatKey = RedisUtil.formatKey(keyPrefix, cache.key());
        switch (cache.redisType()){
            case HASH: {
                if (cache.hashKey().isEmpty()){
                    RedisUtil.mapPutAll(redisTemplate, formatKey, (Map)object, cache.cacheTime());
                }
                else {
                    RedisUtil.mapPut(redisTemplate, formatKey, cache.hashKey(), object, cache.cacheTime());
                }
                break;
            }
            case LIST: {
                RedisUtil.delete(redisTemplate, formatKey);
                RedisUtil.listAddAll(redisTemplate, formatKey, (Collection) object, cache.cacheTime());
                break;
            }
            case STRING: {
                RedisUtil.setString(redisTemplate, formatKey, (String) object, cache.cacheTime());
                break;
            }
        }
    }
}
