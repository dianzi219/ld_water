package com.fastbee.common.core.notify.msg;

import lombok.Data;

/**
 * @author fastb
 * @version 1.0
 * @description: 企业微信消息模板参数
 * @date 2023-12-22 10:57
 */
@Data
public class WeComMsgParams {

    /**
     * 发送账号
     */
    private String sendAccount;

    /**
     * 消息类型
     */
    private String msgType;
    /**
     * 消息内容
     */
    private String content;
    /**
     * 消息标题
     */
    private String title;
    /**
     * 消息描述
     */
    private String description;
    /**
     * 跳转链接
     */
    private String url;
    /**
     * 图片链接
     */
    private String picUrl;
}
