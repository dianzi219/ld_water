package com.fastbee.iot.model.modbus;

import com.fastbee.iot.domain.DeviceJob;
import lombok.Data;

import java.util.List;

/**
 * @author gsb
 * @date 2024/6/21 9:32
 */
@Data
public class ModbusJobVO {

    private List<DeviceJob> list;

    private Long deviceId;
}
