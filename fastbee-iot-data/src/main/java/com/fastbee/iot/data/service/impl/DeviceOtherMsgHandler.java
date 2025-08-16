package com.fastbee.iot.data.service.impl;

import com.fastbee.common.core.mq.DeviceReportBo;
import com.fastbee.common.enums.TopicType;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.common.utils.gateway.mq.TopicsUtils;
import com.fastbee.iot.data.service.IDataHandler;
import com.fastbee.iot.data.service.IMqttMessagePublish;
import com.fastbee.common.core.mq.message.ReportDataBo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author gsb
 * @date 2023/2/27 14:42
 */
@Component
@Slf4j
public class DeviceOtherMsgHandler {

    @Resource
    private TopicsUtils topicsUtils;
    @Resource
    private IDataHandler dataHandler;
    @Resource
    private IMqttMessagePublish messagePublish;

    /**
     * true: 使用netty搭建的mqttBroker  false: 使用emq
     */
    @Value("${server.broker.enabled}")
    private Boolean enabled;


    /**
     * 非属性消息消息处理入口
     *
     * @param bo
     */
    public void messageHandler(DeviceReportBo bo) {
        String type = "";
        String name = topicsUtils.parseTopicName(bo.getTopicName());
        if (StringUtils.isEmpty(name) || name.endsWith(TopicType.FUNCTION_GET.getTopicSuffix())) return;
        ReportDataBo data = this.buildReportData(bo);
        TopicType topicType = TopicType.getType(name);
        switch (topicType) {
            case INFO_POST:
                dataHandler.reportDevice(data);
                break;
            case NTP_POST:
                messagePublish.publishNtp(data);
                break;
            case FUNCTION_POST:
                data.setShadow(false);
                data.setType(2);
                data.setRuleEngine(true);
                dataHandler.reportData(data);
                break;
            case EVENT_POST:
                data.setType(3);
                data.setRuleEngine(true);
                dataHandler.reportEvent(data);
                break;
        }
    }

    /**
     * 组装数据
     */
    private ReportDataBo buildReportData(DeviceReportBo bo) {
        String message = new String(bo.getData());
        log.info("收到设备信息[{}]", message);
        Long productId = topicsUtils.parseProductId(bo.getTopicName());
        String serialNumber = topicsUtils.parseSerialNumber(bo.getTopicName());
        ReportDataBo dataBo = new ReportDataBo();
        dataBo.setMessage(message);
        dataBo.setProductId(productId);
        dataBo.setSerialNumber(serialNumber);
        dataBo.setRuleEngine(false);
        return dataBo;
    }

}
