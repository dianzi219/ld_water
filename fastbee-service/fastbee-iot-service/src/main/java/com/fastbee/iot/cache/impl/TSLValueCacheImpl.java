package com.fastbee.iot.cache.impl;

import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.core.redis.RedisCache;
import com.fastbee.common.core.redis.RedisKeyBuilder;
import com.fastbee.common.enums.DataEnum;
import com.fastbee.iot.cache.ITSLCache;
import com.fastbee.iot.cache.ITSLValueCache;
import com.fastbee.iot.model.ThingsModels.ThingsModelValueItem;
import com.fastbee.iot.model.ThingsModels.ValueItem;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 物模型值缓存
 *
 * @author gsb
 * @date 2024/3/22 16:37
 */
@Service
public class TSLValueCacheImpl implements ITSLValueCache {

    @Resource
    private RedisCache redisCache;
    @Resource
    private ITSLCache itslCache;


    /**
     * 获取Redis缓存的设备物模型值
     *
     * @param productId    产品ID
     * @param deviceNumber 设备编号
     * @return
     */
    @Override
    public List<ValueItem> getCacheDeviceStatus(Long productId, String deviceNumber) {
        String key = RedisKeyBuilder.buildTSLVCacheKey(productId, deviceNumber);
        Map<String, String> map = redisCache.hashEntity(key);
        List<ValueItem> valueList = new ArrayList<>();
        if (map == null || map.size() == 0) {
            // 缓存设备状态（物模型值）到redis
            valueList = addCacheDeviceStatus(productId, deviceNumber);
        } else {
            // 获取redis缓存的物模型值
            valueList = map.values().stream().map(s -> JSONObject.parseObject(s, ValueItem.class))
                    .collect(Collectors.toList());
        }
        return valueList;
    }


    /**
     * 缓存设备物模型值到redis
     *
     * @return
     */
    @Override
    public List<ValueItem> addCacheDeviceStatus(Long productId, String serialNumber) {
        List<ValueItem> resultList = new ArrayList<>();
        // 获取物模型值
        List<ThingsModelValueItem> valueList = itslCache.getThingsModelList(productId);
        // redis存储设备默认状态 键：产品ID_设备编号
        String key = RedisKeyBuilder.buildTSLVCacheKey(productId, serialNumber);
        Map<String, String> maps = new HashMap<>();
        for (ThingsModelValueItem item : valueList) {
            ValueItem valueItem = new ValueItem();
            valueItem.setId(item.getId());
            valueItem.setValue("");
            valueItem.setShadow("");
            DataEnum dataType = DataEnum.convert(item.getDatatype().getType());
            if (dataType.equals(DataEnum.ARRAY) || dataType.equals(DataEnum.OBJECT)) {
                //数组和对象类型额外处理
                this.handleOtherType(maps, item, dataType, valueItem);
            } else {
                maps.put(item.getId(), JSONObject.toJSONString(valueItem));
            }
            resultList.add(valueItem);
        }
        redisCache.hashPutAll(key, maps);
        return resultList;
    }

    @Override
    public String getCacheIdentifierValue(Long productId, String serialNumber, String identifier) {
        String key = RedisKeyBuilder.buildTSLVCacheKey(productId, serialNumber);
        ValueItem valueItem = JSONObject.parseObject(redisCache.getCacheMapValue(key, identifier), ValueItem.class);
        return null != valueItem ? valueItem.getValue() : "";
    }

    @Override
    public ValueItem getCacheIdentifier(Long productId, String serialNumber, String identifier) {
        String key = RedisKeyBuilder.buildTSLVCacheKey(productId, serialNumber);
        return JSONObject.parseObject(redisCache.getCacheMapValue(key, identifier), ValueItem.class);
    }

    /**
     * 数组和对象类型额外处理
     * @param maps
     * @param item
     * @param dataType
     * @param valueItem
     */
    private void handleOtherType(Map<String, String> maps, ThingsModelValueItem item, DataEnum dataType, ValueItem valueItem) {
        List<ThingsModelValueItem> params = item.getDatatype().getParams();
        switch (dataType) {
            case ARRAY:
                // 数组元素赋值（英文逗号分割的字符串,包含简单类型数组和对象类型数组，数组元素ID格式：array_01_humidity）
                StringBuilder defaultValue = new StringBuilder(" ");
                if (item.getDatatype().getArrayType().equals("object")) {
                    for (int i = 0; i < item.getDatatype().getArrayCount(); i++) {
                        // 默认值需要保留为空格,便于解析字符串为数组
                        defaultValue.append(", ");
                    }
                    for (ThingsModelValueItem param : params) {
                        valueItem.setValue(defaultValue.toString());
                        valueItem.setShadow(defaultValue.toString());
                        valueItem.setName(param.getName());
                        valueItem.setId(param.getId());
                        maps.put(param.getId(), JSONObject.toJSONString(valueItem));
                    }
                } else {
                    for (int i = 0; i < item.getDatatype().getArrayCount(); i++) {
                        defaultValue.append(", ");
                    }
                    valueItem.setValue(defaultValue.toString());
                    valueItem.setShadow(defaultValue.toString());
                    valueItem.setName(item.getName());
                    valueItem.setId(item.getId());
                    maps.put(item.getId(), JSONObject.toJSONString(valueItem));
                }
                break;
            case OBJECT:
                for (ThingsModelValueItem param : params) {
                    valueItem.setName(param.getName());
                    valueItem.setId(param.getId());
                    maps.put(param.getId(), JSONObject.toJSONString(valueItem));
                }
                break;
        }
    }

}
