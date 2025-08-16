package com.fastbee.iot.model.scenemodel;

import lombok.Data;

/**
 * @author fastb
 * @version 1.0
 * @description: 场景数据来源信息
 * @date 2024-05-30 11:35
 */
@Data
public class SceneModelDeviceVO {

    private Long sceneModelDataId;

    private Long sceneModelDeviceId;

    private String sceneModelDeviceName;
}
