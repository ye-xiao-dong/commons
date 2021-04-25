package com.yd.elasticjob;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.yd.elasticjob.annotation.Param;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author： 叶小东
 * @date： 2021/4/25 10:31
 */
@Slf4j
@Data
public class Job implements SimpleJob {

    private Method method;

    private Object bean;

    @Override
    public void execute(ShardingContext shardingContext) {
        if (method != null && bean != null){

            Object[] params = null;
            if (method.getParameterCount() > 0){

                JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(shardingContext));
                Class<?>[] parameterTypes = method.getParameterTypes();
                Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                String[] parameterNames = new LocalVariableTableParameterNameDiscoverer().getParameterNames(method);
                params = new Object[parameterTypes.length];
                for(int i = 0, j = parameterTypes.length; i < j; i++) {
                    String name = parameterTypes[i].getName();
                    if (name.contains(".")){
                        name = name.substring(name.lastIndexOf(".") + 1);
                    }

                    if ("ShardingContext".equals(name)){
                        params[i] = shardingContext;
                    }
                    else {

                        String key = null;

                        // 先找到这个参数上的注解的名字
                        for (Annotation annotation : parameterAnnotations[i]) {
                            if (Param.class.equals(annotation.annotationType())) {
                                key = ((Param) annotation).value();
                            }
                        }

                        // 如果没有注解，直接取参数名称
                        if (key == null) {
                            key = parameterNames[i];
                        }

                        if (key != null) {
                            params[i] = jsonObject.get(key);
                        }
                    }
                }
            }

            // 执行方法
            try {
                long beginTime = System.currentTimeMillis();
                log.info("===>{} JOB BEGIN TIME: {} <===", shardingContext.getJobName(), DateTime.now().toString());
                method.invoke(bean, params);
                log.info("===>{}, JOB END TIME: {},TOTAL CAST: {} <===", shardingContext.getJobName(),
                        DateTime.now().toString(), System.currentTimeMillis() - beginTime);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("===>{}, JOB ERROR: {} <===", shardingContext.getJobName(), e.getMessage());
            }
        }
    }
}

