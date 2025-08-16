package com.fastbee.controller.tool;

import com.fastbee.iot.ruleEngine.MsgContext;
import com.fastbee.ruleEngine.core.FlowLogExecutor;
import com.fastbee.ruleEngine.core.RequestIdBuilder;
import com.yomahub.liteflow.builder.el.LiteFlowChainELBuilder;
import com.yomahub.liteflow.flow.LiteflowResponse;
import com.yomahub.liteflow.slot.DefaultContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
public class TestRuleController {

    @Autowired
    private FlowLogExecutor flowExecutorlog;

    @GetMapping("/testrule")
    public void test(@RequestParam String id) {
        MsgContext context = MsgContext.builder()
                .topic("topic")
                .payload("payload")
                .serialNumber("serialNumber")
                .productId(111L)
                .protocolCode("ProtocolCode")
                .build();
        DefaultContext dcxt = new DefaultContext();
        LiteFlowChainELBuilder.createChain().setChainName("dataChain").setEL("THEN(" + id + ",httpBridge)").build();
        // 执行规则脚本
        LiteflowResponse response = flowExecutorlog.execute2Resp("dataChain", null, context, dcxt);
        if (!response.isSuccess()) {
            log.error("规则脚本执行发生错误：" + response.getMessage());
        }
    }

    @GetMapping("/testrule1")
    public void test1(@RequestParam String ifid, @RequestParam String id) {
        MsgContext context = MsgContext.builder()
                .topic("topic")
                .payload("payload")
                .serialNumber("serialNumber")
                .productId(111L)
                .protocolCode("ProtocolCode")
                .build();
        DefaultContext dcxt = new DefaultContext();
        LiteFlowChainELBuilder.createChain().setChainName("dataChain").setEL("IF(" + ifid + "," + id +")").build();
        String Rid = RequestIdBuilder.buildProductRequestId(context.getProductId(),context.getSerialNumber());
        // 执行规则脚本
        LiteflowResponse response = flowExecutorlog.execute2RespWithRid("dataChain", null, Rid, context, dcxt);
        if (!response.isSuccess()) {
            log.error("规则脚本执行发生错误：" + response.getMessage());
        }
    }
}
