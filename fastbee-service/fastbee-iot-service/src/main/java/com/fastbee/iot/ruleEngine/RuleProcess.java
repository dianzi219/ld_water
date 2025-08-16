package com.fastbee.iot.ruleEngine;

import com.alibaba.fastjson2.JSON;
import com.fastbee.common.core.redis.RedisCache;
import com.fastbee.common.core.redis.RedisKeyBuilder;
import com.fastbee.iot.model.ProductCode;
import com.fastbee.iot.model.ScriptCondition;
import com.fastbee.iot.service.IDeviceService;
import com.fastbee.iot.service.IScriptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 执行规则引擎
 *
 * @author gsb
 * @date 2024/2/3 16:07
 */
@Component
@Slf4j
public class RuleProcess {

    @Resource
    private IScriptService scriptService;
    @Resource
    private RedisCache redisCache;
    @Resource
    private IDeviceService deviceService;

    /**
     * 规则引擎脚本处理
     *
     * @param topic
     * @param payload
     * @param event   1=设备上报 2=平台下发 3=设备上线 4=设备下线 （其他可以增加设备完成主题订阅之类）
     * @return
     */
    public MsgContext processRuleScript(String serialNumber, int event, String topic, String payload) {
        ProductCode productCode = getDeviceDetail(serialNumber);
        if (Objects.isNull(productCode)) {
            return new MsgContext();
        }
        // 查询数据流脚本组件
        ScriptCondition scriptCondition = ScriptCondition.builder()
                .scriptPurpose(1)
                .scriptEvent(event)
                .productId(productCode.getProductId())
                .build();
        MsgContext context = MsgContext.builder()
                .serialNumber(serialNumber)
                .productId(productCode.getProductId())
                .protocolCode(productCode.getProtocolCode())
                .payload(payload)
                .topic(topic)
                .build();
        //返回处理完的消息上下文
        return scriptService.execRuleScript(scriptCondition, context);
    }


    /**
     * 查询产品id,协议编号，缓存到redis,后续查询协议的地方替换数据库查询
     *
     * @param serialNumber
     */
    public ProductCode getDeviceDetail(String serialNumber) {
        ProductCode productCode;
        String cacheKey = RedisKeyBuilder.buildDeviceMsgCacheKey(serialNumber);
        if (redisCache.containsKey(cacheKey)) {
            Object cacheObject = redisCache.getCacheObject(cacheKey);
            return JSON.parseObject(cacheObject.toString(), ProductCode.class);
        }
        productCode = deviceService.getProtocolBySerialNumber(serialNumber);
        String jsonString = JSON.toJSONString(productCode);
        redisCache.setCacheObject(cacheKey, jsonString);
        return productCode;
    }
}
