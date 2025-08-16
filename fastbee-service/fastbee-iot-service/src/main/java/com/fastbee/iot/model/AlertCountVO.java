package com.fastbee.iot.model;

import lombok.Data;

/**
 * @author fastb
 * @version 1.0
 * @description: 告警数量统计
 * @date 2024-06-17 10:09
 */
@Data
public class AlertCountVO {

    /**
     * 数量
     */
    private Integer count;

    /**
     * 类型  处理或告警类型
     */
    private Integer type;
}
