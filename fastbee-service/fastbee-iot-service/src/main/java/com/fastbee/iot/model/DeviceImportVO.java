package com.fastbee.iot.model;

import com.fastbee.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author fastb
 * @version 1.0
 * @description: 设备导入VO类
 * @date 2024-03-25 17:23
 */
@Data
public class DeviceImportVO {

    /** 设备名称 */
    @ApiModelProperty("设备名称")
    @Excel(name = "设备名称")
    private String deviceName;

    /** 设备编号 */
    @ApiModelProperty("设备编号")
    @Excel(name = "设备编号")
    private String serialNumber;

}
