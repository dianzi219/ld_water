package com.fastbee.iot.ruleEngine;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.fastbee.common.utils.spring.SpringUtils;
import com.fastbee.iot.domain.MultipleDataSource;
import com.yomahub.liteflow.exception.NullParamException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;

import java.sql.*;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class MsgContext {

    private static Logger logger = LoggerFactory.getLogger("script");

    /**
     * 消息主题
     */
    private String topic;

    /**
     * 消息内容
     */
    private String payload;
    /**
     * 设备编号
     */
    private String serialNumber;
    /**
     * 产品id
     */
    private Long productId;
    /**
     * 协议编码
     */
    private String protocolCode;

    private ConcurrentHashMap<String, Object> dataMap = new ConcurrentHashMap<>();

    private <T> void putDataMap(String key, T t) {
        if (this.dataMap == null) {
            this.dataMap = new ConcurrentHashMap<>();
        }
        if (ObjectUtil.isNull(t)) {
            throw new NullParamException("data can't accept null param");
        } else {
            this.dataMap.put(key, t);
        }
    }

    public boolean hasData(String key) {
        if (this.dataMap == null) {
            return false;
        }
        return this.dataMap.containsKey(key);
    }

    public <T> T getData(String key) {
        if (this.dataMap == null) {
            return null;
        }
        return (T) this.dataMap.get(key);
    }

    public <T> void setData(String key, T t) {
        this.putDataMap(key, t);
    }

    //"{ topic:${topic}, payload:${payload} }";
    // 自定义占位符，可在脚本中使用msgContext.setData("test":1);
    // 然后通过${test}调用，可在输出侧http body和 sql语句中传入该值
    public String placeholders(String str) {
        if (getData("topic") == null) {
            setData("topic", getTopic());
        }
        if (getData("payload") == null) {
            setData("payload", getPayload());
        }
        if (getData("serialNumber") == null) {
            setData("serialNumber", getSerialNumber());
        }
        if (getData("productId") == null) {
            setData("productId", getProductId());
        }
        if (getData("protocolCode") == null) {
            setData("protocolCode", getProtocolCode());
        }
        StringSubstitutor substitutor = new StringSubstitutor(this.dataMap);
        return substitutor.replace(str);
    }

    public void databaseStorage(Long id) {
        DataSourceService dataSourceService = SpringUtils.getBean(DataSourceService.class);
        MultipleDataSource multipleDataSource = dataSourceService.getDataSource(id);
        DruidDataSource dataSource = dataSourceService.createDataSource(multipleDataSource);
        DruidPooledConnection connection = null;
        Statement statement = null;
        String sql = placeholders(multipleDataSource.getSql());
        log.info("执行sql：{}", sql);
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            log.error("sql执行失败---------，原因：{ }", e);
        }
        try {
            connection.close();
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            log.error("关闭数据库连接失败，原因：{ }", e);
        }
    }
}
