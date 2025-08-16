package com.fastbee.iot.model;

import lombok.Data;

@Data
public class DeviceAlertCount {
    private String serialNumber;
    private String sceneId;
    private Integer alertCount;
    private Integer noprocessedCount;
    private Integer processedCount;
    private Integer unprocessedCount;
}
