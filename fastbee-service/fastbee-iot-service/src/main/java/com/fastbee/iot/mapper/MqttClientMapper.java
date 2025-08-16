package com.fastbee.iot.mapper;

import java.util.List;
import com.fastbee.iot.domain.MqttClient;

/**
 * mqtt桥接配置表Mapper接口
 * 
 * @author gx_ma
 * @date 2024-06-03
 */
public interface MqttClientMapper 
{
    /**
     * 查询mqtt桥接配置表
     * 
     * @param id mqtt桥接配置表主键
     * @return mqtt桥接配置表
     */
    public MqttClient selectMqttClientById(Long id);

    /**
     * 查询mqtt桥接配置表列表
     * 
     * @param mqttClient mqtt桥接配置表
     * @return mqtt桥接配置表集合
     */
    public List<MqttClient> selectMqttClientList(MqttClient mqttClient);

    /**
     * 新增mqtt桥接配置表
     * 
     * @param mqttClient mqtt桥接配置表
     * @return 结果
     */
    public int insertMqttClient(MqttClient mqttClient);

    /**
     * 修改mqtt桥接配置表
     * 
     * @param mqttClient mqtt桥接配置表
     * @return 结果
     */
    public int updateMqttClient(MqttClient mqttClient);

    /**
     * 删除mqtt桥接配置表
     * 
     * @param id mqtt桥接配置表主键
     * @return 结果
     */
    public int deleteMqttClientById(Long id);

    /**
     * 批量删除mqtt桥接配置表
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMqttClientByIds(Long[] ids);
}
