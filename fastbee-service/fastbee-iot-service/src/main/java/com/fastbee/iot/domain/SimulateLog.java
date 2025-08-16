package com.fastbee.iot.domain;

import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;
import com.fastbee.common.core.mq.message.MqttBo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 模拟设备日志对象 iot_simulate_log
 *
 * @author kerwincui
 * @date 2023-04-06
 */
@ApiModel(value = "SimulateLog", description = "模拟设备日志对象 iot_simulate_log")
@EqualsAndHashCode(callSuper = true)
@Data
public class SimulateLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    @ApiModelProperty("主键ID")
    private Long logId;

    /** 云端发送指令 */
    @ApiModelProperty("云端发送指令")
    @Excel(name = "云端发送指令")
    private String sendData;

    /** 设备回复 */
    @ApiModelProperty("设备回复")
    @Excel(name = "设备回复")
    private String callbackData;

    /** 设备ID */
    @ApiModelProperty("设备ID")
    @Excel(name = "设备ID")
    private Long deviceId;

    /** 设备名称 */
    @ApiModelProperty("设备名称")
    @Excel(name = "设备名称")
    private String deviceName;

    /** 设备编号 */
    @ApiModelProperty("设备编号")
    @Excel(name = "设备编号")
    private String serialNumber;

    private MqttBo send;
    private MqttBo receive;

}