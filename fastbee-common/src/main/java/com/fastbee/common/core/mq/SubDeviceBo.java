package com.fastbee.common.core.mq;

import lombok.Data;

/**
 * @author bill
 */
@Data
public class SubDeviceBo {
    /**
     * 网关设备id
     */

    private Long gwDeviceId;

    /**
     * 子设备id
     */
    private Long subDeviceId;

    /**
     * 从机地址
     */
    private Integer slaveId;
    /**
     * 子设备名称
     */
    private String subDeviceName;
    /**
     * 子设备编号
     */
    private String subDeviceNo;

    private Long subProductId;
}
