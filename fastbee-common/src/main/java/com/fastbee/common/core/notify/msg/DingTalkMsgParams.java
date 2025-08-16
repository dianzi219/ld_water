package com.fastbee.common.core.notify.msg;

import lombok.Data;

/**
 * @author fastb
 * @version 1.0
 * @description: 钉钉模版配置参数类
 * @date 2024-01-12 17:51
 */
@Data
public class DingTalkMsgParams {

    /**
     * 发送账号
     */
    private String sendAccount;

    /**
     * 是否发送所有人
     */
    private String sendAllEnable;

    /**
     * 发送什么类型的文本
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
     * 消息链接
     */
    private String messageUrl;

    /**
     * 图片链接
     */
    private String picUrl;

    /**
     * 所属部门id
     */
    private String deptId;
}
