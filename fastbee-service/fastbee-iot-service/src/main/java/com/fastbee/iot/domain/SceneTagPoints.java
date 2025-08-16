package com.fastbee.iot.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;

/**
 * 运算型变量点对象 scene_tag_points
 *
 * @author kerwincui
 * @date 2024-05-21
 */
@Getter
@Setter
public class SceneTagPoints extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 运算型变量点id */
    private Long id;

    /** 变量点名称 */
    @ApiModelProperty(value = "变量点名称")
    private String name;

    /** 点别名，如 A */
    @ApiModelProperty(value = "点别名，如 A")
    private String alias;

    /** 关联的变量id */
    @ApiModelProperty(value = "关联的变量id")
    private Long tagId;

    /** 统计方式 ，用字典定义，暂时是”原值“ */
    @ApiModelProperty(value = "统计方式 ，用字典定义，暂时是”原值“")
    private Integer operation;

    /** 数据来源方式 1-设备物模型 2-录入型变量 3-运算型变量 */
    @ApiModelProperty(value = "数据来源方式 1-设备物模型 2-录入型变量 3-运算型变量")
    private Integer variableType;

    /** 场景变量id */
    @ApiModelProperty(value = "场景变量id")
    private Long sceneModelDataId;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /**
     * 场景关联设备id
     */
    private Long sceneModelDeviceId;

    /**
     * 场景关联设备名称
     */
    private String sceneModelDeviceName;

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("name", getName())
            .append("alias", getAlias())
            .append("tagId", getTagId())
            .append("operation", getOperation())
            .append("variableType", getVariableType())
            .append("sceneModelDataId", getSceneModelDataId())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
