package com.fastbee.mqtt.manager;

import com.fastbee.common.enums.DeviceStatus;
import com.fastbee.common.enums.TopicType;
import com.fastbee.common.utils.gateway.mq.TopicsUtils;
import com.fastbee.iot.domain.Device;
import com.fastbee.iot.service.IDeviceService;
import com.fastbee.mqtt.model.PushMessageBo;
import com.fastbee.mqttclient.PubMqttClient;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

/**
 * mqttBroker获取设备在线情况
 * @author gsb
 * @date 2022/10/26 10:25
 */
@Component
public class MqttRemoteManager {

    @Resource
    private TopicsUtils topicsUtils;
    @Resource
    private IDeviceService deviceService;
    /**
     * true: 使用netty搭建的mqttBroker  false: 使用emq
     */
    @Value("${server.broker.enabled}")
    private Boolean enabled;

    @Resource
    private PubMqttClient pubMqttClient;

    /**
     * 检查设备是否在该集群节点上(集群)
     * @param clientId
     * @return
     */
    public static boolean checkDeviceStatus(String clientId){
        return ClientManager.validClient(clientId);
    }


    /**
     * 公共推送消息方法
     * @param bo 消息体
     */
    public void pushCommon(PushMessageBo bo){
        //netty版本发送
        if (enabled){
            MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.AT_MOST_ONCE, false, 0),
                    new MqttPublishVariableHeader(bo.getTopic(), 0),
                    Unpooled.buffer().writeBytes(bo.getMessage().getBytes())
            );
            ClientManager.pubTopic(publishMessage);
        }else {
            pubMqttClient.publish(0,false,bo.getTopic(), bo.getMessage());
        }
    }
}
