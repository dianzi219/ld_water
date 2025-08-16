package com.fastbee.iot.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;

/**
 * 监控设备关联对象 iot_sip_relation
 *
 * @author kerwincui
 * @date 2024-06-06
 */
@ApiModel(value = "SipRelation", description = "监控设备关联 iot_sip_relation")
@Data
@EqualsAndHashCode(callSuper = true)
public class SipRelation extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 业务id
     */
    private Long id;

    /**
     * 监控设备编号
     */
    @Excel(name = "监控设备编号")
    @ApiModelProperty("监控设备编号")
    private String channelId;

    @ApiModelProperty("通道名称")
    @Excel(name = "通道名称")
    private String channelName;

    @ApiModelProperty("产品型号")
    @Excel(name = "产品型号")
    private String model;

    /**
     * 关联的设备id
     */
    @Excel(name = "关联的设备id")
    @ApiModelProperty("关联的设备id")
    private Long reDeviceId;

    /**
     * 关联的场景id
     */
    @Excel(name = "关联的场景id")
    @ApiModelProperty("关联的场景id")
    private Long reSceneModelId;

    @ApiModelProperty("监控设备id")
    private Long deviceId;

    @ApiModelProperty("sip设备编号")
    private String deviceSipId;

    @ApiModelProperty("通道状态")
    private Integer status;

}
