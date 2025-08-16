package com.fastbee.iot.domain;

import com.fastbee.iot.model.scenemodel.CusDeviceVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fastbee.common.core.domain.BaseEntity;

import java.util.List;

/**
 * 场景管理对象 scene_model
 *
 * @author kerwincui
 * @date 2024-05-20
 */
@Setter
@Getter
@ApiModel(value = "SceneModel", description = "场景管理")
public class SceneModel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 场景管理id */
    private Long sceneModelId;

    /** 所属租户id */
    @ApiModelProperty(value = "所属租户id")
    private Long tenantId;

    /** 场景管理名称 */
    @ApiModelProperty(value = "场景管理名称")
    private String sceneModelName;

    /** 场景状态 0-停用 1-启用 */
    @ApiModelProperty(value = "场景状态 0-停用 1-启用")
    private Integer status;

    /** 关联的组态id */
    @ApiModelProperty(value = "关联的组态id")
    private String guid;

    /** 场景描述 */
    @ApiModelProperty(value = "场景描述")
    private String desc;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /**
     * 场景图片
     */
    @ApiModelProperty(value = "场景图片")
    private String imgUrl;

    /**
     * 机构id
     */
    private Long deptId;

    /**
     * 机构名称
     */
    private String deptName;

    /**
     * 关联设备数
     */
    private Integer deviceTotal;

    /**
     * 关联设备名称
     */
    private List<CusDeviceVO> cusDeviceList;

    /**
     * 关联设备类型
     */
    private List<SceneModelDevice> sceneModelDeviceList;
    /**
     * 关联的监控设备
     */
    private List<SipRelation> sipRelationList;

    /**
     * 组态id
     */
    private Long scadaId;

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("sceneModelId", getSceneModelId())
            .append("tenantId", getTenantId())
            .append("sceneModelName", getSceneModelName())
            .append("status", getStatus())
            .append("guid", getGuid())
            .append("desc", getDesc())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
                .append("imgUrl", getImgUrl())
            .toString();
    }
}
