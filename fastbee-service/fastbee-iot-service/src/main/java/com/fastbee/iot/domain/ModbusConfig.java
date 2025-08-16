package com.fastbee.iot.domain;

import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;
import com.fastbee.common.core.protocol.modbus.ModbusCode;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * modbus配置对象 iot_modbus_config
 ** @date 2024-05-22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ModbusConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 业务id */
    private Long id;

    /**所属产品id*/
    @ApiModelProperty(value = "所属产品id")
    private Long productId;

    /**从机地址*/
    @ApiModelProperty(value = "从机地址")
    private Integer slave;
    /** 关联属性 */
    @ApiModelProperty(value = "关联属性")
    private String identifier;

    /** 寄存器地址 */
    @ApiModelProperty(value = "寄存器地址")
    private Integer address;

    /** 是否只读(0-否，1-是) */
    @ApiModelProperty(value = "是否只读(0-否，1-是)")
    private Integer isReadonly;

    /** modbus数据类型 */
    @ApiModelProperty(value = "modbus数据类型")
    private String dataType;

    /** 读取个数 */
    @ApiModelProperty(value = "读取个数")
    private Integer quantity;

    /** 寄存器类型 1-IO寄存器 2-数据寄存器 */
    @ApiModelProperty(value = "寄存器类型 1-IO寄存器 2-数据寄存器")
    private Integer type;

    @ApiModelProperty(value = "BIT位")
    private Integer bitOrder;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    private ModbusCode modbusCode;

}
