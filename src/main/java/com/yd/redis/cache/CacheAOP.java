package com.yd.redis.cache;

import com.yd.redis.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
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

            // 先格式化key值
            Map<String, Object> variables = getVariables(joinPoint);
            String key = formatKey(cache.key(), variables);
            String hashKey = formatKey(cache.hashKey(), variables);

            // 这里先读缓存
            if (cache.readCache()){
                result = readCache(key, cache.redisType(), hashKey);
            }

            if (result != null){
                return result;
            }

            result = joinPoint.proceed();

            // 这里判断是否要刷新缓存
            if (cache.refreshCache() && result != null){
                refreshCache(key, cache.redisType(), hashKey, cache.cacheTime(), result);
            }

            return result;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    /**
     * 获取参数名和参数值的对应关系
     *
     * @param joinPoint
     * @return
     */
    private Map<String, Object> getVariables(ProceedingJoinPoint joinPoint){
        Map<String, Object> variables = new HashMap<>();

        // 先把参数放进去
        Object[] args = joinPoint.getArgs();
        String[] names = ((MethodSignature)joinPoint.getSignature()).getParameterNames();
        for (int i = 0, j = names.length, k = args.length; i < j; i++){
            Object value = null;
            if (i < k){
                value = args[i];
            }

            variables.put(names[i], value);
        }

        // 再把对象放进去
        variables.put("target", joinPoint.getTarget());
        variables.put("this", joinPoint.getThis());

        return variables;
    }

    /**
     * 格式化缓存key
     *
     * @param spEL
     * @param variables
     * @return
     */
    private String formatKey(String spEL, Map<String, Object> variables){
        if (spEL.isEmpty()){
            return spEL;
        }

        // 这里要一个SpEl容器
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariables(variables);

        // 解析SpEl表达式
        ExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(spEL);
        String key = String.valueOf(expression.getValue(context));

        // 然后拼上前缀
        return RedisUtil.formatKey(keyPrefix, key);
    }

    /**
     * 读缓存操作
     *
     * @param key
     * @param redisType
     * @param hashKey
     * @return
     */
    private Object readCache(String key, RedisType redisType, String hashKey){
        Object result = null;
        switch (redisType){
            case HASH: {
                if (hashKey.isEmpty()){
                    result = RedisUtil.mapGetAll(redisTemplate, key);
                }
                else {
                    result = RedisUtil.mapGet(redisTemplate, key, hashKey);
                }
                break;
            }
            case LIST: {
                result = RedisUtil.listGetAll(redisTemplate, key);
                break;
            }
            case STRING: {
                result = RedisUtil.getString(redisTemplate, key);
                break;
            }
        }

        return result;
    }

    /**
     * 刷新缓存操作
     *
     * @param key
     * @param redisType
     * @param hashKey
     * @param cacheTime
     * @param object
     */
    private void refreshCache(String key, RedisType redisType, String hashKey, long cacheTime, Object object){
        switch (redisType){
            case HASH: {
                if (hashKey.isEmpty()){
                    RedisUtil.mapPutAll(redisTemplate, key, (Map)object, cacheTime);
                }
                else {
                    RedisUtil.mapPut(redisTemplate, key, hashKey, object, cacheTime);
                }
                break;
            }
            case LIST: {
                RedisUtil.delete(redisTemplate, key);
                RedisUtil.listAddAll(redisTemplate, key, (Collection) object, cacheTime);
                break;
            }
            case STRING: {
                RedisUtil.setString(redisTemplate, key, (String) object, cacheTime);
                break;
            }
        }
    }
}
