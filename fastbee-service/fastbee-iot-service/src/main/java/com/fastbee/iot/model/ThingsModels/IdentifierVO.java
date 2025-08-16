package com.fastbee.iot.model.ThingsModels;

import lombok.Data;

/**
 * @author fastb
 * @version 1.0
 * @description: 物模型标识VO
 * @date 2024-06-06 10:31
 */
@Data
public class IdentifierVO {

    /**
     * 原标识，对应产品和redis物模标识
     */
    private String identifier;

    /**
     * 值索引
     */
    private Integer index;

    /**
     * 值
     */
    private String value;
}
