package com.fastbee.oss.domain;

import lombok.Builder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;

/**
 * 文件记录对象 oss_detail
 *
 * @author zhuangpeng.li
 * @date 2024-04-22
 */
@Builder
public class OssDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 文件id */
    private Integer id;

    /** 租户ID */
    @Excel(name = "租户ID")
    private Long tenantId;

    /** 租户名称 */
    @Excel(name = "租户名称")
    private String tenantName;

    /** 文件名 */
    @Excel(name = "文件名")
    private String fileName;

    /** 原名 */
    @Excel(name = "原名")
    private String originalName;

    /** 文件后缀名 */
    @Excel(name = "文件后缀名")
    private String fileSuffix;

    /** URL地址 */
    @Excel(name = "URL地址")
    private String url;

    /** 服务商 */
    @Excel(name = "服务商")
    private String service;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getId()
    {
        return id;
    }
    public void setTenantId(Long tenantId)
    {
        this.tenantId = tenantId;
    }

    public Long getTenantId()
    {
        return tenantId;
    }
    public void setTenantName(String tenantName)
    {
        this.tenantName = tenantName;
    }

    public String getTenantName()
    {
        return tenantName;
    }
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getFileName()
    {
        return fileName;
    }
    public void setOriginalName(String originalName)
    {
        this.originalName = originalName;
    }

    public String getOriginalName()
    {
        return originalName;
    }
    public void setFileSuffix(String fileSuffix)
    {
        this.fileSuffix = fileSuffix;
    }

    public String getFileSuffix()
    {
        return fileSuffix;
    }
    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getUrl()
    {
        return url;
    }
    public void setService(String service)
    {
        this.service = service;
    }

    public String getService()
    {
        return service;
    }
    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("tenantId", getTenantId())
                .append("tenantName", getTenantName())
                .append("fileName", getFileName())
                .append("originalName", getOriginalName())
                .append("fileSuffix", getFileSuffix())
                .append("url", getUrl())
                .append("service", getService())
                .append("delFlag", getDelFlag())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
