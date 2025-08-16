package com.fastbee.ruleEngine.core;

import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.gateway.mq.TopicsUtils;
import com.fastbee.mqttclient.PubMqttClient;
import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.flow.LiteflowResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Component
@Slf4j
public class FlowLogExecutor {
    @Resource
    private FlowExecutor flowExecutor;
    @Resource
    private PubMqttClient mqttClient;

    Set<String> ScriptSet = new HashSet<>();
    Set<String> SceneSet = new HashSet<>();

    public LiteflowResponse execute2Resp(String chainId, Object param, Object... contextBeanArray) {
        LiteflowResponse response = flowExecutor.execute2Resp(chainId, param, contextBeanArray);
        printResponse(response);
        return response;
    }

    public LiteflowResponse execute2RespWithRid(String chainId, Object param, String requestId, Object... contextBeanArray) {
        LiteflowResponse response = flowExecutor.execute2RespWithRid(chainId, param, requestId, contextBeanArray);
        printResponseWithRid(requestId, response);
        return response;
    }

    public LiteflowResponse execute2Future(String chainId, Object param, Object... contextBeanArray) throws ExecutionException, InterruptedException {
        Future<LiteflowResponse> future = flowExecutor.execute2Future(chainId, param, contextBeanArray);
        LiteflowResponse response = future.get();
        printResponse(response);
        return response;
    }

    public LiteflowResponse execute2FutureWithRid(String chainId, Object param, String requestId, Object... contextBeanArray) throws ExecutionException, InterruptedException {
        Future<LiteflowResponse> future = flowExecutor.execute2FutureWithRid(chainId, param, requestId, contextBeanArray);
        LiteflowResponse response = future.get();
        printResponseWithRid(requestId, response);
        return response;
    }

    public void printResponse(LiteflowResponse response) {
        if (!response.isSuccess()) {
            Exception e = response.getCause();
            log.error("报错信息：{}", e.getMessage(), e);
        } else {
            //步骤详情
//            Map<String, List<CmpStep>> stepMap = response.getExecuteSteps();
//            stepMap.forEach((k, v) -> {
//                v.forEach((step) -> {
//                    log.info("步骤：{}({})，执行时间：{}", step.getNodeId(), step.getNodeName(), step.getTimeSpent());
//                });
//            });
            //每各步骤执行时间
            String stepStr = response.getExecuteStepStrWithTime();
            log.info("步骤：{}", stepStr);
        }
    }

    public void printResponseWithRid(String requestId, LiteflowResponse response) {
        if (!response.isSuccess()) {
            Exception e = response.getCause();
            log.error("[{}]-报错信息：{}", requestId, e.toString());
            publishLog(requestId, response.getCause().toString());
        } else {
            //步骤详情
//            Map<String, List<CmpStep>> stepMap = response.getExecuteSteps();
//            stepMap.forEach((k, v) -> {
//                v.forEach((step) -> {
//                    log.info("[{}]-步骤：{}({})，执行时间：{}", requestId, step.getNodeId(), step.getNodeName(), step.getTimeSpent());
//                });
//            });
            //每各步骤执行时间
            String stepStr = response.getExecuteStepStrWithTime();
            log.info("步骤：{}", stepStr);
            publishLog(requestId, stepStr);
        }
    }

    public void openScriptLog(String scriptId) {
        ScriptSet.add(scriptId);
    }

    public void closeScriptLog(String scriptId) {
        ScriptSet.remove(scriptId);
    }

    public void openSceneLog(String sceneId) {
        SceneSet.add(sceneId);
    }

    public void closeSceneLog(String sceneId) {
        SceneSet.remove(sceneId);
    }

    //requestId : Chainid + nodeId|scriptId  / sceneId + nodeId|scriptId
    public void publishLog(String requestId, String log) {
        String[] splits = requestId.split("/");
        String message = "{\"requestId\":\"" + requestId + "\",\"time\":\"" + DateUtils.getNowDate() + "\",\"log\":\"" + log + "\"}";
        for (String id : splits) {
            if (ScriptSet.contains(id)) {
                String topic = TopicsUtils.buildRuleEngineTopic(id);
                mqttClient.publish(1, false, topic, message);
            }
            if (SceneSet.contains(id)) {
                String topic = TopicsUtils.buildRuleEngineTopic(id);
                mqttClient.publish(1, false, topic, message);
            }
        }
    }

}
