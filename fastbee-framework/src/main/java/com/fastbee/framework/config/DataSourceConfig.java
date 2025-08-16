package com.fastbee.framework.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAutoConfiguration;
import com.fastbee.framework.datasource.DynamicDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


@Configuration
@AutoConfigureBefore({DynamicDataSourceAutoConfiguration.class, SpringBootConfiguration.class})
public class DataSourceConfig {

    @Bean(name = "masterDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.dynamic.datasource.master")
    public DruidDataSource masterDataSource() {
        return new DruidDataSource();
    }

//    @Bean(name = "slaveDataSource")
//    @ConfigurationProperties(prefix = "spring.datasource.dynamic.datasource.slave")
//    public DruidDataSource slaveDataSource() {
//        return new DruidDataSource();
//    }

    @Bean
    @Primary
    public DynamicDataSource dataSource(@Qualifier("masterDataSource") DataSource master) throws SQLException, IOException {
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("master", master);
        //targetDataSources.put("slave", slave);
        return new DynamicDataSource(master,targetDataSources);
    }


    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

//    /**
//     * 将动态数据源设置为首选数据源
//     */
//    @Primary
//    @Bean
//    public DynamicDataSource dataSource(DynamicDataSourceProvider dynamicDataSourceProvider) {
////        DynamicRoutingDataSource dataSource = new DynamicRoutingDataSource();
////        dataSource.setPrimary(properties.getPrimary());
////        dataSource.setStrict(properties.getStrict());
////        dataSource.setStrategy(properties.getStrategy());
////        dataSource.setProvider(dynamicDataSourceProvider);
////        dataSource.setP6spy(properties.getP6spy());
////        dataSource.setSeata(properties.getSeata());
////        return dataSource;
//        Map<String, DataSource> targetDataSources = dynamicDataSourceProvider.loadDataSources();
//        Map<Object, Object> objectObjectMap = new HashMap<>(targetDataSources);
//        DataSource masterDataSource = targetDataSources.get(properties.getPrimary());
//        return new DynamicDataSource(masterDataSource,objectObjectMap);
//    }
}
