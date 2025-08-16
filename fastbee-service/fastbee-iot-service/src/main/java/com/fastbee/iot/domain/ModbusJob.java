package com.fastbee.iot.domain;

import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 轮训任务列对象 iot_modbus_job
 *
 * @author kerwincui
 * @date 2024-07-05
 */
@ApiModel(value = "ModbusJob", description = "轮训任务列 iot_modbus_job")
@Data
@EqualsAndHashCode(callSuper = true)
public class ModbusJob extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 任务id
     */
    private Long taskId;


    @ApiModelProperty("任务名")
    private String jobName;
    /**
     * 子设备id
     */
    @Excel(name = "子设备id")
    @ApiModelProperty("子设备id")
    private Long subDeviceId;

    /**
     * 子设备编号
     */
    @Excel(name = "子设备编号")
    @ApiModelProperty("子设备编号")
    private String subSerialNumber;

    /**
     * 指令
     */
    @Excel(name = "指令")
    @ApiModelProperty("指令")
    private String command;

    /**
     * 任务id
     */
    @Excel(name = "任务id")
    @ApiModelProperty("任务id")
    private Long jobId;

    /**
     * 状态（0正常 1暂停）
     */
    @Excel(name = "状态", readConverterExp = "0=正常,1=暂停")
    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("设备类型")
    private Integer deviceType;

    private List<String> subDeviceList;

    /**
     * 时间周期描述
     */
    private String remarkStr;


}
