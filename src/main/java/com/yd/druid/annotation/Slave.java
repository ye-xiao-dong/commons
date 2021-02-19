package com.yd.druid.annotation;

import java.lang.annotation.*;

/**
 * @author: 叶小东
 * @date: 2021/2/18 20:01
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface Slave {
}