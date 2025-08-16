package com.fastbee.common.core.notify.msg;

import lombok.Data;

/**
 * @author fastb
 * @version 1.0
 * @description: 邮箱模板消息参数
 * @date 2023-12-22 10:47
 */
@Data
public class EmailMsgParams {

    /**
     * 发送账号
     */
    private String sendAccount;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 附件
     */
    private String attachment;
}
