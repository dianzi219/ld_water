package com.fastbee.sip.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeviceChannelStatus {
    //1=-未使用，2-禁用，3-在线，4-离线
    notused(1),
    off(2),
    online(3),
    offline(4);
    private final Integer value;
}
