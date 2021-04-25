package com.yd.elasticjob.annotation;

import java.lang.annotation.*;

/**
 * @author： 叶小东
 * @date： 2021/4/25 10:29
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Schedules {

    Scheduled[] value();

}
