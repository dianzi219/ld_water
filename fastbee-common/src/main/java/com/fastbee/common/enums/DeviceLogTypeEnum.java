package com.fastbee.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 场景管理物模型、变量类型枚举
 * 注意：以下4张表下的variable_type相关字段统一用该枚举，保持一致
 * scene_model_tag表、scene_tag_points表、scene_model_device表、scene_model_data表
 * @author fastb
 * @date 2024-05-22 10:01
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public enum DeviceLogTypeEnum {

    ATTRIBUTE_REPORT(1, "属性上报"),
    INVOKE_FUNCTION(2, "调用功能"),
    EVENT_REPORT(3, "事件上报"),
    DEVICE_UPDATE(4, "设备升级"),
    DEVICE_ONLINE(5, "设备上线"),
    DEVICE_OFFLINE(6, "设备离线"),
    SCENE_VARIABLE_REPORT(7, "场景录入、运算变量上报下发");

    private final Integer type;

    private final String desc;

}
