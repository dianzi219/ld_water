package com.fastbee.iot.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;

/**
 * 告警通知模版关联对象 iot_alert_notify_template
 * 
 * @author kerwincui
 * @date 2024-01-29
 */
public class AlertNotifyTemplate extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 告警id */
    @Excel(name = "告警id")
    private Long alertId;

    /** 通知模版id */
    @Excel(name = "通知模版id")
    private Long notifyTemplateId;

    public void setAlertId(Long alertId) 
    {
        this.alertId = alertId;
    }

    public Long getAlertId() 
    {
        return alertId;
    }
    public void setNotifyTemplateId(Long notifyTemplateId) 
    {
        this.notifyTemplateId = notifyTemplateId;
    }

    public Long getNotifyTemplateId() 
    {
        return notifyTemplateId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("alertId", getAlertId())
            .append("notifyTemplateId", getNotifyTemplateId())
            .toString();
    }
}
