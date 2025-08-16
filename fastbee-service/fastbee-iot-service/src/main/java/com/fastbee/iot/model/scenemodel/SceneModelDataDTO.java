package com.fastbee.iot.model.scenemodel;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author fastb
 * @version 1.0
 * @description: 场景管理变量DTO
 * @date 2024-05-22 10:27
 */
@Data
public class SceneModelDataDTO {

    /** 主键id */
    private Long id;

    /**
     * 数据源id
     * 设备物模型-对应iot_things_model:model_id; 录入、运算型对于scene_model_tag:id
     */
    private Long datasourceId;

    /** 物模型变量名 */
    private String sourceName;

    /**
     * 物模型标识
     */
    private String identifier;

    /**
     * 是否启用
     */
    private Integer enable;

    /** 场景管理id */
    private Long sceneModelId;

    /** 场景关联数据来源id */
    private Long sceneModelDeviceId;

    /** 场景关联数据来源名称 */
    private String sceneModelDeviceName;

    /**
     * 从机地址
     */
    private String slaveIndex;

    /**
     * 从机名称
     */
    private String slaveName;

    /**
     * 当前值
     */
    private String value = "";

    /**
     * 单位
     */
    private String unit;

    /** 更新时间 */
    private String updateTime;

    /**
     * 变量类型
     */
    private Integer variableType;

    private Integer type;

    private Long productId;

    private Long deviceId;

    private String serialNumber;

    private Boolean isShadow;

    /** 是否只读数据(0-否，1-是) */
    private Integer isReadonly;

    private Integer status;
}
