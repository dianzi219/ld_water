package com.fastbee.iot.model.scenemodel;

import lombok.Data;

/**
 * @author fastb
 * @version 1.0
 * @description: 查询单个物模型来源
 * @date 2024-05-31 10:25
 */
@Data
public class SceneDeviceThingsModelVO {

    /**
     * 物模型标识
     */
    private String identifier;

    /**
     * 产品id
     */
    private Long productId;

    /**
     * 设备编号
     */
    private String serialNumber;
}
