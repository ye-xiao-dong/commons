package com.yd.elasticjob.annotation;

import cn.hutool.core.date.DateField;

import java.lang.annotation.*;

/**
 * @author： 叶小东
 * @date： 2021/4/25 10:29
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Schedules.class)
public @interface Scheduled{

    //cron表达式
    String cron();

    //job的名字，用于区分同一个项目中的多个job
    String jobName() default "";

    //job的描述信息
    String description() default "";

    //job的分片数量，默认1
    int shardingTotalCount() default 1;

    //job分片序列号和个性化参数对照表。
    //分片序列号和参数用等号分隔, 多个键值对用逗号分隔。
    //分片序列号从0开始, 不可大于或等于作业分片总数。
    String shardingItemParameters() default "";

    //job的自定义参数
    String jobParameter() default "";

    //时间追踪的数据源，参数为DataSource的name,执行频率高的任务不建议使用
    String dataSource() default "";

    //是否禁用
    boolean disabled() default false;

    //本地配置是否可覆盖注册中心配置
    boolean overwrite() default true;

    //是否开启失效转移，仅monitorExecution开启时才起作用
    boolean failover() default false;

    //监控作业执行时状态
    boolean monitorExecution() default true;

    // 定时任务分片策略
    Class<?> jobShardingStrategyClass() default Void.class;

    // 指定环境，指定该任务只有在指定环境生效，默认不限制
    String[] envs() default {};

    // 自动进行日志记录，默认开启
    boolean autoLog() default true;

    // 进行执行时间二次判断
    boolean recheckTime() default true;

    // 二次判断允许延迟的时间误差范围，正数，仅在recheckTime为true的时候有效
    int allowRange() default 1;

    // 二次判断允许延迟的时间误差范围单位，仅在recheckTime为true的时候有效
    DateField timeUnit() default DateField.SECOND;
}
