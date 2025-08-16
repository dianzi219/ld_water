package com.fastbee.notify.core.wechat.service;

import com.fastbee.common.core.notify.NotifySendResponse;
import com.fastbee.notify.core.wechat.vo.WeChatMiniPushVO;
import com.fastbee.notify.core.wechat.vo.WxMssVo;
import com.fastbee.notify.domain.NotifyChannel;
import com.fastbee.notify.domain.NotifyTemplate;
import com.fastbee.notify.vo.NotifyVO;

/**
 * @description: 微信通知推送业务类
 * @author fastb
 * @date 2023-12-29 16:39
 * @version 1.0
 */
public interface WeChatPushService {

    /**
     * 统一发送接口
     * @param notifyVO 发送参数
     * @return
     */
    NotifySendResponse send(NotifyVO notifyVO);

    /**
     * @description: 推送消息给指定的用户 --微信小程序服务号推送
     * @param: wxMssVo
     * @param: url
     * @return: java.lang.String
     */
    NotifySendResponse weChatPostPush(String json, String url);

    /**
     * @description: 生成微信小程序基本推送参数
     * @param: notifyTemplateId
     * @return: com.fastbee.notify.core.wechat.vo.WeChatMiniPushVO
     */
    WeChatMiniPushVO createWeChatMiniPushVO(NotifyChannel notifyChannel, NotifyTemplate notifyTemplate, Long userId);

}

