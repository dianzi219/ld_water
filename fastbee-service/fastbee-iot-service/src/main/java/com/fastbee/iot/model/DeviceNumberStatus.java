package com.fastbee.iot.model;

import lombok.Data;

/**
 * @author kerwincui
 * @date 2024-02-06 11:18
 */
@Data
public class DeviceNumberStatus {

    /**
     * 设备编号
     */
    private String serialNumber;

    /**
     * 设备状态（1-未激活，2-禁用，3-在线，4-离线）
     */
    private int  status;


}
