package com.fastbee.iot.ruleEngine;

import com.fastbee.common.utils.spring.SpringUtils;
import com.fastbee.iot.model.ScriptCondition;
import com.fastbee.iot.service.IScriptService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;

@Slf4j
class MessageArrivedHandler implements IMqttMessageListener {

    private String topic;

    private static IScriptService scriptService = SpringUtils.getBean(IScriptService.class);

    public MessageArrivedHandler(String topic) {
        this.topic = topic;
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        log.info("Message arrived on topic '" + topic + "': " + new String(mqttMessage.getPayload()));
        if (this.topic.equals(topic)) {
            // 处理特定主题的消息
            ScriptCondition scriptCondition = ScriptCondition.builder()
                    .scriptPurpose(1)
                    .scriptEvent(6)
                    .route(topic)
                    .build();
            String payload = new String(mqttMessage.getPayload(), StandardCharsets.UTF_8);
            MsgContext context = MsgContext.builder()
                    .topic(topic)
                    .payload(payload)
                    .build();
            //返回处理完的消息上下文
            scriptService.execRuleScript(scriptCondition,context);
        }
    }
}
