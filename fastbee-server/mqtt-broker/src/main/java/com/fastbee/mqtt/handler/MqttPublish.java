package com.fastbee.mqtt.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fastbee.base.service.ISessionStore;
import com.fastbee.common.constant.FastBeeConstant;
import com.fastbee.common.core.mq.DeviceReportBo;
import com.fastbee.common.core.mq.DeviceStatusBo;
import com.fastbee.common.core.redis.RedisCache;
import com.fastbee.common.enums.DeviceStatus;
import com.fastbee.common.enums.ServerType;
import com.fastbee.common.enums.TopicType;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.common.utils.gateway.mq.TopicsUtils;
import com.fastbee.iot.ruleEngine.MsgContext;
import com.fastbee.iot.ruleEngine.RuleProcess;
import com.fastbee.mq.producer.MessageProducer;
import com.fastbee.mqtt.annotation.Process;
import com.fastbee.mqtt.handler.adapter.MqttHandler;
import com.fastbee.mqtt.manager.ClientManager;
import com.fastbee.mqtt.manager.ResponseManager;
import com.fastbee.mqtt.manager.RetainMsgManager;
import com.fastbee.mqtt.manager.SessionManger;
import com.fastbee.mqtt.model.ClientMessage;
import com.fastbee.mqtt.service.IMessageStore;
import com.fastbee.base.session.Session;
import com.fastbee.base.util.AttributeUtils;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 客户端消息推送处理类
 *
 * @author bill
 */
@Slf4j
@Process(type = MqttMessageType.PUBLISH)
public class MqttPublish implements MqttHandler {

    @Autowired
    private IMessageStore messageStore;
    @Resource
    private TopicsUtils topicsUtils;
    @Resource
    private RedisCache redisCache;
    @Resource
    private RuleProcess ruleProcess;
    @Resource
    private ISessionStore sessionStore;

    @Override
    public void handler(ChannelHandlerContext ctx, MqttMessage message) {
        MqttPublishMessage publishMessage = (MqttPublishMessage) message;
        /*获取客户端id*/
        String clientId = AttributeUtils.getClientId(ctx.channel());
        String topicName = publishMessage.variableHeader().topicName();
        log.debug("=>***客户端[{}],主题[{}],推送消息[{}]", clientId, topicName,
                ByteBufUtil.hexDump(publishMessage.content()));

        /*获取客户端session*/
        Session session = AttributeUtils.getSession(ctx.channel());
        //平台检测session是否同步
        boolean containsKey = sessionStore.containsKey(clientId);
        if (!containsKey) {
            SessionManger.buildSession(clientId, session);
        }
        /*推送保留信息*/
        pubRetain(publishMessage);
        /*响应客户端消息到达Broker*/
        callBack(session, publishMessage, clientId);
        /*推送到订阅的客户端*/
        sendMessageToClients(publishMessage);
        /*推送到MQ处理*/
        sendToMQ(publishMessage, clientId);
        /*累计接收消息数*/
        redisCache.incr2(FastBeeConstant.REDIS.MESSAGE_RECEIVE_TOTAL, -1L);
        redisCache.incr2(FastBeeConstant.REDIS.MESSAGE_RECEIVE_TODAY, 60 * 60 * 24);
    }

