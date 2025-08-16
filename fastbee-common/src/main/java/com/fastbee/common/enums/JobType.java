package com.fastbee.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum JobType {
    //1==设备定时，2=设备告警，3=场景联动 4=规则引擎
    Device(1),
    DeviceAlert(2),
    Scene(3),
    RuleEngine(4);
    private final Integer value;

    public static JobType fromValue(Integer value) {
        for (JobType type : JobType.values()) {
            if (Objects.equals(type.getValue(), value)) {
                return type;
            }
        }
        return null;
    }

    public static String getName(Integer value) {
        for (JobType type : JobType.values()) {
            if (Objects.equals(type.getValue(), value)) {
                return type.name();
            }
        }
        return null;
    }
}
