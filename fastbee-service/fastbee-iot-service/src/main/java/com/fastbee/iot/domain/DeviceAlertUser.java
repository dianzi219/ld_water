package com.fastbee.iot.domain;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fastbee.common.annotation.Excel;
import com.fastbee.common.core.domain.BaseEntity;

/**
 * 设备告警用户对象 iot_device_alert_user
 *
 * @author kerwincui
 * @date 2024-05-15
 */
@Data
public class DeviceAlertUser
{

    /** 设备id */
    private Long deviceId;

    /** 用户id */
    private Long userId;

    private String userName;

    private String phoneNumber;

}
