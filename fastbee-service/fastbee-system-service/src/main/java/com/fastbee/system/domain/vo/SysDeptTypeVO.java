package com.fastbee.system.domain.vo;

import lombok.Data;

/**
 * @author fastb
 * @version 1.0
 * @description: 机构类型VO
 * @date 2024-03-12 16:37
 */
@Data
public class SysDeptTypeVO {

    /**
     * 机构类型
     */
    private Integer deptType;

    /**
     * 机构类型名称
     */
    private String deptTypeName;

    /**
     * 祖级列表
     */
    private String ancestors;
}
