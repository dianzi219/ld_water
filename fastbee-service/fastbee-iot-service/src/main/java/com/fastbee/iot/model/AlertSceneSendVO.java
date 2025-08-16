package com.fastbee.iot.model;

import lombok.Data;

/**
 * @author fastb
 * @version 1.0
 * @description: 场景告警通知VO类
 * @date 2024-01-29 17:11
 */
@Data
public class AlertSceneSendVO {

    /**
     * 场景ID
     */
    private Long sceneId;

    /**
     * 告警ID
     */
    private Long alertId;

    /**
     * 告警名称
     */
    private String alertName;

    /**
     * 告警级别（1=提醒通知，2=轻微问题，3=严重警告）
     */
    private Long alertLevel;


}
