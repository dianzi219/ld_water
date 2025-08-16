package com.fastbee.iot.data.service;

import com.fastbee.common.core.mq.message.DeviceMessage;
import com.fastbee.common.core.thingsModel.ThingsModelSimpleItem;
import com.fastbee.iot.model.VariableReadVO;
import com.fastbee.modbus.model.ModbusRtu;

import java.util.List;

/**
 * 设备消息Service接口
 */
public interface IDeviceMessageService {

    void messagePost(DeviceMessage deviceMessage);

    String messageEncode(ModbusRtu modbusRtu);

    List<ThingsModelSimpleItem> messageDecode(DeviceMessage deviceMessage);


    /**
     * 变量读取
     * @param readVO
     */
    public void readVariableValue(VariableReadVO readVO);
}
