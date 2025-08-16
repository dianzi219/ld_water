package com.fastbee.iot.model;

import lombok.Data;

/**
 * @author fastb
 * @date 2023-10-18 10:23
 */
@Data
public class ThingsModelSimVO {

    private Long productId;

    private String identifier;

    private String modelName;

    private Integer thingsModelType;

    private String datatype;
}
