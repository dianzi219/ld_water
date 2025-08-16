package com.fastbee.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description: 通知业务编码枚举
 * @author fastb
 * @date 2023-12-16 17:00
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public enum NotifyServiceCodeEnum {

    /**
     * 确保唯一，不能重复
     */
    ALERT("alert", "设备告警"),
    CAPTCHA("captcha","验证码"),
    MARKETING("marketing", "营销通知");


    /**
     * 业务编码
     */
    private String serviceCode;

    /**
     * 描述
     */
    private String desc;

    public static NotifyServiceCodeEnum getNotifyServiceCodeEnum(String serviceCode) {
        for (NotifyServiceCodeEnum notifyServiceCodeEnum : NotifyServiceCodeEnum.values()) {
            if (serviceCode.equals(notifyServiceCodeEnum.serviceCode)) {
                return notifyServiceCodeEnum;
            }
        }
        return null;
    }

}
