package com.fastbee.notify.core.email.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.core.notify.NotifySendResponse;
import com.fastbee.common.core.notify.config.EmailConfigParams;
import com.fastbee.common.core.notify.msg.EmailMsgParams;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.notify.core.email.config.EmailNotifyConfig;
import com.fastbee.notify.core.email.service.EmailService;
import com.fastbee.notify.domain.NotifyChannel;
import com.fastbee.notify.domain.NotifyTemplate;
import com.fastbee.notify.vo.NotifyVO;
import org.dromara.email.api.MailClient;
import org.dromara.email.comm.entity.MailMessage;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author fastb
 * @version 1.0
 * @description: 邮箱发送业务类
 * @date 2023-12-20 10:21
 */
@Service
public class EmailServiceImpl implements EmailService {


    public MailClient createMailClient(NotifyChannel notifyChannel, NotifyTemplate notifyTemplate) {
        // 构建邮箱配置对象，key为 provider_id
        String mailClientKey = notifyChannel.getProvider() + "_" + notifyTemplate.getId();
        EmailConfigParams emailNotifyConfig = JSONObject.parseObject(notifyChannel.getConfigContent(), EmailConfigParams.class);
        return EmailNotifyConfig.create(mailClientKey, emailNotifyConfig);
    }

    @Override
    public NotifySendResponse send(NotifyVO notifyVO) {
        NotifySendResponse notifySendResponse = new NotifySendResponse();
        if (StringUtils.isEmpty(notifyVO.getSendAccount())) {
            notifySendResponse.setStatus(0);
            notifySendResponse.setResultContent("发送邮箱号为空，请先配置发送邮箱号！");
            return notifySendResponse;
        }
        MailClient mailClient;
        try {
            mailClient = this.createMailClient(notifyVO.getNotifyChannel(), notifyVO.getNotifyTemplate());
        } catch (Exception e) {
            notifySendResponse.setStatus(0);
            notifySendResponse.setResultContent("获取邮箱发送类失败，" + e);
            return notifySendResponse;
        }
        NotifyTemplate notifyTemplate = notifyVO.getNotifyTemplate();
        EmailMsgParams emailMsgParams = JSONObject.parseObject(notifyTemplate.getMsgParams(), EmailMsgParams.class);
        // 目前附件就支持一个
        Map<String, String> filesMap = new HashMap<>(2);
        if (StringUtils.isNotEmpty(emailMsgParams.getAttachment())) {
            String fileName = emailMsgParams.getAttachment().substring(emailMsgParams.getAttachment().lastIndexOf("/"));
            filesMap.put(fileName, emailMsgParams.getAttachment());
        }
        // 多个邮箱以,分隔
        List<String> mailList = StringUtils.str2List(notifyVO.getSendAccount(), ",", true, true);
        MailMessage mailMessage = MailMessage.Builder()
                .mailAddress(mailList)
                .title(emailMsgParams.getTitle())
                .html(new ByteArrayInputStream(emailMsgParams.getContent().getBytes()))
                .htmlValues(notifyVO.getMap())
                .files(filesMap)
                .build();
        try {
            mailClient.send(mailMessage);
        } catch (Exception e) {
            notifySendResponse.setStatus(0);
            notifySendResponse.setResultContent("邮箱发送失败，" + e);
            return notifySendResponse;
        }
        String sendContent = StringUtils.strReplaceVariable("#{", "}", emailMsgParams.getContent(), notifyVO.getMap());
        notifySendResponse.setSendContent(sendContent);
        notifySendResponse.setStatus(1);
        return notifySendResponse;
    }

}
