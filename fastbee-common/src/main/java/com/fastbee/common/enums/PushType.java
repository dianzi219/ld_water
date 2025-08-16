package com.fastbee.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 推送类型
 * @author bill
 */
@Getter
@AllArgsConstructor
public enum PushType {

    WECHAT_SERVER_PUSH("wechat_server_push","微信小程序服务号推送");


    /**
     * 业务编号
     */
    private String serviceCode;

    private String desc;
}
