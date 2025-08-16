package com.fastbee.oss.service;

import com.fastbee.common.core.redis.RedisCache;
import com.fastbee.common.exception.GlobalException;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.oss.domain.OssConfig;
import com.fastbee.oss.entity.OssConstant;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class OssFactory {

    private static final Map<String, OssClient> CLIENT_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取默认实例
     */
    public static OssClient instance(RedisCache redis) {
        //获取redis 操作类
        String configKey = redis.getCacheObject(OssConstant.DEFAULT_CONFIG_KEY);

        if (StringUtils.isEmpty(configKey)) {
            throw new GlobalException("文件存储服务类型无法找到!");
        }
        return instance(configKey, redis);
    }

    /**
     * 根据类型获取实例
     */
    public static synchronized OssClient instance(String configKey, RedisCache redis) {
        OssConfig config = redis.getCacheObject(OssConstant.OSS_CONFIG_KEY + configKey);
        if (config == null) {
            throw new GlobalException("系统异常, '" + configKey + "'配置信息不存在!");
        }

        // 使用租户标识避免多个租户相同key实例覆盖
        String key = config.getTenantId() + ":" + configKey;
        OssClient client = CLIENT_CACHE.get(key);
        if (client == null) {
            CLIENT_CACHE.put(key, new OssClient(configKey, config));
            log.info("创建OSS实例 key => {}", configKey);
            return CLIENT_CACHE.get(key);
        }
        // 配置不相同则重新构建
        if (!client.checkPropertiesSame(config)) {
            CLIENT_CACHE.put(key, new OssClient(configKey, config));
            log.info("重载OSS实例 key => {}", configKey);
            return CLIENT_CACHE.get(key);
        }
        return client;
    }

}
