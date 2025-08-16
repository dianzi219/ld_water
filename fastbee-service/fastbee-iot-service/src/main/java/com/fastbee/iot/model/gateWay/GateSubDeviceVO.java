package com.fastbee.iot.model.gateWay;

import lombok.Data;

/**
 * 网关子设备
 * @author bill
 */
@Data
public class GateSubDeviceVO {

    /**
     * 设备id
     */
    private Long deviceId;
    /**
     * 设备编号
     */
    private String serialNumber;
    /**
     * 设备名称
     */
    private String deviceName;
}
