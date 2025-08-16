package com.fastbee.notify.core.sms.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.core.notify.NotifySendResponse;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.notify.core.sms.config.ReadConfig;
import com.fastbee.notify.core.sms.service.ISmsService;
import com.fastbee.notify.vo.NotifyVO;
import org.dromara.sms4j.api.SmsBlend;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.dromara.sms4j.provider.config.BaseConfig;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/**
 * @author gsb
 * @date 2023/12/15 11:03
 */
@Service
public class SmsServiceImpl implements ISmsService {

    @Resource
    private ReadConfig config;

    @Override
    public NotifySendResponse send(NotifyVO notifyVO) {
        NotifySendResponse notifySendResponse = new NotifySendResponse();
        // 注意：因为配置参数是分开渠道和模版配的，所以这里需要先转换，再copy一下
        BaseConfig baseConfig = (BaseConfig) JSONObject.parseObject(notifyVO.getNotifyChannel().getConfigContent(), notifyVO.getNotifyChannelProviderEnum().getConfigContentClass());
        CopyOptions copyOptions = CopyOptions.create(null, true);
        BeanUtil.copyProperties(JSONObject.parseObject(notifyVO.getNotifyTemplate().getMsgParams(), notifyVO.getNotifyChannelProviderEnum().getMsgParamsClass()), baseConfig, copyOptions);
        JSONObject jsonMsgParams = JSONObject.parseObject(notifyVO.getNotifyTemplate().getMsgParams());
        String content = jsonMsgParams.get("content").toString();
        LinkedHashMap<String, String> map = notifyVO.getMap();
        String sendContent = "";
        switch (notifyVO.getNotifyChannelProviderEnum()) {
            case SMS_ALIBABA:
                sendContent = StringUtils.strReplaceVariable("${", "}", content, map);
                break;
            case SMS_TENCENT:
                sendContent = StringUtils.strReplaceVariable("{", "}", content, map);
                break;
            default:
                break;
        }
        notifySendResponse.setSendContent(sendContent);
        SmsBlend smsBlend = this.getSmsInstance(notifyVO.getNotifyTemplate().getId().toString());
        List<String> phoneList = StringUtils.str2List(notifyVO.getSendAccount(), ",", true, true);
        SmsResponse smsResponse = this.massTexting(smsBlend, phoneList, baseConfig.getTemplateId(), map);
        notifySendResponse.setStatus(smsResponse.isSuccess() ? 1 : 0);
        notifySendResponse.setResultContent(JSON.toJSONString(smsResponse.getData()));
        return notifySendResponse;
    }

    /**
     * 获取短信实例
     * @return
     */
    private SmsBlend getSmsInstance(String configId){
        SmsBlend smsBlend = SmsFactory.getSmsBlend(configId);
        if (Objects.isNull(smsBlend)){
            //如果没有初始化，则先进行初始化
            SmsFactory.createSmsBlend(config, configId);
            return SmsFactory.getSmsBlend(configId);
        }
        return smsBlend;
    }

    /**
     * 单个电话发送短信
     *
     * @param phone   电话
     * @param message 模板内容
     * @return 结果
     */
    @Override
    public SmsResponse sendMessage(SmsBlend smsBlend, String phone, String message) {
        return smsBlend.sendMessage(phone, message);
    }

    /**
     * 根据模板id发送 --多参数
     *
     * @param phone      电话
     * @param templateId 模板id
     * @param messages   内容集合
     * @return 结果
     */
    @Override
    public SmsResponse sendMessage(SmsBlend smsBlend, String phone, String templateId, LinkedHashMap<String, String> messages) {
        return smsBlend.sendMessage(phone, templateId, messages);
    }

    /**
     * 群发短信
     *
     * @param phones  电话集合
     * @param templateId 模板id
     * @param messages   内容集合
     * @return 结果
     */
    @Override
    public SmsResponse massTexting(SmsBlend smsBlend, List<String> phones, String templateId, LinkedHashMap<String,String> messages) {
        return smsBlend.massTexting(phones, templateId, messages);
    }

    /**
     * 延迟发送
     *
     * @param phone       电话
     * @param message     模板内容
     * @param delayedTime 延迟时间
     */
    @Override
    public void delayedMessage(SmsBlend smsBlend, String phone, String message, Long delayedTime) {
        smsBlend.delayedMessage(phone, message, delayedTime);
    }

    /**
     * 根据模板延迟发送
     *
     * @param phone       电话
     * @param messages    模板内容集合
     * @param delayedTime 延迟时间
     */
    @Override
    public void delayedMessage(SmsBlend smsBlend, String phone, String templateId, LinkedHashMap<String, String> messages, Long delayedTime) {
        smsBlend.delayedMessage(phone, templateId, messages, delayedTime);
    }

}
