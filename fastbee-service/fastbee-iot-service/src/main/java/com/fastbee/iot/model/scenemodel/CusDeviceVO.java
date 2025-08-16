package com.fastbee.iot.model.scenemodel;

import lombok.Data;

/**
 * @author fastb
 * @version 1.0
 * @description: 场景关联设备信息
 * @date 2024-06-13 16:45
 */
@Data
public class CusDeviceVO {

    /**
     * 设备名称
     */
    private String name;

    /**
     * 产品id
     */
    private Long productId;

    /**
     * 设备编号
     */
    private String serialNumber;
}
