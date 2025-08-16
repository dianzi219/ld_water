package com.fastbee.iot.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 设备告警对象 iot_alert_log
 *
 * @author kerwincui
 * @date  2022-01-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "AlertLog", description = "设备告警日志实体 iot_alert_log")
public class AlertLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 告警ID */
    @ApiModelProperty("告警ID")
    private Long alertLogId;

    /** 告警名称 */
    @ApiModelProperty("告警名称")
    @Excel(name = "告警名称")
    private String alertName;

    /** 告警级别（1=提醒通知，2=轻微问题，3=严重警告，4=场景联动） */
    @ApiModelProperty("告警级别（1=提醒通知，2=轻微问题，3=严重警告，4=场景联动）")
    @Excel(name = "告警级别", readConverterExp = "1==提醒通知，2=轻微问题，3=严重警告，4=场景联动")
    private Long alertLevel;

    /** 处理状态(1=不需要处理,2=未处理,3=已处理) */
    @ApiModelProperty("处理状态(1=不需要处理,2=未处理,3=已处理)")
    @Excel(name = "处理状态(1=不需要处理,2=未处理,3=已处理)")
    private Integer status;

    /** 产品ID */
    @ApiModelProperty("产品ID")
    @Excel(name = "产品ID")
    private Long productId;

    /** 设备编号 */
    @ApiModelProperty("设备编号")
    private String serialNumber;

    /** 告警详情 */
    @ApiModelProperty("告警详情")
    private String detail;

    private Long userId;

    private String deviceName;

    private Long deviceId;
    private Long sceneId;
    private String sceneName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date beginTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    private Long deptUserId;
}
