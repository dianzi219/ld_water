package com.fastbee.common.core.notify;

import lombok.Data;

import java.util.Set;

/**
 * @author fastb
 * @version 1.0
 * @description: TODO
 * @date 2023-12-26 11:03
 */
@Data
public class AlertPushParams {

    /**
     * 通知模版id
     */
    private Long notifyTemplateId;
    /**
     * 告警时间
     */
    private String alertTime;
    /**
     * 设备名称
     */
    private String deviceName;
    /**
     * 设备编号
     */
    private String serialNumber;
    /**
     * 告警发生地点
     */
    private String address;
    /**
     * 告警名称
     */
    private String alertName;
    /**
     * 告警推送手机号
     */
    private Set<String> userPhoneSet;
    /**
     * 告警推送用户id
     */
    private Set<Long> userIdSet;
}
