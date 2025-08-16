package com.fastbee.protocol.base.protocol;


import com.fastbee.common.core.mq.DeviceReport;
import com.fastbee.common.core.mq.MQSendMessageBo;
import com.fastbee.common.core.mq.message.DeviceData;
import com.fastbee.common.core.mq.message.FunctionCallBackBo;

/**
 * 基础协议
 * @author gsb
 * @date 2022/10/10 15:48
 */
public interface IProtocol {

    DeviceReport decode(DeviceData data, String clientId);

    FunctionCallBackBo encode(MQSendMessageBo message);

    /**
     * 默认方法，处理设备回复的报文编码
     */
    public default byte[] encodeCallBack(Object message) {
        return new byte[0];
    }

    default byte[] encode(DeviceData data)  {
        return new byte[0];
    }

    default DeviceData decode(String message) {
        return DeviceData.builder().build();
    }

}
