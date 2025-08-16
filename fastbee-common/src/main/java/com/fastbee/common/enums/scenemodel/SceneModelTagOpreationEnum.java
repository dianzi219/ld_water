package com.fastbee.common.enums.scenemodel;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 场景变量统计方式
 * @author fastb
 * @date 2024-06-05 10:42
 * @version 1.0
 */
@AllArgsConstructor
@Getter
public enum SceneModelTagOpreationEnum {
    /**
     * 原值
     */
    ORIGINAL_VALUE(1, "原值"),
    /**
     * 累计值
     */
    CUMULATIVE(2, "累计值"),
    /**
     * 平均值
     */
    AVERAGE_VALUE(3, "平均值"),
    /**
     * 最大值
     */
    MAX_VALUE(4, "最大值"),
    /**
     * 最小值
     */
    MIN_VALUE(5,"最小值");

    private final Integer code;
    private final String desc;

    public static SceneModelTagOpreationEnum getByCode(Integer code) {
        for (SceneModelTagOpreationEnum opreationEnum : SceneModelTagOpreationEnum.values()) {
            if (opreationEnum.getCode().equals(code)) {
                return opreationEnum;
            }
        }
        return null;
    }
}
