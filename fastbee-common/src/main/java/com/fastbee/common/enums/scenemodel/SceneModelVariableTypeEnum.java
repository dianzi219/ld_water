package com.fastbee.common.enums.scenemodel;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
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
public enum SceneModelVariableTypeEnum {
    //1==设备物模型（直采变量），2=录入型变量，3=运算型变量
    THINGS_MODEL(1, "设备配置"),
    INPUT_VARIABLE(2, "录入型变量"),
    OPERATION_VARIABLE(3, "运算型变量");

    public final static List<SceneModelVariableTypeEnum> ADD_LIST = Arrays.asList(INPUT_VARIABLE, OPERATION_VARIABLE);


    private final Integer type;

    private final String name;

}
