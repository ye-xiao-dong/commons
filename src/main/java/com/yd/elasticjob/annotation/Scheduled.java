package com.yd.elasticjob.annotation;

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
    boolean failover() default true;

    //监控作业执行时状态
    boolean monitorExecution() default true;

    // 定时任务分片策略
    Class<?> jobShardingStrategyClass() default Void.class;
}
