package com.fastbee.iot.model.modbus;

import lombok.Data;

/**
 * @author bill
 */
@Data
public class ModbusAndThingsVO {

    /**物模型id*/
    private Long modelId;
    /**
     * 物模型名称
     */
    private String modelName;
    /**
     * 标识符
     */
    private String identifier;
    /**
     * 是否可选 0-可选 1-不可选
     */
    private Boolean isSelect;

    /**
     * IO寄存器是否可选 0-可选 1-不可选
     */
    private Boolean isSelectIo;
    /**
     * 数据类型
     */
    private String dataType;
    /**
     * 数据寄存器是否可选 0-可选 1-不可选
     */
    private Boolean isSelectData;
}
