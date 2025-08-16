package com.fastbee.notify.core.vo;

import lombok.Data;

/**
 * @author fastb
 * @version 1.0
 * @description: 通知测试传参
 * @date 2023-12-28 15:15
 */
@Data
public class SendParams {

    /**
     * 模板编号
     */
    private Long id;
    /**
     * 发送账号：手机号、邮箱、用户id
     */
    private String sendAccount;
    /**
     * 模板内容变量json字符串
     */
    private String variables;
}
