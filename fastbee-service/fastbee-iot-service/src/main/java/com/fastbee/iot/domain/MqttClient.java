package com.fastbee.iot.domain;

import lombok.Data;
import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;

/**
 * mqtt桥接配置表对象 mqtt_client
 *
 * @author gx_ma
 * @date 2024-06-03
 */
@Data
public class MqttClient
{
    /** 连接器名称 */
    @Excel(name = "连接器名称")
    private String name;

    /** MQTT服务地址（broker.emqx.io:1883） */
    @Excel(name = "MQTT服务地址", readConverterExp = "b=roker.emqx.io:1883")
    private String hostUrl;

    /** 用户名 */
    @Excel(name = "用户名")
    private String username;

    /** 密码 */
    @Excel(name = "密码")
    private String password;

    /** 客户端ID */
    @Excel(name = "客户端ID")
    private String clientId;
}
