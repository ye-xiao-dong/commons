package com.yd.elasticjob.annotation;

import com.yd.elasticjob.JobRegistryCenterConfig;
import com.yd.elasticjob.ScheduledConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author： 叶小东
 * @date： 2021/4/25 10:26
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(value = {JobRegistryCenterConfig.class, ScheduledConfig.class})
@Documented
public @interface EnableElasticJob {
}
