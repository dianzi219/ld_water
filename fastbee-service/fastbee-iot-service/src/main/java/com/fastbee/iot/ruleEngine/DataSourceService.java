package com.fastbee.iot.ruleEngine;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson2.JSON;
import com.fastbee.iot.domain.Bridge;
import com.fastbee.iot.domain.MultipleDataSource;
import com.fastbee.iot.service.IBridgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

/**
 * 动态创建数据库连接
 *
 * @author gx_ma
 * @date 2024-06-14
 */
@Service
@Slf4j
@Configuration
public class DataSourceService {

    @Resource
    private IBridgeService bridgeService;

    public DruidDataSource createDataSource(MultipleDataSource multipleDataSource) {
        String databaseSource = multipleDataSource.getDatabaseSource();
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(buildUrl(multipleDataSource));
        dataSource.setUsername(multipleDataSource.getUserName());
        dataSource.setPassword(multipleDataSource.getPassword());
        dataSource.setDriverClassName(getDriverClassName(databaseSource));
        dataSource.setInitialSize(5); // 初始连接数
        dataSource.setMaxWait(60000); // 最大等待超时时间
        dataSource.setMaxActive(20); // 最大连接数
        dataSource.setMinIdle(10); // 最小连接数
        dataSource.setTimeBetweenEvictionRunsMillis(60000); // 间隔时间，检测需要关闭的空闲连接
        dataSource.setMinEvictableIdleTimeMillis(300000); // 空闲连接存活时间
        dataSource.setValidationQuery("SELECT 1"); // 验证查询连接是否有效
        return dataSource;
    }
    private String getDriverClassName(String databaseSource) {
        // 根据数据库类型返回对应的驱动类名
        switch (databaseSource) {
            case "MySQL":
                return "com.mysql.cj.jdbc.Driver";
            case "SQLServer":
                return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            case "Oracle":
                return "oracle.jdbc.driver.OracleDriver";
            case "PostgreSQL":
                return "org.postgresql.Driver";
            case "H2":
                return "org.h2.Driver";
            default:
                throw new IllegalArgumentException("Unsupported database type: " + databaseSource);
        }
    }

    private String buildUrl(MultipleDataSource config) {
        String url = null;
        // 根据配置构建JDBC URL
        if ("MySQL".equals(config.getDatabaseSource())){
            url = "jdbc:" + config.getDatabaseSource().toLowerCase() + "://" + config.getHost() + "/" + config.getDataBaseName() + "?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8";
        } else if ("SQLServer".equals(config.getDatabaseSource())) {
            url = "jdbc:" + config.getDatabaseSource().toLowerCase() + "://" + config.getHost() + ";DatabaseName=" + config.getDataBaseName();
        }else if ("Oracle".equals(config.getDatabaseSource())) {
            url = "jdbc:" + config.getDatabaseSource().toLowerCase() + ":thin:@" + config.getHost() + "/" + config.getDataBaseName();
        } else if ("PostgreSQL".equals(config.getDatabaseSource())) {
            url = "jdbc:" + config.getDatabaseSource().toLowerCase() + "://" + config.getHost() + "/" + config.getDataBaseName();
        } else if ("H2".equals(config.getDatabaseSource())) {
            url = "jdbc:" + config.getDatabaseSource().toLowerCase() + "://" + config.getHost() + "/" + config.getDataBaseName();
        } else {
            throw new IllegalArgumentException("Unsupported database type: " + config.getDatabaseSource());
        }
        return url;
    }

    public MultipleDataSource getDataSource(Long id) {
        Bridge bridge = bridgeService.queryByIdWithCache(id);
        if (bridge == null || bridge.getType() != 5) {
            log.error("数据源不存在或者当前配置类型不是数据库存储，id={}", id);
            throw new IllegalArgumentException("数据源不存在或者当前配置类型不是数据库存储" + id);
        }
        return JSON.parseObject(bridge.getConfigJson(), MultipleDataSource.class);
    }

}
