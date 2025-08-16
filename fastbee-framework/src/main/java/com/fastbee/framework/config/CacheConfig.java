package com.fastbee.framework.config;

import com.fastbee.framework.manager.SpringCacheManager;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 缓存配置
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.enable", havingValue = "true")
public class CacheConfig {
    @Value("${spring.cache.ttl}")
    private Long ttl;

    @Bean
    public Cache<Object, Object> caffeine() {
        return Caffeine.newBuilder()
                // 设置最后一次写入或访问后经过固定时间过期
                .expireAfterWrite(60, TimeUnit.SECONDS)
                // 初始的缓存空间大小
                .initialCapacity(100)
                // 缓存的最大条数
                .maximumSize(10000)
                .build();
    }

    /**
     * 自定义缓存管理器 整合spring-cache
     */
    @Bean
    public CacheManager cacheManager() {
        return new SpringCacheManager(ttl);
    }

}
