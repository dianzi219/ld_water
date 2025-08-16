package com.fastbee.notify.core.email.service;

import com.fastbee.common.core.notify.NotifySendResponse;
import com.fastbee.notify.vo.NotifyVO;

/**
 * @description: 邮箱发送业务类
 * @author fastb
 * @date 2023-12-29 16:20
 * @version 1.0
 */
public interface EmailService {

    /**
     * @description:  邮件简要内容发送
     * @param: notifyVO 发送VO类
     * @return: void
     */
    NotifySendResponse send(NotifyVO notifyVO);

}
