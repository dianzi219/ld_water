package com.fastbee.system.domain;

import com.fastbee.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * app语言对象 app_language
 */
public class AppLanguage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 语言 */
    @ApiModelProperty(value = "语言")
    private String language;

    /** 国家 */
    @ApiModelProperty(value = "国家")
    private String country;

    /** 时区 */
    @ApiModelProperty(value = "时区")
    private String timeZone;

    /** 语言名称 */
    @ApiModelProperty(value = "语言名称")
    private String langName;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }
    public void setLanguage(String language)
    {
        this.language = language;
    }

    public String getLanguage()
    {
        return language;
    }
    public void setCountry(String country)
    {
        this.country = country;
    }

    public String getCountry()
    {
        return country;
    }
    public void setTimeZone(String timeZone)
    {
        this.timeZone = timeZone;
    }

    public String getTimeZone()
    {
        return timeZone;
    }
    public void setLangName(String langName)
    {
        this.langName = langName;
    }

    public String getLangName()
    {
        return langName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("language", getLanguage())
            .append("country", getCountry())
            .append("timeZone", getTimeZone())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("langName", getLangName())
            .toString();
    }
}
