package com.fastbee.iot.ruleEngine;

import com.alibaba.fastjson2.JSON;
import com.fastbee.iot.domain.Bridge;
import com.fastbee.iot.domain.MqttClient;
import com.fastbee.iot.service.IBridgeService;
import com.fastbee.iot.service.IMqttClientService;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@LiteflowComponent("mqttBridge")
public class MqttBridge extends NodeComponent {
    @Autowired
    private IBridgeService bridgeService;

    @Autowired
    private IMqttClientService mqttClientService;

    @Override
    public void process() throws Exception {
        // 获取上下文对象
        MsgContext cxt = this.getContextBean(MsgContext.class);
        Integer id = cxt.getData("mqttBridgeID");
        Bridge bridge = bridgeService.queryByIdWithCache(Long.valueOf(id));
        if (bridge != null) {
            MqttClient config = JSON.parseObject(bridge.getConfigJson(), MqttClient.class);
            MqttAsyncClient client = MqttClientFactory.instance(mqttClientService.buildmqttclientconfig(config));
            MqttMessage message = new MqttMessage();
            message.setPayload(cxt.getPayload().getBytes());
            try {
                client.publish(cxt.getTopic(),message);
                log.info("=>桥接发布主题成功 topic={},message={}", cxt.getTopic(), message);
            } catch (MqttException e) {
                log.error("msg:"+e.getMessage());
                log.error("cause:"+e.getCause());
                log.error("reason:"+e.getReasonCode());
                log.error("=>桥接发布主题时发生错误 topic={},message={}", cxt.getTopic(), e.getMessage());
            }
        }
    }
}
