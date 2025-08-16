package com.fastbee.iot.model.gateWay;

import lombok.Data;

import java.util.List;

/**
 * @author admin
 * @version 1.0
 * @description: 网关产品新增VO类
 * @date 2024-09-04 16:11
 */
@Data
public class ProductSubGatewayAddVO {

    /** 网关产品id */
    private Long gwProductId;

    /**
     * 子产品id集合
     */
    private List<Long> subProductIds;
}
