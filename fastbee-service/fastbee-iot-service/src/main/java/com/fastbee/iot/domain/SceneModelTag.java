package com.fastbee.iot.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 场景录入型变量对象 scene_model_tag
 *
 * @author kerwincui
 * @date 2024-05-21
 */
@Getter
@Setter
@ApiModel(value = "SceneModelTag", description = "场景录入运算变量")
public class SceneModelTag extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键id */
    private Long id;

    /** 场景id */
    @ApiModelProperty(value = "场景id")
    private Long sceneModelId;

    /** 录入型变量名 */
    @ApiModelProperty(value = "录入型变量名")
    private String name;

    /** 单位 */
    @ApiModelProperty(value = "单位")
    private String unit;

    /** 数据类型 */
    @ApiModelProperty(value = "数据类型")
    private String dataType;

    /** 默认值 */
    @ApiModelProperty(value = "默认值")
    private String defaultValue;

    /** 是否只读 0-否 1-是 */
    @ApiModelProperty(value = "是否只读 0-否 1-是")
    private Integer isReadonly;

    /** 存储方式 0-不存储 1-存储 */
    @ApiModelProperty(value = "存储方式 0-不存储 1-存储")
    private Integer storage;

    /** 变量类型 1-运算型变量 2-输入型变量 */
    @ApiModelProperty(value = "变量类型 1-设备物模型 2-运算型变量 3-输入型变量")
    private Integer variableType;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 周期执行（0未执行 1执行） */
    @ApiModelProperty(value = "周期执行", notes = "0=未执行,1=执行")
    private Integer cycleExecuted;

    /** 计算公式 ${id} + ${id} */
    @ApiModelProperty(value = "计算公式 ${id} + ${id}")
    private String formule;

    /** 显示的计算公式  A+B */
    @ApiModelProperty(value = "显示的计算公式  A+B")
    private String aliasFormule;

    /** 时间周期方式 1-周期计算 2-自定义时间段 */
    @ApiModelProperty(value = "时间周期方式 1-周期计算 2-自定义时间段")
    private Integer cycleType;

    /** 时间周期内容 */
    @ApiModelProperty(value = "时间周期内容")
    private String cycle;

    /**
     * 场景关联设备id
     */
    private Long sceneModelDeviceId;

    /**
     * 变量运算
     */
    private List<SceneTagPoints> tagPointsList = new ArrayList<>();

    /**
     * 启用
     */
    private Integer enable;

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("sceneModelId", getSceneModelId())
            .append("name", getName())
            .append("unit", getUnit())
            .append("dataType", getDataType())
            .append("defaultValue", getDefaultValue())
            .append("isReadonly", getIsReadonly())
            .append("storage", getStorage())
            .append("variableType", getVariableType())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("cycleExecuted", getCycleExecuted())
            .append("formule", getFormule())
            .append("aliasFormule", getAliasFormule())
            .append("cycleType", getCycleType())
            .append("cycle", getCycle())
            .toString();
    }
}
