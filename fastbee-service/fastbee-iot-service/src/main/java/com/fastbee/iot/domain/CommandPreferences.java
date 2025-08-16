package com.fastbee.iot.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;

/**
 * 指令偏好设置对象 command_preferences
 *
 * @author kerwincui
 * @date 2024-06-29
 */
@ApiModel(value = "CommandPreferences", description = "指令偏好设置 command_preferences")
@Data
@EqualsAndHashCode(callSuper = true)
public class CommandPreferences extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 指令id
     */
    private Long id;

    /**
     * 指令名称
     */
    @Excel(name = "指令名称")
    @ApiModelProperty("指令名称")
    private String name;

    /**
     * 指令
     */
    @Excel(name = "指令")
    @ApiModelProperty("指令")
    private String command;

    /**
     * 设备编号
     */
    @Excel(name = "设备编号")
    @ApiModelProperty("设备编号")
    private String serialNumber;


}
