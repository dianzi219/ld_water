package com.fastbee.iot.ruleEngine;

import com.fastbee.common.core.redis.RedisCache;
import com.yomahub.liteflow.script.annotation.ScriptBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 规则引擎上下文执行方法
 * @author gsb
 * @date 2024/2/2 14:19
 */
@Component
@Slf4j
@ScriptBean("msgContextService")
public class MsgContextService {

    private final RedisCache redisCache;

    public MsgContextService(RedisCache redisCache){
        this.redisCache = redisCache;
    }

    public void process(){
        //执行的业务逻辑
        log.info("process");
    }

    public boolean processBoolean(boolean test){
        //执行的业务逻辑
        log.info("processBoolean");
        return test;
    }

    public String processSwitch(String Id){
        //执行的业务逻辑
        log.info("processSwitch");
        return Id;
    }

    public int processFor(int count){
        //执行的业务逻辑
        log.info("processFor");
        return count;
    }

}
