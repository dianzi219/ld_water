package com.fastbee.iot.cache;

import com.fastbee.iot.model.ThingsModels.ValueItem;

import java.util.List;

/**
 * @author gsb
 * @date 2024/3/22 16:37
 */
public interface ITSLValueCache {



    /**
     * 获取Redis缓存的设备物模型值
     *
     * @param productId    产品ID
     * @param deviceNumber 设备编号
     * @return
     */
    public List<ValueItem> getCacheDeviceStatus(Long productId, String deviceNumber);


    /**
     * 缓存设备物模型值到redis
     *
     * @return
     */
    public List<ValueItem> addCacheDeviceStatus(Long productId, String serialNumber);

    /**
     * 获取单个物模型的值
     * @param productId 产品id
     * @param: deviceNumber 设备编号
     * @param: identifier 物模型标识
     * @return java.lang.String
     */
    String getCacheIdentifierValue(Long productId, String serialNumber, String identifier);

    /**
     * 获取单个物模型的值
     * @param productId 产品id
     * @param: deviceNumber 设备编号
     * @param: identifier 物模型标识
     * @return java.lang.String
     */
    ValueItem getCacheIdentifier(Long productId, String serialNumber, String identifier);
}
