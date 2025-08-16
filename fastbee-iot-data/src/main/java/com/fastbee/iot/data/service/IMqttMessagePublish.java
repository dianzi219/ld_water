package com.fastbee.iot.data.service;

import com.fastbee.common.core.mq.DeviceReportBo;
import com.fastbee.common.core.mq.MQSendMessageBo;
import com.fastbee.common.core.mq.message.DeviceDownMessage;
import com.fastbee.common.core.mq.message.FunctionCallBackBo;
import com.fastbee.common.core.mq.ota.OtaUpgradeBo;
import com.fastbee.common.core.thingsModel.ThingsModelSimpleItem;
import com.fastbee.common.enums.DeviceStatus;
import com.fastbee.common.enums.TopicType;
import com.fastbee.iot.domain.Device;
import com.fastbee.common.core.mq.message.ReportDataBo;
import org.checkerframework.checker.units.qual.C;

import java.util.List;

public interface IMqttMessagePublish {


    /**
     * 服务(指令)下发
     */
    public void funcSend(MQSendMessageBo bo) throws InterruptedException;

    /**
     * OTA升级下发
     */
    public void upGradeOTA(OtaUpgradeBo bo);

    /**
     * 下发数据编码
     */
    FunctionCallBackBo buildMessage(MQSendMessageBo bo);

    /**
     * 1.发布设备状态
     */
    public void publishStatus(Long productId, String deviceNum, int deviceStatus, int isShadow, int rssi);


    /**
     * 2.发布设备信息
     */
    public void publishInfo(Long productId, String deviceNum);


    /**
     * 3.发布时钟同步信息
     *
     * @param bo 数据模型
     */
    public void publishNtp(ReportDataBo bo);


    /**
     * 4.发布属性
     * delay 延时，秒为单位
     */
    public void publishProperty(Long productId, String deviceNum, List<ThingsModelSimpleItem> thingsList, int delay);


    /**
     * 5.发布功能
     * delay 延时，秒为单位
     */
    public void publishFunction(Long productId, String deviceNum, List<ThingsModelSimpleItem> thingsList, int delay);


    /**
     * 推送设备状态
     *
     * @param device 设备
     * @param status       状态
     */
    public void pushDeviceStatus(Device device, DeviceStatus status);

}
