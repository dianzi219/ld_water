package com.fastbee.iot.model.scenemodel;

import lombok.Data;

import java.util.List;

/**
 * @author fastb
 * @version 1.0
 * @description: 查询场景变量历史数据参数
 * @date 2024-06-14 12:02
 */
@Data
public class SceneHistoryParam {

    private String ids;

    private Long sceneModelId;

    private Long sceneModelDeviceId;

    private String serialNumber;

    private Integer variableType;

    /** 查询用的开始时间 */
    private String beginTime;

    /** 查询用的结束时间 */
    private String endTime;
}
