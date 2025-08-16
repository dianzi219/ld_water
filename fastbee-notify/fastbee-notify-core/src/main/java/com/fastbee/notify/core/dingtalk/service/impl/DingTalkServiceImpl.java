package com.fastbee.notify.core.dingtalk.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.aliyun.dingtalkoauth2_1_0.Client;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenRequest;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.Common;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiMessageCorpconversationAsyncsendV2Request;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiMessageCorpconversationAsyncsendV2Response;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.fastbee.common.core.notify.NotifySendResponse;
import com.fastbee.common.core.notify.config.DingTalkConfigParams;
import com.fastbee.common.core.notify.msg.DingTalkMsgParams;
import com.fastbee.common.enums.NotifyChannelProviderEnum;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.notify.core.dingtalk.service.DingTalkService;
import com.fastbee.notify.domain.NotifyChannel;
import com.fastbee.notify.domain.NotifyTemplate;
import com.fastbee.notify.vo.NotifyVO;
import com.taobao.api.ApiException;
import org.springframework.stereotype.Service;

/**
 * @author fastb
 * @version 1.0
 * @description: 钉钉通知服务类
 * @date 2024-01-12 17:58
 */
@Service
public class DingTalkServiceImpl implements DingTalkService {


    @Override
    public NotifySendResponse send(NotifyVO notifyVO) {
        NotifySendResponse notifySendResponse = new NotifySendResponse();
        NotifyChannel notifyChannel = notifyVO.getNotifyChannel();
        NotifyTemplate notifyTemplate = notifyVO.getNotifyTemplate();
        String content = JSONObject.parseObject(notifyTemplate.getMsgParams()).get("content").toString();
        String sendContent = StringUtils.strReplaceVariable("${", "}", content, notifyVO.getMap());
        DingTalkMsgParams dingTalkMsgParams = JSONObject.parseObject(notifyTemplate.getMsgParams(), DingTalkMsgParams.class);
        // 获取AppKey和AppSecret
        DingTalkConfigParams dingTalkConfigParams = JSONObject.parseObject(notifyChannel.getConfigContent(), DingTalkConfigParams.class);
        if (NotifyChannelProviderEnum.DING_TALK_WORK.equals(notifyVO.getNotifyChannelProviderEnum())) {
            notifySendResponse = this.workSend(dingTalkConfigParams, dingTalkMsgParams, notifyVO.getSendAccount(), sendContent);
        } else if (NotifyChannelProviderEnum.DING_TALK_GROUP_ROBOT.equals(notifyVO.getNotifyChannelProviderEnum())) {
            notifySendResponse = this.customizeRobotSend(dingTalkConfigParams, dingTalkMsgParams, sendContent);
        }
        return notifySendResponse;
    }

    /**
     * 自定义机器人发送
     * @param dingTalkConfigParams 渠道配置参数
     * @param: dingTalkMsgParams 模版配置参数
     * @param: sendContent 发送内容
     * @return com.fastbee.common.core.notify.NotifySendResponse
     */
    private NotifySendResponse customizeRobotSend(DingTalkConfigParams dingTalkConfigParams, DingTalkMsgParams dingTalkMsgParams, String sendContent) {
        NotifySendResponse notifySendResponse = new NotifySendResponse();
        DefaultDingTalkClient client = new DefaultDingTalkClient(dingTalkConfigParams.getWebHook());
        OapiRobotSendRequest request = this.createOapiRobotMsg(dingTalkMsgParams, sendContent);
        try {
            OapiRobotSendResponse execute = client.execute(request);
            notifySendResponse.setStatus("0".equals(execute.getErrorCode()) ? 1 : 0);
            notifySendResponse.setResultContent(JSON.toJSONString(execute));
        } catch (ApiException e) {
            notifySendResponse.setStatus(0);
            notifySendResponse.setResultContent(e.toString());
        }
        return notifySendResponse;
    }

