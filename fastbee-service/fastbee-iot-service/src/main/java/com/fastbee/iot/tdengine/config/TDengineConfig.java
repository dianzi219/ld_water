package com.fastbee.iot.tdengine.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.fastbee.common.utils.StringUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 类名: TDengineConfig
 * 描述: TDengine配置类
 * 时间: 2022/5/13,0016 1:14
 * 开发人: wxy
 */
@Configuration
@MapperScan(basePackages = {"com.fastbee.iot.tdengine.dao"}, sqlSessionTemplateRef = "tdengineSqlSessionTemplate")
@ConditionalOnProperty(name = "spring.datasource.taos.enabled", havingValue = "true")
public class TDengineConfig {

    @Value("${spring.datasource.taos.url}")
    private String url;

    @Value("${spring.datasource.taos.username}")
    private String username;

    @Value("${spring.datasource.taos.password}")
    private String password;

    @Value("${spring.datasource.taos.dbName}")
    private String dbName;

    @Autowired
    private DataSource source;

    @Bean(name = "tDengineSqlSessionFactory")
    public SqlSessionFactory tDengineSqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setMapperLocations(resolveMapperLocations(StringUtils.split("classpath:mapper/tdengine/*Mapper.xml", ",")));
        sqlSessionFactoryBean.setDataSource(createDataSource());
        return sqlSessionFactoryBean.getObject();
    }

    public DataSource createDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName("com.taosdata.jdbc.TSDBDriver");
        dataSource.setInitialSize(5); // 初始连接数
        dataSource.setMaxWait(60000); // 最大等待超时时间
        dataSource.setMaxActive(20); // 最大连接数
        dataSource.setMinIdle(10); // 最小连接数
        dataSource.setTimeBetweenEvictionRunsMillis(60000); // 间隔时间，检测需要关闭的空闲连接
        dataSource.setMinEvictableIdleTimeMillis(300000); // 空闲连接存活时间
        dataSource.setValidationQuery("SELECT 1"); // 验证查询连接是否有效
        return dataSource;
    }

    @Bean(name = "tdengineSqlSessionTemplate")
    public SqlSessionTemplate tdengineSqlSessionTemplate(@Qualifier("tDengineSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }
    public Resource[] resolveMapperLocations(String[] mapperLocations)
    {
        ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
        List<Resource> resources = new ArrayList<Resource>();
        if (mapperLocations != null)
        {
            for (String mapperLocation : mapperLocations)
            {
                try
                {
                    Resource[] mappers = resourceResolver.getResources(mapperLocation);
                    resources.addAll(Arrays.asList(mappers));
                }
                catch (IOException e)
                {
                    // ignore
                }
            }
        }
        return resources.toArray(new Resource[resources.size()]);
    }
}
