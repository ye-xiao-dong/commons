package com.yd.druid;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置多数据源
 *
 * @author: 叶小东
 * @date: 2021/2/18 19:47
 */
public class DynamicDataSourceConfig {

    private static String master;

    private static String[] slaves;

    @Bean
    public DynamicDataSource dataSource(ApplicationContext applicationContext) {
        Map<Object, Object> targetDataSources = new HashMap<>();
        Map<String, DataSource> beansOfType = applicationContext.getBeansOfType(DataSource.class);
        DataSource masterDataSource = applicationContext.getBean(DataSource.class);
        initDynamicDataSourceNames(beansOfType, masterDataSource);
        targetDataSources.putAll(beansOfType);
        return new DynamicDataSource(masterDataSource, targetDataSources);
    }

    private void initDynamicDataSourceNames(Map<String, DataSource> beansOfType, DataSource masterDataSource){
        String masterName = null;
        for (Map.Entry<String, DataSource> entry : beansOfType.entrySet()){
            if (entry.getValue() == masterDataSource){
                masterName = entry.getKey();
                break;
            }
        }

        List<String> slaveNames = new ArrayList<>(beansOfType.keySet());
        if (masterName == null){
            masterName = slaveNames.remove(0);
        }
        else {
            slaveNames.remove(masterName);
        }

        master = masterName;
        slaves = slaveNames.toArray(new String[]{});
    }

    public static String getMaster() {
        return master;
    }

    public static String[] getSlaves() {
        return slaves;
    }
}