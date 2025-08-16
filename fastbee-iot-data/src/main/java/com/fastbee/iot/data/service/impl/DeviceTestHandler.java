package com.fastbee.iot.data.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.core.device.DeviceAndProtocol;
import com.fastbee.common.core.mq.DeviceTestReportBo;
import com.fastbee.common.core.mq.message.DeviceMessage;
import com.fastbee.common.enums.TopicType;
import com.fastbee.common.utils.gateway.mq.TopicsUtils;
import com.fastbee.iot.data.service.IDeviceTestService;
import com.fastbee.iot.service.IDeviceService;
import com.fastbee.mqtt.manager.MqttRemoteManager;
import com.fastbee.mqtt.model.PushMessageBo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

/**
 * @author bill
 */
@Slf4j
@Component
public class DeviceTestHandler implements IDeviceTestService {

    @Resource
    private TopicsUtils topicsUtils;
    @Resource
    private MqttRemoteManager mqttRemoteManager;
    @Resource
    private IDeviceService deviceService;

    /**
     * 处理数据调试信息
     * @param testReportBo
     */
    @Override
    public void messageHandler(DeviceTestReportBo testReportBo){
        boolean isReply = testReportBo.getIsReply();
        Long productId = testReportBo.getProductId();
        String serialNumber = testReportBo.getSerialNumber();
        DeviceMessage deviceMessage = new DeviceMessage();
        deviceMessage.setMessage(testReportBo.getSources());
        deviceMessage.setTime(new Date());
        if (!isReply) {
            deviceMessage.setTopicName(TopicType.SERVICE_INVOKE_REPLY.getTopicSuffix());
        } else {
            deviceMessage.setTopicName(TopicType.PROPERTY_POST.getTopicSuffix());
        }
        if (Objects.isNull(productId)){
            DeviceAndProtocol protocol = deviceService.selectProtocolBySerialNumber(serialNumber);
            productId = protocol.getProductId();
        }
        //发送至前端
        PushMessageBo messageBo = new PushMessageBo();
        messageBo.setTopic(topicsUtils.buildTopic(productId, serialNumber.toUpperCase(), TopicType.WS_SERVICE_INVOKE));
        JSONObject pushObj = new JSONObject();
        pushObj.put("message", testReportBo.getThingsModelSimpleItem());
        pushObj.put("sources",testReportBo.getSources());
        messageBo.setMessage(JSON.toJSONString(pushObj));
        mqttRemoteManager.pushCommon(messageBo);
    }

}
