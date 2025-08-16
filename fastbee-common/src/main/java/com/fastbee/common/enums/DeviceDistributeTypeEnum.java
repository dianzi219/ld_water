package com.fastbee.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description:
 * @author admin
 * @date 2024-07-18 14:52
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public enum DeviceDistributeTypeEnum {

    /**
     * 确保唯一，不能重复
     */
    SELECT(1, "选择分配"),
    IMPORT(2,"导入分配");

    /**
     * 渠道类型
     */
    private Integer type;

    /**
     * 描述
     */
    private String desc;

    public static String getDesc(Integer type) {
        for (DeviceDistributeTypeEnum item : DeviceDistributeTypeEnum.values()) {
            if (item.getType().equals(type)) {
                return item.getDesc();
            }
        }
        return "";
    }
}
