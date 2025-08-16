package com.fastbee.framework.datasource;

import java.util.Map;
import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 动态数据源
 *
 * @author ruoyi
 */
@Slf4j
public class DynamicDataSource extends AbstractRoutingDataSource
{
    public DynamicDataSource(DataSource defaultTargetDataSource, Map<Object, Object> targetDataSources)
    {
        super.setDefaultTargetDataSource(defaultTargetDataSource);
        super.setTargetDataSources(targetDataSources);
        super.afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey()
    {
        //log.debug("数据源：" + DynamicDataSourceContextHolder.getDataSourceType());
        return DynamicDataSourceContextHolder.getDataSourceType();
    }
}
