package com.fastbee.common.core.notify;

import lombok.Data;

/**
 * @author fastb
 * @version 1.0
 * @description: 通知发送响应类
 * @date 2024-01-11 16:06
 */
@Data
public class NotifySendResponse {

    /**
     * 发送结果 1-成功；0-失败
     */
    private Integer status = 0;

    /**
     * 返回结果内容
     */
    private String resultContent = "";

    /**
     * 发送内容，变量替换后的
     */
    private String sendContent = "";

    /**
     * 不是使用sendAccount账号发送，而是像钉钉这种，发送所有人或部门，记录这个
     */
    private String otherSendAccount = "";
}
