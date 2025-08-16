package com.fastbee.iot.domain;

import com.fastbee.notify.domain.NotifyTemplate;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;

import java.util.List;

/**
 * 设备告警对象 iot_alert
 *
 * @author kerwincui
 * @date 2022-01-13
 */
@ApiModel(value = "Alert", description = "设备告警实体 iot_alert")
public class Alert extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 告警ID
     */
    @ApiModelProperty("告警ID")
    private Long alertId;

    /**
     * 告警名称
     */
    @ApiModelProperty("告警名称")
    @Excel(name = "告警名称")
    private String alertName;

    /**
     * 告警级别（1=提醒通知，2=轻微问题，3=严重警告）
     */
    @ApiModelProperty("告警级别（1=提醒通知，2=轻微问题，3=严重警告）")
    @Excel(name = "告警级别", readConverterExp = "1==提醒通知，2=轻微问题，3=严重警告")
    private Long alertLevel;

    /**
     * 告警状态（1-启动，2-停止）
     */
    @Excel(name = "告警状态", readConverterExp = "1=-启动，2-停止")
    private Integer status;

    /**
     * 通知方式 格式："1,2,3"
     */
    @Excel(name = "通知方式")
    private String notify;

    private List<Scene> scenes;

    private List<NotifyTemplate> notifyTemplateList;

    /** 租户id */
    private Long tenantId;

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

    /** 租户名称 */
    private String tenantName;

    public List<NotifyTemplate> getNotifyTemplateList() {
        return notifyTemplateList;
    }

    public void setNotifyTemplateList(List<NotifyTemplate> notifyTemplateList) {
        this.notifyTemplateList = notifyTemplateList;
    }

    public List<Scene> getScenes() {
        return scenes;
    }

    public void setScenes(List<Scene> scenes) {
        this.scenes = scenes;
    }

    public Long getAlertId() {
        return alertId;
    }

    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }

    public String getAlertName() {
        return alertName;
    }

    public void setAlertName(String alertName) {
        this.alertName = alertName;
    }

    public Long getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(Long alertLevel) {
        this.alertLevel = alertLevel;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getNotify() {
        return notify;
    }

    public void setNotify(String notify) {
        this.notify = notify;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("alertId", getAlertId())
                .append("alertName", getAlertName())
                .append("alertLevel", getAlertLevel())
                .append("status", getStatus())
                .append("notify", getNotify())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
