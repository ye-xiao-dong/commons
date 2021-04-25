package com.yd.redis.util;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author： 叶小东
 * @date： 2021/1/26 10:30
 */
@Slf4j
public class RedisUtil {

    /**
     * 格式化键名
     *
     * @param keyPrefix
     * @param key
     * @return
     */
    public static String formatKey(String keyPrefix, String key) {
        return keyPrefix + "_" + key;
    }

    /**
     * redis的删除
     *
     * @param redisTemplate
     * @param key
     */
    public static void delete(RedisTemplate redisTemplate, Object key) {
        try {
            redisTemplate.delete(key);
        }catch (Exception e){
            log.error("redis【delete】操作异常，异常信息：{}", JSON.toJSONString(e));
        }
    }

    /**
     * redis的删除，会比较值相同才删除
     *
     * @param redisTemplate
     * @param key
     * @param value
     */
    public static void deleteByKeyValue(RedisTemplate redisTemplate, Object key, Object value) {
        try {
            Object o = get(redisTemplate, key);
            if (o != null && o.equals(value)){
                delete(redisTemplate, key);
            }
        }catch (Exception e){
            log.error("redis【deleteByKeyValue】操作异常，异常信息：{}", JSON.toJSONString(e));
        }
    }

    /**
     * redis判断一个key是否存在
     *
     * @param redisTemplate
     * @param key
     */
    public static boolean hasKey(RedisTemplate redisTemplate, Object key) {
        try {
            return redisTemplate.hasKey(key);
        }catch (Exception e){
            log.error("redis【hasKey】操作异常，异常信息：{}", JSON.toJSONString(e));
            return false;
        }
    }

    /*-------------------------object操作---------------------------------*/

    /**
     * redis的object获取
     *
     * @param redisTemplate
     * @param key
     * @return
     */
    public static Object get(RedisTemplate redisTemplate, Object key){
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e){
            log.error("redis【get】操作异常，异常信息：{}", JSON.toJSONString(e));
            return null;
        }
    }

    /**
     * redis的object设置
     *
     * @param redisTemplate
     * @param key
     * @param value
     * @param millisecond
     */
    public static void set(RedisTemplate redisTemplate, Object key, Object value, long millisecond){
        try {
            redisTemplate.opsForValue().set(key, value, millisecond, TimeUnit.MILLISECONDS);
        } catch (Exception e){
            log.error("redis【set】操作异常，异常信息：{}", JSON.toJSONString(e));
        }
    }

    /*-------------------------string操作---------------------------------*/

    /**
     * redis的string获取
     *
     * @param redisTemplate
     * @param key
     * @return
     */
    public static String getString(RedisTemplate redisTemplate, String key){
        try {
            Object o = get(redisTemplate, key);
            return o == null ? null : o.toString();
        } catch (Exception e){
            log.error("redis【getString】操作异常，异常信息：{}", JSON.toJSONString(e));
            return null;
        }
    }

    /**
     * redis的string设置
     *
     * @param redisTemplate
     * @param key
     * @param value
     * @param millisecond
     */
    public static void setString(RedisTemplate redisTemplate, String key, String value, long millisecond){
        try {
            set(redisTemplate, key, value, millisecond);
        } catch (Exception e){
            log.error("redis【setString】操作异常，异常信息：{}", JSON.toJSONString(e));
        }
    }

    /**
     * 获得锁，这里为了防止释放锁失败，加上key的过期时间，根据业务需要设置，毫秒
     *
     * @param redisTemplate
     * @param key
     * @param value
     * @param millisecond
     * @param waitingTime
     * @return
     */
    public static boolean getLock(RedisTemplate redisTemplate, String key, String value, long millisecond, long waitingTime){
        try {
            long timeMillis = System.currentTimeMillis();
            do {
                Boolean lock = redisTemplate.opsForValue().setIfAbsent(key, value, millisecond, TimeUnit.MILLISECONDS);
                if (lock != null && lock){
                    return true;
                }
            } while (System.currentTimeMillis() - timeMillis < waitingTime);
            return false;
        } catch (Exception e){
            log.error("获取redis分布式锁异常，异常信息：{}", JSON.toJSONString(e));
            return false;
        }
    }

    /*-------------------------map操作---------------------------------*/

    /**
     * redis的map添加
     *
     * @param redisTemplate
     * @param key
     * @param hashKey
     * @param value
     * @param millisecond
     */
    public static void mapPut(RedisTemplate redisTemplate, String key, String hashKey, Object value, long millisecond) {
        try {
            redisTemplate.opsForHash().put(key, hashKey, value);
            redisTemplate.expire(key, millisecond, TimeUnit.MILLISECONDS);
        } catch (Exception e){
            log.error("redis【mapPut】操作异常，异常信息：{}", JSON.toJSONString(e));
        }
    }

    /**
     * redis的map添加全部
     *
     * @param redisTemplate
     * @param key
     * @param map
     * @param millisecond
     */
    public static void mapPutAll(RedisTemplate redisTemplate, String key, Map<String, Object> map, long millisecond) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            redisTemplate.expire(key, millisecond, TimeUnit.MILLISECONDS);
        } catch (Exception e){
            log.error("redis【mapPutAll】操作异常，异常信息：{}", JSON.toJSONString(e));
        }
    }

    /**
     * redis的map获取
     *
     * @param redisTemplate
     * @param key
     * @param hashKey
     * @return
     */
    public static Object mapGet(RedisTemplate redisTemplate, String key, String hashKey) {
        try {
            return redisTemplate.opsForHash().get(key, hashKey);
        } catch (Exception e){
            log.error("redis【mapGet】操作异常，异常信息：{}", JSON.toJSONString(e));
            return null;
        }
    }

    /**
     * redis获取整个map
     *
     * @param redisTemplate
     * @param key
     * @return
     */
    public static Map mapGetAll(RedisTemplate redisTemplate, String key) {
        try {
            if (hasKey(redisTemplate, key)) {
                return redisTemplate.opsForHash().entries(key);
            }
            else {
                return null;
            }
        } catch (Exception e){
            log.error("redis【mapGetAll】操作异常，异常信息：{}", JSON.toJSONString(e));
            return null;
        }
    }

    /*-------------------------list操作---------------------------------*/

    /**
     * redis的list获取，当key没有的时候默认返回空集合
     *
     * @param redisTemplate
     * @param key
     * @return
     */
    public static List listGetAll(RedisTemplate redisTemplate, String key) {
        try {
            if (hasKey(redisTemplate, key)){
                return redisTemplate.opsForList().range(key, 0, -1);
            }
            else {
                return null;
            }
        } catch (Exception e){
            log.error("redis【listGetAll】操作异常，异常信息：{}", JSON.toJSONString(e));
            return null;
        }
    }

    /**
     * redis的添加整个集合
     *
     * @param redisTemplate
     * @param key
     * @param value
     * @param millisecond
     */
    public static void listAddAll(RedisTemplate redisTemplate, String key, Collection value, long millisecond) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            redisTemplate.expire(key, millisecond, TimeUnit.MILLISECONDS);
        } catch (Exception e){
            log.error("redis【listAddAll】操作异常，异常信息：{}", JSON.toJSONString(e));
        }
    }

}

