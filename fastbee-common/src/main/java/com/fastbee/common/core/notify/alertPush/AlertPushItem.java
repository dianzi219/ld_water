package com.fastbee.common.core.notify.alertPush;

import lombok.Data;

/**
 * 推送项item
 * @author bill
 */
@Data
public class AlertPushItem {

    /**
     * 告警时间
     */
    private String alertTime;
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
}
