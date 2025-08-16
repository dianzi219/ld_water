package com.fastbee.notify.core.service;

import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.core.notify.AlertPushParams;
import com.fastbee.notify.core.vo.SendParams;
import com.fastbee.notify.vo.NotifyVO;

/**
 * @description: 所有通知发送入口
 * 以后有通知业务可写在这里
 * @author fastb
 * @date 2023-12-26 15:47
 * @version 1.0
 */
public interface NotifySendService {

    /**
     * @description: 通知测试发送接口
     * @param: sendParams
     * @return: void
     */
    AjaxResult send(SendParams sendParams);

    /**
     * @description: 通知统一发送方法
     * @param: notifyVO 通知发送参数VO
     * @return: void
     */
    AjaxResult notifySend(NotifyVO notifyVO);

    /**
     * 告警通知统一推送
     * @param alertPushParams 告警推送参数
     * @return void
     */
    void alertSend(AlertPushParams alertPushParams);

    /**
     * @description: 发送短信验证码
     * @author fastb
     * @date 2023-12-26 15:49
     * @version 1.0
     */
    void sendCaptchaSms(String phone, String captcha);

    /**
     * @description: 根据业务编码、渠道、服务商获取唯一启用通知配置信息
     * @param serviceCode 业务编码，一定传
     * @param channelType 渠道类型 一定传
     * @param provider 钉钉和微信渠道 一定传
     * @author fastb
     * @date 2024-01-02 11:13
     */
    NotifyVO selectOnlyEnable(String serviceCode, String channelType, String provider, Long tenantId);

    /**
     * @description:  短信登录获取验证码
     * @param: phoneNumber
     * @return: com.fastbee.common.core.domain.AjaxResult
     */
    AjaxResult smsLoginCaptcha(String phoneNumber);

    /**
     * 企业微信验证url有效性
     * @param msgSignature
     * @param: timestamp
     * @param: nonce
     * @param: echostr
     * @param: response
     * @return void
     */
    String weComVerifyUrl(String msgSignature, String timestamp, String nonce, String echostr);

    /**
     * @description: 短信注册获取验证码
     * @param: phoneNumber 手机号
     * @return: com.fastbee.common.core.domain.AjaxResult
     */
    AjaxResult smsRegisterCaptcha(String phoneNumber);
}
