package com.fastbee.iot.model;

import lombok.Data;

import java.util.List;

/**
 * @author fastb
 * @version 1.0
 * @description: 告警用户VO类
 * @date 2024-05-15 16:16
 */
@Data
public class DeviceAlertUserVO {

    private Long deviceId;

    private List<Long> userIdList;
}
