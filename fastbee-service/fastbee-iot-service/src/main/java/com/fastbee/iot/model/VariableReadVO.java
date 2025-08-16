package com.fastbee.iot.model;

import lombok.Data;

/**
 * 变量读取
 * @author bill
 */
@Data
public class VariableReadVO {

    /**
     * 单个读取 type=1 全部读取:type=2
     */
    private Integer type;
    /**
     * 设备编号 如果是网关设备:单个读取传子设备，全部读取传网关编号
     */
    private String serialNumber;

    /**
     * 单个读取时的标识符
     */
    private String identifier;

    /**
     * 网关设备编号
     */
    private String parentSerialNumber;
}
