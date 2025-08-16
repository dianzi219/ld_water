package com.fastbee.iot.model.modbus;

import com.fastbee.common.core.mq.message.ModbusPollMsg;
import com.fastbee.iot.domain.DeviceJob;
import lombok.Data;

/**
 * @author gsb
 * @date 2024/8/28 11:31
 */
@Data
public class ModbusPollJob {

    private ModbusPollMsg pollMsg;

    private DeviceJob deviceJob;
    /**
     * 1- 主动读取  2- 轮训下发
     */
    private Integer type;
}
