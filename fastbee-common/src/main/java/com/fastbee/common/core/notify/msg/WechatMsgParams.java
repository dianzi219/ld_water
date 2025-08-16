package com.fastbee.common.core.notify.msg;

import lombok.Data;

/**
 * @author fastb
 * @version 1.0
 * @description: 微信消息模板参数
 * @date 2023-12-22 10:57
 */
@Data
public class WechatMsgParams {

    /**
     * 发送账号
     */
    private String sendAccount;

    /**
     * 模版id
     */
    private String templateId;

    /**
     * 内容
     */
    private String content;

    /**
     * 跳转链接
     */
    private String redirectUrl;

    /**
     * 跳转小程序appid
     */
    private String appid;

    /**
     * 小程序跳转路径
     */
    private String pagePath;
}
