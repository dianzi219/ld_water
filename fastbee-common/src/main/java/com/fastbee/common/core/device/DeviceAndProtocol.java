package com.fastbee.common.core.device;

import lombok.Data;

/**
 * @author gsb
 * @date 2024/6/14 9:25
 */
@Data
public class DeviceAndProtocol {

    /**
     * 子设备编号
     */
    private Long deviceId;
    /**
     * 设备编号
     */
    private String serialNumber;
    /**
     * 协议编号
     */
    private String protocolCode;
    /**
     * 产品id
     */
    private Long productId;

    private String transport;

    /**
     * 设备类型
     */
    private Integer deviceType;
    /**
     * 子设备地址
     */
    private Integer slaveId;
    /**
     * 网关绑定的子设备地址
     */
    private Integer proSlaveId;
    /**
     * 网关设备id
     */
    private Long gwDeviceId;
    /**
     * 网关设备产品id
     */
    private Long gwProductId;
    /**
     * 网关设备编号
     */
    private String gwSerialNumber;
    /**
     * 网关设备名
     */
    private String gwDeviceName;
    /**
     * 网关产品名
     */
    private String gwProductName;

    private Long tenantId;

}
