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
public enum DeviceRecordTypeEnum {

    /**
     * 确保唯一，不能重复
     */
    IMPORT(1, "导入记录"),
    RECOVERY(2,"回收记录"),
    ASSIGNMENT(3,"分配记录"),
    ASSIGNMENT_DETAIL(4,"分配详细记录");


    /**
     * 渠道类型
     */
    private Integer type;

    /**
     * 描述
     */
    private String desc;
}
