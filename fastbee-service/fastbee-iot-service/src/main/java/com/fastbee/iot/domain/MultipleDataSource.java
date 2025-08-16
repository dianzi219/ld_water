package com.fastbee.iot.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fastbee.common.annotation.Excel;
import lombok.Data;
import com.fastbee.common.core.domain.BaseEntity;

/**
 * 数据源配置对象 multiple_data_source
 *
 * @author gx_ma
 * @date 2024-06-17
 */
@Data
public class MultipleDataSource extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 数据源 */
    @Excel(name = "数据源")
    private String databaseSource;

    /** sql语句 */
    @Excel(name = "sql语句")
    private String sql;

    /** 类型 */
    @Excel(name = "类型")
    private String type;

    /** 数据库名称 */
    @Excel(name = "数据库名称")
    private String dataBaseName;

    /** 连接地址 */
    @Excel(name = "连接地址")
    private String host;

    /** 用户名 */
    @Excel(name = "用户名")
    private String userName;

    /** 密码 */
    @Excel(name = "密码")
    private String password;

}
