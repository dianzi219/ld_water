package com.fastbee.iot.data.service;

import com.fastbee.common.core.mq.DeviceReport;
import com.fastbee.common.core.mq.DeviceReportBo;
import com.fastbee.iot.domain.Device;
import com.fastbee.iot.model.DeviceStatusVO;
import com.fastbee.protocol.base.protocol.IProtocol;

/**
 * 处理设备上报数据解析
 * @author gsb
 * @date 2022/10/10 13:48
 */
public interface IDeviceReportMessageService {

    /**
     * 处理设备主动上报数据
     * @param bo
     */
    public void parseReportMsg(DeviceReportBo bo);

    /**
     * 处理设备普通消息回调
     * @param bo
     */
    public void parseReplyMsg(DeviceReportBo bo);

    /**
     * 处理设备OTA升级
     * @param bo
     */
    public void parseOTAUpdateReply(DeviceReportBo bo);


    /**
     * 构建消息
     * @param bo
     */
    public DeviceStatusVO buildReport(DeviceReportBo bo);


    /**
     * 根据产品id获取协议处理器
     */
    IProtocol selectedProtocol(Long productId);

}
