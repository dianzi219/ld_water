package com.fastbee.iot.model;

import com.fastbee.common.annotation.Excel;
import lombok.Data;

/**
 * @author admin
 * @version 1.0
 * @description: 设备编号VO类
 * @date 2024-07-19 11:59
 */
@Data
public class SerialNumberVO {

    @Excel(name = "设备编号")
    private String serialNumber;
}