    /**
     * 工作通知发送
     * @param dingTalkConfigParams 渠道配置参数
     * @param: dingTalkMsgParams 模版配置参数
     * @param: sendAccount 发送账号，多个以,隔开
     * @param: sendContent 发送内容
     * @return com.fastbee.common.core.notify.NotifySendResponse
     */
    private NotifySendResponse workSend(DingTalkConfigParams dingTalkConfigParams, DingTalkMsgParams dingTalkMsgParams, String sendAccount, String sendContent) {
        NotifySendResponse notifySendResponse = new NotifySendResponse();
        notifySendResponse.setSendContent(sendContent);
        notifySendResponse.setStatus(1);
        // 发送
        try {
            // 获取应用访问凭证accessToken
            Config config = new Config();
            config.protocol = "https";
            config.regionId = "central";
            Client client = new Client(config);
            GetAccessTokenRequest accessTokenRequest = new GetAccessTokenRequest();
            accessTokenRequest.setAppKey(dingTalkConfigParams.getAppKey());
            accessTokenRequest.setAppSecret(dingTalkConfigParams.getAppSecret());
            GetAccessTokenResponse accessTokenResponse = client.getAccessToken(accessTokenRequest);
            String accessToken = accessTokenResponse.getBody().getAccessToken();
            DingTalkClient dingTalkClient = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2");
            OapiMessageCorpconversationAsyncsendV2Request req = new OapiMessageCorpconversationAsyncsendV2Request();
            req.setAgentId(Long.valueOf(dingTalkConfigParams.getAgentId()));
            // 优先取发送账号、然后是部门、然后是所有人
            if (StringUtils.isNotEmpty(sendAccount)) {
                // 根据手机号获取钉钉userid
//                DingTalkClient dingTalkClientUser = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/user/getbymobile");
//                OapiV2UserGetbymobileRequest phoneReq = new OapiV2UserGetbymobileRequest();
//                phoneReq.setMobile(sendAccount);
//                OapiV2UserGetbymobileResponse rsp = dingTalkClientUser.execute(phoneReq, accessToken);
//                userId = rsp.getResult().getUserid();
                req.setUseridList(sendAccount);
            } else if (StringUtils.isNotEmpty(dingTalkMsgParams.getDeptId())) {
                req.setDeptIdList(dingTalkMsgParams.getDeptId());
                notifySendResponse.setOtherSendAccount(dingTalkMsgParams.getDeptId());
            } else if (Boolean.TRUE.toString().equals(dingTalkMsgParams.getSendAllEnable())) {
                req.setToAllUser(Boolean.TRUE);
                notifySendResponse.setOtherSendAccount("allUser");
            }
            OapiMessageCorpconversationAsyncsendV2Request.Msg msg = this.createOapiMessageMsg(dingTalkMsgParams, sendContent);
            req.setMsg(msg);
            OapiMessageCorpconversationAsyncsendV2Response rsp = dingTalkClient.execute(req, accessToken);
            notifySendResponse.setStatus(0 == rsp.getErrcode() ? 1 : 0);
            notifySendResponse.setResultContent(JSON.toJSONString(rsp));
        } catch (Exception e) {
            notifySendResponse.setStatus(0);
            notifySendResponse.setResultContent(e.toString());
        }
        return notifySendResponse;
    }

    private OapiRobotSendRequest createOapiRobotMsg(DingTalkMsgParams dingTalkMsgParams, String sendContent) {
        OapiRobotSendRequest request = new OapiRobotSendRequest();
        request.setMsgtype(dingTalkMsgParams.getMsgType());
        switch (request.getMsgtype()) {
            case "text":
                OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
                text.setContent(sendContent);
                request.setText(text);
                break;
            case "link":
                OapiRobotSendRequest.Link link = new OapiRobotSendRequest.Link();
                link.setTitle(dingTalkMsgParams.getTitle());
                link.setText(sendContent);
                link.setMessageUrl(dingTalkMsgParams.getMessageUrl());
                link.setPicUrl(dingTalkMsgParams.getPicUrl());
                request.setLink(link);
                break;
            case "markdown":
                OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
                markdown.setText(sendContent);
                markdown.setTitle(dingTalkMsgParams.getTitle());
                request.setMarkdown(markdown);
                break;
            default:
                break;
        }
        return request;
    }

    /**
     * 构建钉钉发送消息
     * @param msgParams 消息参数
     * @return
     */
    private OapiMessageCorpconversationAsyncsendV2Request.Msg createOapiMessageMsg(DingTalkMsgParams msgParams, String content) {
        OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
        msg.setMsgtype(msgParams.getMsgType());
        switch (msg.getMsgtype()) {
            case "text":
                msg.setText(new OapiMessageCorpconversationAsyncsendV2Request.Text());
                msg.getText().setContent(content);
                break;
            case "link":
                msg.setLink(new OapiMessageCorpconversationAsyncsendV2Request.Link());
                msg.getLink().setTitle(msgParams.getTitle());
                msg.getLink().setText(content);
                msg.getLink().setMessageUrl(msgParams.getMessageUrl());
                msg.getLink().setPicUrl(msgParams.getPicUrl());
                break;
            case "markdown":
                msg.setMarkdown(new OapiMessageCorpconversationAsyncsendV2Request.Markdown());
                msg.getMarkdown().setText(content);
                msg.getMarkdown().setTitle(msgParams.getTitle());
                break;
            default:
                break;
        }
        return msg;
    }

    /**
     * 获取AccessToken
     * @param appKey
     * @param: appSecret
     * @return java.lang.String
     */
    private String getAccessToken(String appKey, String appSecret) {
        try {
            Config config = new Config();
            config.protocol = "https";
            config.regionId = "central";
            Client client = new Client(config);
            GetAccessTokenRequest accessTokenRequest = new GetAccessTokenRequest();
            accessTokenRequest.setAppKey(appKey);
            accessTokenRequest.setAppSecret(appSecret);
            GetAccessTokenResponse accessToken = client.getAccessToken(accessTokenRequest);
            return accessToken.getBody().getAccessToken();
        } catch (TeaException err) {
            if (!Common.empty(err.code) && !Common.empty(err.message)) {
                // err 中含有 code 和 message 属性，可帮助开发定位问题
            }
        } catch (Exception _err) {
            TeaException err = new TeaException(_err.getMessage(), _err);
            if (!Common.empty(err.code) && !Common.empty(err.message)) {
                // err 中含有 code 和 message 属性，可帮助开发定位问题
            }

        }
        return "";
    }

}
