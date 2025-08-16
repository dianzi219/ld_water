package com.fastbee.notify.core.voice.service;

import com.fastbee.common.core.notify.NotifySendResponse;
import com.fastbee.notify.vo.NotifyVO;

/**
 * @description: 语音通知服务类
 * @author fastb
 * @date 2023-12-15 11:05
 * @version 1.0
 */
public interface VoiceService {

    /**
     * 语音发送
     * @param notifyVO 发送参数
     * @return
     */
    NotifySendResponse send(NotifyVO notifyVO);
}
