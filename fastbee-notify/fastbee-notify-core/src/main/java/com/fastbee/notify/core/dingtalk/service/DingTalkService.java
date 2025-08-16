package com.fastbee.notify.core.dingtalk.service;

import com.fastbee.common.core.notify.NotifySendResponse;
import com.fastbee.notify.vo.NotifyVO;

/**
 * 钉钉通知服务类
 * @author fastb
 * @date 2024-01-12 17:57
 * @version 1.0
 */
public interface DingTalkService {

    /** 
     * 钉钉统一发送方法
     * @param notifyVO 通知参数
     * @return com.fastbee.common.core.notify.NotifySendResponse
     */ 
    NotifySendResponse send(NotifyVO notifyVO);

}
