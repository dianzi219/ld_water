package com.fastbee.iot.cache.impl;


import com.fastbee.common.core.mq.ota.OtaUpgradeBo;
import com.fastbee.common.core.redis.RedisCache;
import com.fastbee.common.core.redis.RedisKeyBuilder;
import com.fastbee.iot.cache.IOtaTaskCache;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class OtaTaskCacheImpl implements IOtaTaskCache {


    @Resource
    private RedisCache redisCache;

    @Override
    public void setOtaCache(String serialNumber, Map<String, String> hMap, long timeout) {
        String cacheKey = RedisKeyBuilder.buildDeviceOtaKey(serialNumber);
        redisCache.hashPutAll(cacheKey, hMap);
        redisCache.expire(cacheKey, timeout, TimeUnit.MINUTES);
    }

    @Override
    public void removeOtaCache(String serialNumber) {
        String cacheKey = RedisKeyBuilder.buildDeviceOtaKey(serialNumber);
        redisCache.deleteObject(cacheKey);
    }

    @Override
    public String getOtaCacheValue(String serialNumber, String hKey) {
        String cacheKey = RedisKeyBuilder.buildDeviceOtaKey(serialNumber);
        return redisCache.getCacheMapValue(cacheKey, hKey);
    }

    @Override
    public void setOtaCacheValue(String serialNumber, String hKey, String value, long timeout) {
        String cacheKey = RedisKeyBuilder.buildDeviceOtaKey(serialNumber);
        redisCache.setCacheMapValue(cacheKey, hKey, value);
        redisCache.expire(cacheKey, timeout, TimeUnit.MINUTES);
    }

    @Override
    public boolean checkOtaCacheExist(String serialNumber) {
        return redisCache.hasKey(RedisKeyBuilder.buildDeviceOtaKey(serialNumber));
    }

}
