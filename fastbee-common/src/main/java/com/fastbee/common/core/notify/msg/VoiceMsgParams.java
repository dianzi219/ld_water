package com.fastbee.common.core.notify.msg;

import lombok.Data;

/**
 * @author fastb
 * @version 1.0
 * @description: 语音消息模板参数
 * @date 2023-12-22 10:54
 */
@Data
public class VoiceMsgParams {

    /**
     * 发送账号
     */
    private String sendAccount;

    /**
     * 模板ID
     */
    private String templateId;

    /**
     * 内容
     */
    private String content;

    /**
     * 应用ID
     */
    private String sdkAppId;

    /**
     * 播放次数 1~3
     */
    private String playTimes = "1";

    /**
     * 播放音量 0-100
     */
    private String volume = "50";

    /**
     * 语速控制 -500-500
     */
    private String speed = "0";

}
