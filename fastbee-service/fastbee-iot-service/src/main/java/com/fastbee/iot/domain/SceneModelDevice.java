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
 * 场景管理关联设备对象 scene_model_device
 *
 * @author kerwincui
 * @date 2024-05-21
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "SceneModelDevice", description = "场景关联设备")
public class SceneModelDevice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键id */
    private Long id;

    /** 场景id */
    @ApiModelProperty(value = "场景id")
    private Long sceneModelId;

    /** 关联设备id */
    @ApiModelProperty(value = "关联设备id")
    private Long cusDeviceId;

    /** 排序 */
    @ApiModelProperty(value = "排序")
    private Integer sort;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 类型（0设备 1录入型 2运算型） */
    @ApiModelProperty(value = "类型", notes = "0=设备,1=录入型,2=运算型")
    private Integer variableType;

    /** 全部启用（0否 1是） */
    @ApiModelProperty(value = "全部启用", notes = "0=否,1=是")
    private Integer allEnable;

    /** 名称 */
    @ApiModelProperty(value = "名称")
    private String name;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 产品id
     */
    private Long productId;

    /**
     * 设备编号
     */
    private String serialNumber;

}
