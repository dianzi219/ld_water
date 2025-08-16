package com.fastbee.iot.model.modbus;

import com.fastbee.common.annotation.Excel;
import lombok.Data;

/**
 * modbusIO导入
 * @author gsb
 * @date 2024/5/27 10:20
 */
@Data
public class ModbusIoImport {

    /** 关联属性 */
    @Excel(name = "关联属性")
    private String identifier;

    /**从机地址*/
    @Excel(name = "从机地址")
    private Integer slave;

    /** 寄存器地址 */
    @Excel(name = "寄存器地址")
    private Integer address;

    /** 是否只读(0-否，1-是) */
    @Excel(name = "是否只读",readConverterExp = "0=否,1=是")
    private Integer isReadonly;


}
