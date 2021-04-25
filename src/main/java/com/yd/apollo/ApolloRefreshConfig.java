package com.yd.apollo;

import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author： 叶小东
 * @date： 2021/4/25 11:56
 */
@Component
public class ApolloRefreshConfig implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Autowired
    private RefreshScope refreshScope;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @ApolloConfigChangeListener
    public void onChange(ConfigChangeEvent configChangeEvent){
        applicationContext.publishEvent(new EnvironmentChangeEvent(configChangeEvent.changedKeys()));
        refreshScope.refreshAll();
    }
}
