package com.fastbee.iot.service;

import com.fastbee.iot.domain.MqttClient;
import com.fastbee.iot.model.MqttClientConfig;

/**
 * mqtt桥接配置表Service接口
 *
 * @author gx_ma
 * @date 2024-06-03
 */
public interface IMqttClientService
{
    public void deleteMqttClientByKey(String key);
    /**
     * 构建mqtt客户端配置
     * @param client
     * @return
     */
    public MqttClientConfig buildmqttclientconfig(MqttClient client);
}
