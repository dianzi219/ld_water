package com.fastbee.notify.core.sms.service;

import com.fastbee.common.core.notify.NotifySendResponse;
import com.fastbee.notify.vo.NotifyVO;
import org.dromara.sms4j.api.SmsBlend;
import org.dromara.sms4j.api.entity.SmsResponse;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 短信发送接口
 * @author gsb
 * @date 2023/12/15 11:01
 */
public interface ISmsService {

    /**
     * 根据模版统一发送短信
     * @param notifyVO 发送类
     * @return com.fastbee.common.core.notify.NotifySendResponse
     */
    NotifySendResponse send(NotifyVO notifyVO);


    /**
     * 单个电话发送短信
     * @param phone 电话
     * @param message 模板内容
     * @return 结果
     */
    SmsResponse sendMessage(SmsBlend smsBlend, String phone, String message);

    /**
     * 根据模板id发送 --多参数
     * @param phone 电话
     * @param templateId 模板id
     * @param messages   内容集合
     * @return 结果
     */
    SmsResponse sendMessage(SmsBlend smsBlend, String phone, String templateId, LinkedHashMap<String,String> messages);

    /**
     * 群发短信
     * @param phones 电话集合
     * @param templateId 模板id
     * @param messages   内容集合
     * @return 结果
     */
    SmsResponse massTexting(SmsBlend smsBlend, List<String> phones, String templateId, LinkedHashMap<String,String> messages);

    /**
     * 延迟发送
     * @param phone 电话
     * @param message 模板内容
     * @param delayedTime 延迟时间
     */
    void delayedMessage(SmsBlend smsBlend,String phone ,String message,Long delayedTime);

    /**
     * 根据模板延迟发送
     * @param phone 电话
     * @param messages 模板内容集合
     * @param delayedTime 延迟时间
     */
    void delayedMessage(SmsBlend smsBlend, String phone ,String templateId, LinkedHashMap<String,String> messages,Long delayedTime);

}
