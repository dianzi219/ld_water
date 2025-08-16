package com.fastbee.iot.domain;

import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 网关与子设备关联对象 iot_gateway
 *
 * @author gsb
 * @date 2024-05-27
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SubGateway extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 业务id */
    private Long id;

    /** 网关设备id */
    @Excel(name = "网关设备id")
    @ApiModelProperty("网关设备id")
    private Long gwDeviceId;

    /** 子设备id */
    @Excel(name = "子设备id")
    @ApiModelProperty("子设备id")
    private Long subDeviceId;

    /** 从机地址 */
    @Excel(name = "从机地址")
    @ApiModelProperty("从机地址")
    private Integer slaveId;
    /**
     * 子设备名称
     */
    private String subDeviceName;
    /**
     * 子设备编号
     */
    private String subDeviceNo;

}
