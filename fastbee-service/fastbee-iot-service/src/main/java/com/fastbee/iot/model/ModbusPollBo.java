package com.fastbee.iot.model;

import com.fastbee.common.core.device.DeviceAndProtocol;
import com.fastbee.iot.domain.Device;
import lombok.Data;

import java.util.List;

/**
 * @author gsb
 * @date 2024/7/3 16:06
 */
@Data
public class ModbusPollBo {

    private DeviceAndProtocol gateway;
    private List<ModbusJobBo> commandList;
}
