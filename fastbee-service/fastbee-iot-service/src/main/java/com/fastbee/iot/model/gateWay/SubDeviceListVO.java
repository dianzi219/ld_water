package com.fastbee.iot.model.gateWay;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author gsb
 * @date 2024/5/29 17:31
 */
@Data
public class SubDeviceListVO {

   private Long id;

    /** 网关设备id */
    @ApiModelProperty("网关设备id")
    private Long gwDeviceId;

    /** 子设备id */
    @ApiModelProperty("子设备id")
    private Long subDeviceId;

    /** 从机地址 */
    @ApiModelProperty("从机地址")
    private Integer slaveId;
    /**
     * 子设备名称
     */
    @ApiModelProperty("子设备名称")
    private String subDeviceName;
    /**
     * 子设备编号
     */
    @ApiModelProperty("子设备编号")
    private String subDeviceNo;

    /** 创建时间 */
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private Long subProductId;
}