    /**
     * 消息推送
     *
     * @param message 推送消息
     */
    @SneakyThrows
    public void sendToMQ(MqttPublishMessage message, String clientId) {
        if (clientId.startsWith(FastBeeConstant.SERVER.WM_PREFIX)) return;
        /*获取topic*/
        String topicName = message.variableHeader().topicName();
        byte[] source = ByteBufUtil.getBytes(message.content());
        DeviceReportBo reportBo = DeviceReportBo.builder()
                .serialNumber(clientId).topicName(topicName).packetId((long) message.variableHeader().packetId())
                .platformDate(DateUtils.getNowDate()).data(source).serverType(ServerType.MQTT).build();
        if (topicName.endsWith(TopicType.SERVICE_INVOKE_REPLY.getTopicSuffix()) ||
                topicName.endsWith(FastBeeConstant.MQTT.OTA_REPLY)) {
            /*设备应答服务器回调数据*/
            reportBo.setReportType(2);
        } else {
            /*设备上报数据*/
            reportBo.setReportType(1);
        }
        // 规则引擎脚本处理,完成后返回结果
        MsgContext context = ruleProcess.processRuleScript(clientId, 1, topicName, new String(source));
        if (!Objects.isNull(context) && StringUtils.isNotEmpty(context.getPayload())
                && StringUtils.isNotEmpty(context.getTopic())) {
            reportBo.setTopicName(context.getTopic());
            reportBo.setData(context.getPayload().getBytes(StandardCharsets.UTF_8));
        }

        if (reportBo.getTopicName().contains("property")) {
            MessageProducer.sendPublishMsg(reportBo);
        } else if (reportBo.getTopicName().contains("status")) {
            String jsonString = new String(reportBo.getData(), StandardCharsets.UTF_8);
            JSONObject jsonObject = JSON.parseObject(jsonString);
            int status = jsonObject.getInteger("status");
            DeviceStatusBo bo = DeviceStatusBo.builder()
                    .serialNumber(topicsUtils.parseSerialNumber(reportBo.getTopicName()))
                    .status(DeviceStatus.convert(status))
                    .build();
            MessageProducer.sendStatusMsg(bo);
        } else if (reportBo.getTopicName().endsWith(FastBeeConstant.MQTT.OTA_REPLY)) {
            MessageProducer.sendDeviceReplyMsg(reportBo);
        } else {
            MessageProducer.sendOtherMsg(reportBo);
        }
    }


    /**
     * 推送消息到订阅客户端
     *
     * @param message 消息
     */
    public void sendMessageToClients(MqttPublishMessage message) {
        ClientManager.pubTopic(message);
    }


    /**
     * 应答客户端，消息到达Broker
     *
     * @param session 客户端
     * @param message 消息
     */
    private void callBack(Session session, MqttPublishMessage message, String clientId) {
        /*获取消息等级*/
        MqttQoS mqttQoS = message.fixedHeader().qosLevel();
        int packetId = message.variableHeader().packetId();
        MqttFixedHeader header;
        switch (mqttQoS.value()) {
            /*0,1消息等级，直接回复*/
            case 0:
            case 1:
                header = new MqttFixedHeader(MqttMessageType.PUBACK, false, mqttQoS, false, 0);
                break;
            case 2:
                // 处理Qos2的消息确认
                if (!messageStore.outRelContains(packetId)) {
                    messageStore.saveRelInMsg(packetId);
                }
                header = new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.AT_MOST_ONCE, false, 0);
                break;
            default:
                header = null;
        }
        /*处理消息等级*/
        handleMqttQos(packetId, mqttQoS, true, clientId);
        /*响应客户端*/
        MqttMessageIdVariableHeader variableHeader = null;
        if (packetId > 0) {
            variableHeader = MqttMessageIdVariableHeader.from(packetId);
        }
        MqttPubAckMessage ackMessage = new MqttPubAckMessage(header, variableHeader);
        if (mqttQoS.value() >= 1) {
            ResponseManager.responseMessage(session, ackMessage, true);
        }
        /*更新客户端ping时间*/
        ClientManager.updatePing(session.getClientId());

    }

    /**
     * Qos不同消息处理
     */
    private void handleMqttQos(int packetId, MqttQoS qoS, boolean clearSession, String clientId) {
        if (qoS == MqttQoS.AT_LEAST_ONCE || qoS == MqttQoS.EXACTLY_ONCE) {
            ClientMessage clientMessage = ClientMessage.of(clientId, qoS, null, false);
            messageStore.savePubMsg(packetId, clientMessage);
        }
    }


    /**
     * 推送保留信息
     */
    @SneakyThrows
    private void pubRetain(MqttPublishMessage message) {
        redisCache.incr2(FastBeeConstant.REDIS.MESSAGE_RETAIN_TOTAL, -1L);
        /*根据message.fixedHeader().isRetain() 判断是否有保留信息*/
        RetainMsgManager.pushMessage(message);
    }


}
