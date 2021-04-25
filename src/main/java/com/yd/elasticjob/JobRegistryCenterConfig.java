package com.yd.elasticjob;

import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

/**
 * @author： 叶小东
 * @date： 2021/4/25 10:33
 */
@Order(100)
@ConditionalOnExpression("'${zookeeper.serverlists}'.length() > 0")
@ConditionalOnProperty(value = "spring.application.name")
public class JobRegistryCenterConfig {

    /**
     * 使用框架默认zookeeper实现
     *
     * @param serverlists 服务器列表host:port，逗号分隔
     * @param namespace   zookeeper命名空间，使用应用名
     */
    @Bean(initMethod = "init")
    public ZookeeperRegistryCenter zookeeperRegistryCenter(@Value("${zookeeper.serverlists}") String serverlists,
                                                           @Value("${spring.application.name}") String namespace) {
        namespace = "elasticjob/" + namespace;
        return new ZookeeperRegistryCenter(new ZookeeperConfiguration(serverlists, namespace));
    }
}

