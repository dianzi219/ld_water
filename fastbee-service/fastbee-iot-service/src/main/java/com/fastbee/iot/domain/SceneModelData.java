package com.fastbee.iot.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;

/**
 * 【请填写功能名称】对象 scene_model_data
 *
 * @author kerwincui
 * @date 2024-05-21
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "SceneModelData", description = "场景变量")
public class SceneModelData extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键id */
    private Long id;

    /** 场景管理id */
    @ApiModelProperty(value = "场景管理id")
    private Long sceneModelId;

    /** 场景关联数据来源id */
    @ApiModelProperty(value = "场景关联数据来源id")
    private Long sceneModelDeviceId;

    /** 来源类型(0设备 1录入型 2运算型) */
    @ApiModelProperty(value = "来源类型(0设备 1录入型 2运算型)")
    private Integer variableType;

    /** 数据源id */
    @ApiModelProperty(value = "物模型或变量id")
    private Long datasourceId;

    /** 启用（0未启用 1启用） */
    @ApiModelProperty(value = "启用", notes = "0=未启用,1=启用")
    private Integer enable;

    /** 删除标志（0代表存在 1代表删除） */
    private String delFlag;

    /**
     * 物模型或变量名称
     */
    @ApiModelProperty(value = "变量名称")
    private String sourceName;

    /**
     * 标识符
     */
    @ApiModelProperty(value = "变量标识符")
    private String identifier;

    /**
     * 物模型类型
     */
    @ApiModelProperty(value = "物模型类型")
    private Integer type;

    /**
     * 关联设备名称
     */
    private String sceneModelDeviceName;
}
