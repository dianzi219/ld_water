package com.fastbee.iot.data.service.impl;

import com.alibaba.fastjson2.JSON;
import com.fastbee.common.core.thingsModel.ThingsModelSimpleItem;
import com.fastbee.iot.data.ruleEngine.SceneContext;
import com.fastbee.iot.data.service.IRuleEngine;
import com.fastbee.iot.domain.*;
import com.fastbee.iot.model.ScriptCondition;
import com.fastbee.iot.service.ISceneDeviceService;
import com.fastbee.iot.service.IScriptService;
import com.fastbee.common.core.mq.message.ReportDataBo;
import com.fastbee.ruleEngine.core.FlowLogExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 规则引擎处理数据方法
 *
 * @author bill
 */
@Component
@Slf4j
public class RuleEngineHandler implements IRuleEngine {

    @Resource
    private ISceneDeviceService sceneDeviceService;

    @Resource
    private IScriptService scriptService;

    @Autowired
    private FlowLogExecutor flowExecutor;

    private static final Map<String, ScheduledExecutorService> CEHCK_CACHE = new ConcurrentHashMap<>();

    /**
     * 规则匹配(告警和场景联动)
     *
     * @param bo 上报数据模型bo
     * @see ReportDataBo
     */
    public void ruleMatch(ReportDataBo bo) {
        try {
            // 场景联动处理
            this.sceneProcess(bo);
        } catch (Exception e) {
            log.error("接收数据，解析数据时异常 message={}", e, e.getMessage());
        }
    }

    /**
     * 场景规则处理
     */
    public void sceneProcess(ReportDataBo bo) throws ExecutionException, InterruptedException {
        // 查询设备关联的场景
        SceneDevice sceneDeviceParam = new SceneDevice();
        sceneDeviceParam.setProductId(bo.getProductId());
        sceneDeviceParam.setSerialNumber(bo.getSerialNumber());
        List<Scene> sceneList = sceneDeviceService.selectTriggerDeviceRelateScenes(sceneDeviceParam);

        int type = bo.getType();
        // 获取上报的物模型
        List<ThingsModelSimpleItem> thingsModelSimpleItems = bo.getDataList();
        if (CollectionUtils.isEmpty(bo.getDataList())) {
            thingsModelSimpleItems = JSON.parseArray(bo.getMessage(), ThingsModelSimpleItem.class);
        }
        // 执行场景规则,异步非阻塞
        for (Scene scene : sceneList) {
            SceneContext context = new SceneContext(bo.getSerialNumber(), bo.getProductId(), type, thingsModelSimpleItems);
            ScriptCondition scriptCondition = ScriptCondition.builder().sceneId(scene.getSceneId()).build();
            String requestId = scene.getChainName();
            List<Script> list = scriptService.selectExecRuleScriptList(scriptCondition);
            for(Script script : list) {
                requestId  = requestId + "/" + script.getScriptId();
            }
            flowExecutor.execute2FutureWithRid(scene.getChainName(), null, requestId, context);
        }
    }

    public void addCheckTask(String key, ScheduledExecutorService task) {
        ScheduledExecutorService taskold = CEHCK_CACHE.get(key);
        if (taskold != null) {
            taskold.shutdown();
        }
        CEHCK_CACHE.put(key, task);
    }

    public void removeCheckTask(String key) {
        ScheduledExecutorService taskold = CEHCK_CACHE.get(key);
        if (taskold != null) {
            taskold.shutdown();
            CEHCK_CACHE.remove(key);
        }
    }


}
