package com.fastbee.oss.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;

/**
 * 对象存储配置对象 oss_config
 * 
 * @author zhuangpeng.li
 * @date 2024-04-19
 */
public class OssConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    private Integer id;

    /** 租户ID */
    @Excel(name = "租户ID")
    private Long tenantId;

    /** 租户名称 */
    @Excel(name = "租户名称")
    private String tenantName;

    /** 配置key */
    @Excel(name = "配置key")
    private String configKey;

    /** accessKey */
    @Excel(name = "accessKey")
    private String accessKey;

    /** 秘钥 */
    @Excel(name = "秘钥")
    private String secretKey;

    /** 桶名称 */
    @Excel(name = "桶名称")
    private String bucketName;

    /** 前缀 */
    @Excel(name = "前缀")
    private String prefix;

    /** 访问站点 */
    @Excel(name = "访问站点")
    private String endpoint;

    /** 自定义域名 */
    @Excel(name = "自定义域名")
    private String domain;

    /** 是否https（Y=是,N=否） */
    @Excel(name = "是否https", readConverterExp = "Y==是,N=否")
    private String isHttps;

    /** 域 */
    @Excel(name = "域")
    private String region;

    /** 桶权限类型(0=private 1=public 2=custom) */
    @Excel(name = "桶权限类型(0=private 1=public 2=custom)")
    private String accessPolicy;

    /** 是否默认（0=是,1=否） */
    @Excel(name = "是否默认", readConverterExp = "0==是,1=否")
    private String status;

    /** 扩展字段 */
    @Excel(name = "扩展字段")
    private String ext1;

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
    public void setConfigKey(String configKey) 
    {
        this.configKey = configKey;
    }

    public String getConfigKey() 
    {
        return configKey;
    }
    public void setAccessKey(String accessKey) 
    {
        this.accessKey = accessKey;
    }

    public String getAccessKey() 
    {
        return accessKey;
    }
    public void setSecretKey(String secretKey) 
    {
        this.secretKey = secretKey;
    }

    public String getSecretKey() 
    {
        return secretKey;
    }
    public void setBucketName(String bucketName) 
    {
        this.bucketName = bucketName;
    }

    public String getBucketName() 
    {
        return bucketName;
    }
    public void setPrefix(String prefix) 
    {
        this.prefix = prefix;
    }

    public String getPrefix() 
    {
        return prefix;
    }
    public void setEndpoint(String endpoint) 
    {
        this.endpoint = endpoint;
    }

    public String getEndpoint() 
    {
        return endpoint;
    }
    public void setDomain(String domain) 
    {
        this.domain = domain;
    }

    public String getDomain() 
    {
        return domain;
    }
    public void setIsHttps(String isHttps) 
    {
        this.isHttps = isHttps;
    }

    public String getIsHttps() 
    {
        return isHttps;
    }
    public void setRegion(String region) 
    {
        this.region = region;
    }

    public String getRegion() 
    {
        return region;
    }
    public void setAccessPolicy(String accessPolicy) 
    {
        this.accessPolicy = accessPolicy;
    }

    public String getAccessPolicy() 
    {
        return accessPolicy;
    }
    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }
    public void setExt1(String ext1) 
    {
        this.ext1 = ext1;
    }

    public String getExt1() 
    {
        return ext1;
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
            .append("configKey", getConfigKey())
            .append("accessKey", getAccessKey())
            .append("secretKey", getSecretKey())
            .append("bucketName", getBucketName())
            .append("prefix", getPrefix())
            .append("endpoint", getEndpoint())
            .append("domain", getDomain())
            .append("isHttps", getIsHttps())
            .append("region", getRegion())
            .append("accessPolicy", getAccessPolicy())
            .append("status", getStatus())
            .append("ext1", getExt1())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
