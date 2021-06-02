package com.yd.elasticjob;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.yd.elasticjob.annotation.Param;
import com.yd.elasticjob.annotation.Scheduled;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Date;

/**
 * @author： 叶小东
 * @date： 2021/4/25 10:31
 */
@Slf4j
@Data
public class Job implements SimpleJob {

    private Method method;

    private Object bean;

    private Scheduled scheduled;

    @Override
    public void execute(ShardingContext shardingContext) {
        Date now  = new Date();
        if (method == null || bean == null || scheduled == null){
            return;
        }

        // 判断执行时间二次校验
        if (scheduled.recheckTime() && !MyUtil.isRunTime(now, scheduled.cron())){
            return;
        }

        Object[] params = null;
        if (method.getParameterCount() > 0){
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(shardingContext));
            Parameter[] parameters = method.getParameters();
            params = new Object[parameters.length];
            for(int i = 0, j = parameters.length; i < j; i++) {
                String typeName = parameters[i].getType().getName();
                if (typeName.contains(".")){
                    typeName = typeName.substring(typeName.lastIndexOf(".") + 1);
                }

                if ("ShardingContext".equals(typeName)){
                    params[i] = shardingContext;
                }
                else {

                    // 先找到这个参数上的注解的名字
                    String key = null;
                    for (Annotation annotation : parameters[i].getAnnotations()) {
                        if (Param.class.equals(annotation.annotationType())) {
                            key = ((Param) annotation).value();
                            break;
                        }
                    }

                    // 如果没有注解，直接取参数名称
                    if (key == null) {
                        key = parameters[i].getName();
                    }

                    if (key != null) {
                        params[i] = jsonObject.get(key);
                    }
                }
            }
        }

        // 执行方法
        if (scheduled.autoLog()){
            try {
                long beginTime = System.currentTimeMillis();
                log.info("===>{}, JOB BEGIN TIME: {} <===", shardingContext.getJobName(), DateTime.now().toString());
                method.invoke(bean, params);
                log.info("===>{}, JOB END TIME: {},TOTAL CAST: {} <===", shardingContext.getJobName(),
                        DateTime.now().toString(), System.currentTimeMillis() - beginTime);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("===>{}, JOB ERROR: {} <===", shardingContext.getJobName(), e.getMessage());
            }
        }
        else {
            try {
                method.invoke(bean, params);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

