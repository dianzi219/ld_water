package com.fastbee.iot.cache;

import com.fastbee.common.core.mq.MQSendMessageBo;
import com.fastbee.iot.domain.ModbusConfig;
import com.fastbee.iot.model.ThingsModels.ThingsModelValueItem;

import java.util.List;
import java.util.Map;

/**
 * 产品对应modbus配置参数缓存
 * @author gsb
 * @date 2024/6/13 9:44
 */
public interface IModbusConfigCache {


    /**
     * 获取modbus参数 --返回 Map<String, ModbusConfig>
     * @param productId
     * @return
     *  <p>
     *      返回key-value
     *      key: 物模型标识符
     *      value：ModbusConfig
     *  </p>
     */
    Map<Integer, List<ModbusConfig>> getModbusConfigCacheByProductId(Long productId);


    /**
     * 缓存modbus参数-返回Map
     * @param productId
     *  <p>
     *      返回key-value
     *      key: 物模型标识符
     *      value：ModbusConfig
     *  </p>
     */
    Map<Integer, List<ModbusConfig>> setModbusConfigCacheByProductId(Long productId);

    /**
     * 获取单个modbus参数缓存值
     * @param productId 产品id
     * @param identify 标识符
     * @return ModbusConfig
     */
    public List<ModbusConfig> getSingleModbusConfig(Long productId, String identify);
}
