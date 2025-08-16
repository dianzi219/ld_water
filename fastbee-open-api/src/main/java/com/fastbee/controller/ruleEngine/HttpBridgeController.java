package com.fastbee.controller.ruleEngine;

import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.iot.model.ScriptCondition;
import com.fastbee.iot.ruleEngine.MsgContext;
import com.fastbee.iot.service.IScriptService;
import com.fastbee.ruleEngine.core.FlowLogExecutor;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/bridge")
public class HttpBridgeController {
    @Resource
    private IScriptService scriptService;

    @Autowired
    private FlowLogExecutor flowExecutor;

    @ApiOperation("数据桥接get入口")
    @GetMapping(value = "/get")
    public AjaxResult bridgeGet(HttpServletRequest request)
    {
        ScriptCondition scriptCondition = ScriptCondition.builder()
                .scriptPurpose(1)
                .scriptEvent(5)
                .route("/bridge/get")
                .build();
        MsgContext context = MsgContext.builder().dataMap(buildDataMap(request)).build();
        //返回处理完的消息上下文
        return AjaxResult.success(scriptService.execRuleScript(scriptCondition,context));
    }
    @ApiOperation("数据桥接put入口")
    @PutMapping(value = "/put")
    public AjaxResult bridgePut(HttpServletRequest request, @RequestBody Object body)
    {
        ScriptCondition scriptCondition = ScriptCondition.builder()
                .scriptPurpose(1)
                .scriptEvent(5)
                .route("/bridge/put")
                .build();
        MsgContext context = MsgContext.builder().dataMap(buildDataMap(request)).payload(body.toString()).build();
        //返回处理完的消息上下文
        return AjaxResult.success(scriptService.execRuleScript(scriptCondition,context));
    }
    @ApiOperation("数据桥接post入口")
    @PostMapping(value = "/post")
    public AjaxResult bridgePost(HttpServletRequest request, @RequestBody Object body)
    {
        ScriptCondition scriptCondition = ScriptCondition.builder()
                .scriptPurpose(1)
                .scriptEvent(5)
                .route("/bridge/post")
                .build();
        MsgContext context = MsgContext.builder().dataMap(buildDataMap(request)).payload(body.toString()).build();
        //返回处理完的消息上下文
        return AjaxResult.success(scriptService.execRuleScript(scriptCondition,context));
    }

    private ConcurrentHashMap<String, Object> buildDataMap(HttpServletRequest request)
    {
        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String, List<String>> headers = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, Collections.list(request.getHeaders(headerName)));
        }
        JSONObject headersjson = new JSONObject(headers);
        ConcurrentHashMap<String, Object> dataMap = new ConcurrentHashMap<>();
        dataMap.put("headers", headersjson.toJSONString());
        JSONObject paramsjson = new JSONObject(request.getParameterMap());
        dataMap.put("params", paramsjson.toJSONString());
        return dataMap;
    }

}
