package com.fastbee.system.domain;

import com.fastbee.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * APP用户偏好设置对象 app_preferences
 */
public class AppPreferences extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键id */
    private Long id;

    /** 用户 */
    @ApiModelProperty(value = "用户")
    private Long userId;

    /** 语言 */
    @ApiModelProperty(value = "语言")
    private String language;

    /** 时区 */
    @ApiModelProperty(value = "时区")
    private String timeZone;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }
    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getUserId()
    {
        return userId;
    }
    public void setLanguage(String language)
    {
        this.language = language;
    }

    public String getLanguage()
    {
        return language;
    }
    public void setTimeZone(String timeZone)
    {
        this.timeZone = timeZone;
    }

    public String getTimeZone()
    {
        return timeZone;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("userId", getUserId())
            .append("language", getLanguage())
            .append("timeZone", getTimeZone())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
