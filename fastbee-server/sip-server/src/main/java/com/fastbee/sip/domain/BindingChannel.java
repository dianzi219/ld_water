package com.fastbee.sip.domain;

import lombok.Data;

/**
 * @author bill
 */
@Data
public class BindingChannel {

    /**
     * 监控设备ID
     */
    private String channelId;

    /**
     * 设备id
     */
    private Long deviceId;

    /**
     * 场景id
     */
    private Long sceneModelId;

    private String deviceName;
    private String sceneModelName;
}
