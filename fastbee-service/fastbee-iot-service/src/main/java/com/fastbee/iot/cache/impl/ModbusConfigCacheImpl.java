package com.fastbee.iot.cache.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.core.device.DeviceAndProtocol;
import com.fastbee.common.core.mq.MQSendMessageBo;
import com.fastbee.common.core.redis.RedisCache;
import com.fastbee.common.core.redis.RedisKeyBuilder;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.iot.cache.IModbusConfigCache;
import com.fastbee.iot.cache.ITSLCache;
import com.fastbee.iot.domain.ModbusConfig;
import com.fastbee.iot.model.ThingsModels.ThingsModelValueItem;
import com.fastbee.iot.service.IDeviceService;
import com.fastbee.iot.service.IModbusConfigService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author gsb
 * @date 2024/6/13 9:45
 */
@Service
public class ModbusConfigCacheImpl implements IModbusConfigCache {

    @Resource
    private IModbusConfigService modbusConfigService;
    @Resource
    private RedisCache redisCache;

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
    @Override
    public Map<Integer, List<ModbusConfig>> getModbusConfigCacheByProductId(Long productId){
        Map<Integer, List<ModbusConfig>> resultMap = new HashMap<>();
        String modbusKey = RedisKeyBuilder.buildModbusKey(productId);
        Map<String,String> map = redisCache.hashEntity(modbusKey);
        if (!CollectionUtils.isEmpty(map)){
            for (Map.Entry<String, String> entry : map.entrySet()) {
                List<ModbusConfig> modbusConfigList = JSON.parseArray(entry.getValue(), ModbusConfig.class);
                resultMap.put(Integer.parseInt(entry.getKey()), modbusConfigList);
            }
            return resultMap;
        }
       return setModbusConfigCacheByProductId(productId);
    }

    /**
     * 缓存modbus参数-返回Map
     * @param productId
     *  <p>
     *      返回key-value
     *      key: 物模型标识符
     *      value：ModbusConfig
     *  </p>
     */
    @Override
    public Map<Integer, List<ModbusConfig>> setModbusConfigCacheByProductId(Long productId){
        Map<String,String> resultMap = new HashMap<>();
        ModbusConfig modbusConfig = new ModbusConfig();
        modbusConfig.setProductId(productId);
        List<ModbusConfig> modbusConfigList = modbusConfigService.selectShortListByProductId(modbusConfig);
        Map<Integer, List<ModbusConfig>> listMap = modbusConfigList.stream().collect(Collectors.groupingBy(ModbusConfig::getAddress));
        for (Map.Entry<Integer, List<ModbusConfig>> entry : listMap.entrySet()) {
            List<ModbusConfig> value = entry.getValue();
            String valueStr = JSONObject.toJSONString(value);
            resultMap.put(entry.getKey()+"", valueStr);
        }
        String modbusKey = RedisKeyBuilder.buildModbusKey(productId);
        redisCache.hashPutAll(modbusKey, resultMap);
        return listMap;
    }
    /**
     * 获取单个modbus参数缓存值
     * @param productId 产品id
     * @param address 标识符
     * @return ModbusConfig
     */
    @Override
    public List<ModbusConfig> getSingleModbusConfig(Long productId, String address){
        String modbusKey = RedisKeyBuilder.buildModbusKey(productId);
        String cacheMapValue = redisCache.getCacheMapValue(modbusKey, address);
        if (!StringUtils.isEmpty(cacheMapValue)){
            return JSON.parseArray(cacheMapValue, ModbusConfig.class);
        }
        ModbusConfig modbusConfig = new ModbusConfig();
        modbusConfig.setProductId(productId);
        modbusConfig.setAddress(Integer.parseInt(address));
        List<ModbusConfig> modbusConfigList = modbusConfigService.selectShortListByProductId(modbusConfig);
        if (!CollectionUtils.isEmpty(modbusConfigList)){
            setModbusConfigCacheByProductId(productId);
        }
        return modbusConfigList;
    }

}
