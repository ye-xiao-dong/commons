package com.yd.druid.annotation;

import com.yd.druid.DynamicDataSourceConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author: 叶小东
 * @date: 2020/12/8 10:45
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(value = {DynamicDataSourceConfig.class})
@Documented
public @interface EnableDruidDynamicDataSource {
}
