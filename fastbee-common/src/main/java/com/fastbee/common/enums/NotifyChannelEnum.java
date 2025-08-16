package com.fastbee.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description: 通知渠道枚举
 * @author fastb
 * @date 2023-12-16 17:00
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public enum NotifyChannelEnum {

    /**
     * 确保唯一，不能重复
     */
    SMS("sms", "短信"),
    VOICE("voice","语音"),
    WECHAT("wechat","微信"),
    DING_TALK("dingtalk","钉钉"),
    EMAIL("email", "邮箱");


    /**
     * 渠道类型
     */
    private String type;

    /**
     * 描述
     */
    private String desc;

    public static NotifyChannelEnum getNotifyChannelEnum(String type) {
        for (NotifyChannelEnum notifyChannelEnum : NotifyChannelEnum.values()) {
            if (type.equals(notifyChannelEnum.type)) {
                return notifyChannelEnum;
            }
        }
        return null;
    }

}
