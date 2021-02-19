package com.yd.druid;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 多数据源，切面处理类
 *
 * @author: 叶小东
 * @date: 2021/2/18 19:51
 */
@Order(-1)
@Slf4j
public abstract class DynamicDataSourceAOP {

    private static final AtomicInteger counter = new AtomicInteger(0);

    /**
     * 切换主库
     */
    @Pointcut("@annotation(com.yd.druid.annotation.Master) " +
            "|| @annotation(org.springframework.transaction.annotation.Transactional)")
    public abstract void masterPointCut();

    /**
     * 主库切换方法
     *
     * @param point
     * @return
     * @throws Throwable
     */
    @Around("masterPointCut()")
    public Object masterAround(ProceedingJoinPoint point) throws Throwable {
        String dataSource = DynamicDataSource.getDataSource();
        log.debug("now datasource is " + dataSource);
        if (!DynamicDataSourceConfig.getMaster().equals(dataSource)){
            DynamicDataSource.setDataSource(DynamicDataSourceConfig.getMaster());
            log.debug("set datasource is " + DynamicDataSourceConfig.getMaster());
        }

        return proceed(point);
    }

    /**
     * 切换从库
     */
    @Pointcut("@annotation(com.yd.druid.annotation.Slave)")
    public abstract void slavePointCut();

    /**
     * 从库切换方法
     *
     * @param point
     * @return
     * @throws Throwable
     */
    @Around("slavePointCut()")
    public Object slaveAround(ProceedingJoinPoint point) throws Throwable {
        String dataSource = DynamicDataSource.getDataSource();
        log.debug("now datasource is " + dataSource);
        if (DynamicDataSourceConfig.getSlaves() == null || DynamicDataSourceConfig.getSlaves().length == 0){
            log.debug("slaves is empty, use master");
            if (!DynamicDataSourceConfig.getMaster().equals(dataSource)){
                DynamicDataSource.setDataSource(DynamicDataSourceConfig.getMaster());
                log.debug("set datasource is " + DynamicDataSourceConfig.getMaster());
            }
        }
        else {
            int index = counter.getAndIncrement() % DynamicDataSourceConfig.getSlaves().length;
            log.debug("number of slaves: " + DynamicDataSourceConfig.getSlaves().length);
            DynamicDataSource.setDataSource(DynamicDataSourceConfig.getSlaves()[index]);
            log.debug("set datasource is " + DynamicDataSourceConfig.getSlaves()[index]);
        }

        return proceed(point);
    }

    /**
     * 处理拦截方法
     *
     * @param point
     * @return
     * @throws Throwable
     */
    private Object proceed(ProceedingJoinPoint point) throws Throwable {
        try {
            return point.proceed();
        } finally {
            DynamicDataSource.clearDataSource();
            log.debug("clean datasource");
        }
    }
}