package com.fastbee.iot.domain;

import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 设备告警对象 iot_alert_scene
 *
 * @author kerwincui
 * @date 2022-01-13
 */

public class AlertScene extends BaseEntity {

    /**
     * 告警ID
     */
    @ApiModelProperty("告警ID")
    private Long alertId;

    /**
     * 场景ID
     */
    @ApiModelProperty("场景ID")
    private Long sceneId;

    public Long getAlertId() {
        return alertId;
    }

    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }

    public Long getSceneId() {
        return sceneId;
    }

    public void setSceneId(Long sceneId) {
        this.sceneId = sceneId;
    }
}
