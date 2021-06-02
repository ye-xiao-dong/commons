package com.yd.elasticjob;

import cn.hutool.core.util.ArrayUtil;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.yd.elasticjob.annotation.ElasticJobComponent;
import com.yd.elasticjob.annotation.Scheduled;
import com.yd.elasticjob.annotation.Schedules;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author： 叶小东
 * @date： 2021/4/25 10:34
 */
public class ScheduledConfig {

    @Autowired
    private ZookeeperRegistryCenter registryCenter;

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${spring.application.name}")
    private String namespace;

    /**
     * 初始化配置
     *
     */
    @PostConstruct
    public synchronized void initElasticJob() {

        // 先获得上下文中所有被ElasticJob注解的类
        String[] beanNamesForAnnotation = applicationContext.getBeanNamesForAnnotation(ElasticJobComponent.class);

        // 找到这些类中所有的被Scheduled注解修饰的方法，并返回对应的SimpleJob对象
        Map<String, Map<SimpleJob, Scheduled>> simpleJobMap = getSimpleJobMap(beanNamesForAnnotation);
        for (Map.Entry<String, Map<SimpleJob, Scheduled>> stringMapEntry : simpleJobMap.entrySet()) {
            for (Map.Entry<SimpleJob, Scheduled> simpleJobScheduledEntry : stringMapEntry.getValue().entrySet()){

                // 代理类转换成原本的类
                SimpleJob simpleJob = simpleJobScheduledEntry.getKey();
                if (AopUtils.isAopProxy(simpleJob)) {
                    try {
                        simpleJob = (SimpleJob) ((Advised) simpleJob).getTargetSource().getTarget();
                    } catch (Exception e) {
                        throw new RuntimeException("==>代理类转换异常!", e);
                    }
                }

                // 获取注解的属性
                Scheduled scheduled = simpleJobScheduledEntry.getValue();
                String jobName = StringUtils.isBlank(scheduled.jobName()) ? stringMapEntry.getKey()
                        : scheduled.jobName();
                jobName = namespace + "_" + jobName;

                //SimpleJob任务配置
                SimpleJobConfiguration simpleJobConfiguration = new SimpleJobConfiguration(JobCoreConfiguration
                        .newBuilder(jobName, scheduled.cron(), scheduled.shardingTotalCount())
                        .shardingItemParameters(scheduled.shardingItemParameters())
                        .description(scheduled.description())
                        .failover(scheduled.failover())
                        .jobParameter(scheduled.jobParameter())
                        .build(), simpleJob.getClass().getCanonicalName());
                LiteJobConfiguration.Builder builder = LiteJobConfiguration
                        .newBuilder(simpleJobConfiguration)
                        .disabled(scheduled.disabled())
                        .overwrite(scheduled.overwrite())
                        .monitorExecution(scheduled.monitorExecution());
                if (!Void.class.equals(scheduled.jobShardingStrategyClass())){
                    builder.jobShardingStrategyClass(scheduled.jobShardingStrategyClass().getName());
                }

                LiteJobConfiguration liteJobConfiguration = builder.build();

                //配置数据源
                String dataSourceRef = scheduled.dataSource();
                SpringJobScheduler jobScheduler = null;
                if (StringUtils.isNotBlank(dataSourceRef)) {
                    if (!applicationContext.containsBean(dataSourceRef)) {
                        throw new RuntimeException("not exist datasource [" + dataSourceRef + "] !");
                    }

                    DataSource dataSource = (DataSource) applicationContext.getBean(dataSourceRef);
                    JobEventRdbConfiguration jobEventRdbConfiguration = new JobEventRdbConfiguration(dataSource);
                    jobScheduler = new SpringJobScheduler(simpleJob, registryCenter,liteJobConfiguration,
                            jobEventRdbConfiguration);
                }
                else {
                    jobScheduler = new SpringJobScheduler(simpleJob, registryCenter, liteJobConfiguration);
                }

                jobScheduler.init();
            }
        }
    }

    /**
     * 获取带有Scheduled注解的方法，并进行job注册
     *
     * @param beanNames
     * @return
     */
    private Map<String, Map<SimpleJob, Scheduled>> getSimpleJobMap(String[] beanNames) {
        Map<String, Map<SimpleJob, Scheduled>> result = new HashMap();
        if (beanNames != null) {
            String env = applicationContext.getEnvironment().getActiveProfiles()[0];
            for (String beanName : beanNames) {

                // 先获取这个类里面有Scheduled注解的方法
                Object bean = applicationContext.getBean(beanName);
                int suffix = 0;
                Map<Method, Set<Scheduled>> scheduleMethodMap = getScheduleMethod(bean);
                if (!scheduleMethodMap.isEmpty()) {

                    // 对于每一个定时方法创建一个Job
                    for (Method method : scheduleMethodMap.keySet()) {
                        for (Scheduled scheduled : scheduleMethodMap.get(method)) {
                            if (ArrayUtil.isNotEmpty(scheduled.envs()) && !ArrayUtil.contains(scheduled.envs(), env)){
                                continue;
                            }

                            String name = bean.getClass().getName() + "_" + suffix;
                            registerAJob(name, bean, method, scheduled);
                            Job job = (Job) applicationContext.getBean(name);
                            String methodName = bean.getClass().getName() + "_" + method.getName() + "_" + suffix;
                            result.putIfAbsent(methodName, new HashMap());
                            result.get(methodName).put(job, scheduled);
                            suffix++;
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * 获取具有Scheduled注解的所有方法
     *
     * @param bean
     * @return
     */
    private Map<Method, Set<Scheduled>> getScheduleMethod(Object bean) {
        Map<Method, Set<Scheduled>> result = new HashMap();
        if (bean instanceof AopInfrastructureBean) {
            return result;
        }

        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        result.putAll(MethodIntrospector.selectMethods(targetClass, (Method method) -> {
            Set<Scheduled> scheduledMethods = AnnotatedElementUtils.getMergedRepeatableAnnotations(method,
                    Scheduled.class, Schedules.class);
            return !scheduledMethods.isEmpty() ? scheduledMethods : null;
        }));

        return result;
    }

    /**
     * 注册一个job
     *
     * @param beanName
     * @param bean
     * @param method
     * @param scheduled
     */
    private synchronized void registerAJob (String beanName, Object bean, Method method, Scheduled scheduled){
        ConfigurableApplicationContext context = (ConfigurableApplicationContext) this.applicationContext;
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(Job.class);
        beanDefinitionBuilder.addPropertyValue("method", method);
        beanDefinitionBuilder.addPropertyValue("bean", bean);
        beanDefinitionBuilder.addPropertyValue("scheduled", scheduled);
        beanFactory.registerBeanDefinition(beanName, beanDefinitionBuilder.getBeanDefinition());
    }
}
