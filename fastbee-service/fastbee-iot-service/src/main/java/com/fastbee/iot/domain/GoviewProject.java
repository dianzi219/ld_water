package com.fastbee.iot.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;

/**
 * 项目对象 iot_goview_project
 *
 * @author kami
 * @date 2022-10-27
 */
@ApiModel(value = "GoviewProject", description = "项目对象 iot_goview_project")
public class GoviewProject extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @ApiModelProperty("主键ID")
    private String id;

    /** 项目名称 */
    @ApiModelProperty("项目名称")
    @Excel(name = "项目名称")
    private String projectName;

    /** 项目状态[-1未发布,1发布] */
    @ApiModelProperty(value = "项目状态", notes = "[-1未发布,1发布]")
    @Excel(name = "项目状态[0未发布,1发布]")
    private Integer state;

    /** 删除状态[1删除,-1未删除] */
    @ApiModelProperty("删除状态")
    private Long delFlag;

    /** 首页图片 */
    @ApiModelProperty("首页图片")
    @Excel(name = "首页图片")
    private String indexImage;

    /** 项目介绍 */
    @ApiModelProperty("项目介绍")
    @Excel(name = "项目介绍")
    private String remarks;

    /** 租户id */
    private Long tenantId;

    /** 租户名称 */
    private String tenantName;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }
    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }

    public String getProjectName()
    {
        return projectName;
    }
    public void setState(Integer state)
    {
        this.state = state;
    }

    public Integer getState()
    {
        return state;
    }
    public void setDelFlag(Long delFlag)
    {
        this.delFlag = delFlag;
    }

    public Long getDelFlag()
    {
        return delFlag;
    }
    public void setIndexImage(String indexImage)
    {
        this.indexImage = indexImage;
    }

    public String getIndexImage()
    {
        return indexImage;
    }
    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    public String getRemarks()
    {
        return remarks;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("projectName", getProjectName())
            .append("state", getState())
            .append("createTime", getCreateTime())
            .append("createBy", getCreateBy())
            .append("delFlag", getDelFlag())
            .append("indexImage", getIndexImage())
            .append("remarks", getRemarks())
            .toString();
    }
}
