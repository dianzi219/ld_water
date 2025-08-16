package com.fastbee.iot.model.gateWay;

import lombok.Data;

import java.util.List;

/**
 * @author gsb
 * @date 2024/5/29 15:11
 */
@Data
public class SubDeviceAddVO {
    /**
     * 网关设备id
     */
    private Long gwDeviceId;
    /**
     * 子设备id集合
     */
    private Long[] subDeviceIds;
}
