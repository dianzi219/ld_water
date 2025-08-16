package com.fastbee.notify.core.wechat.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.constant.FastBeeConstant;
import com.fastbee.common.core.notify.NotifySendResponse;
import com.fastbee.common.core.notify.config.WeChatConfigParams;
import com.fastbee.common.core.notify.msg.WeComMsgParams;
import com.fastbee.common.core.notify.msg.WechatMsgParams;
import com.fastbee.common.core.redis.RedisCache;
import com.fastbee.common.enums.NotifyChannelProviderEnum;
import com.fastbee.common.enums.SocialPlatformType;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.common.utils.http.HttpUtils;
import com.fastbee.common.utils.wechat.WechatUtils;
import com.fastbee.common.wechat.WeChatAppResult;
import com.fastbee.iot.domain.SocialUser;
import com.fastbee.iot.service.ISocialUserService;
import com.fastbee.notify.core.wechat.service.WeChatPushService;
import com.fastbee.notify.core.wechat.vo.TemplateDataVo;
import com.fastbee.notify.core.wechat.vo.WeChatMiniPushVO;
import com.fastbee.notify.core.wechat.vo.WeChatPublicAccountPushVO;
import com.fastbee.notify.core.wechat.vo.WxMssVo;
import com.fastbee.notify.domain.NotifyChannel;
import com.fastbee.notify.domain.NotifyTemplate;
import com.fastbee.notify.vo.NotifyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author fastb
 * @version 1.0
 * @description: 微信相关发送服务类
 * @date 2023-12-26 17:15
 */
@Slf4j
@Service
public class WeChatPushServiceImpl implements WeChatPushService {

    @Resource
    private ISocialUserService socialUserService;
    private static RestTemplate restTemplate;
    @Resource
    private RedisCache redisCache;


    @Override
    public NotifySendResponse send(NotifyVO notifyVO) {
        NotifySendResponse notifySendResponse = new NotifySendResponse();
        NotifyChannelProviderEnum notifyChannelProviderEnum = notifyVO.getNotifyChannelProviderEnum();
        switch (notifyChannelProviderEnum) {
            case WECHAT_MINI_PROGRAM:
                notifySendResponse = this.weChatMiniSend(notifyVO);
                break;
            case WECHAT_WECOM_ROBOT:
                notifySendResponse = this.weComRobotSend(notifyVO);
                break;
            case WECHAT_WECOM_APPLY:
                notifySendResponse = this.weComApplySend(notifyVO);
                break;
            case WECHAT_PUBLIC_ACCOUNT:
                notifySendResponse = this.weChatPublicAccountSend(notifyVO);
            default:
                break;
        }
        return notifySendResponse;
    }

    /**
     * 微信公众号发送
     * @param notifyVO 通知参数
     * @return com.fastbee.common.core.notify.NotifySendResponse
     */
    private NotifySendResponse weChatPublicAccountSend(NotifyVO notifyVO) {
        NotifySendResponse notifySendResponse = new NotifySendResponse();
        if (StringUtils.isEmpty(notifyVO.getSendAccount())) {
            notifySendResponse.setStatus(0);
            notifySendResponse.setResultContent("发送用户id为空，请先配置发送用户id！");
            return notifySendResponse;
        }
        LinkedHashMap<String, String> map = notifyVO.getMap();
        LinkedHashMap<String,String> mapVariable = new LinkedHashMap<>();
        Map<String, Object> sendMap = new HashMap<>(5);
        for (Map.Entry<String, String> m : map.entrySet()) {
            sendMap.put(m.getKey(), new TemplateDataVo(m.getValue()));
            mapVariable.put(m.getKey() + ".DATA", m.getValue());
        }
        NotifyChannel notifyChannel = notifyVO.getNotifyChannel();
        NotifyTemplate notifyTemplate = notifyVO.getNotifyTemplate();
        WeChatConfigParams weChatConfigParams = JSONObject.parseObject(notifyChannel.getConfigContent(), WeChatConfigParams.class);
        WechatMsgParams wechatMsgParams = JSONObject.parseObject(notifyTemplate.getMsgParams(), WechatMsgParams.class);
        // 获取accessToken
        if (StringUtils.isEmpty(weChatConfigParams.getAppId()) || StringUtils.isEmpty(weChatConfigParams.getAppSecret())) {
            notifySendResponse.setStatus(0);
            notifySendResponse.setResultContent("通知渠道配置参数为空，请先配置！");
            return notifySendResponse;
        }
        WeChatAppResult weChatAppResult = WechatUtils.getAccessToken(weChatConfigParams.getAppId(), weChatConfigParams.getAppSecret());
        if (ObjectUtil.isNull(weChatAppResult) || StringUtils.isEmpty(weChatAppResult.getAccessToken())) {
            notifySendResponse.setStatus(0);
            notifySendResponse.setResultContent("获取AccessToken失败，原因：" + JSON.toJSONString(weChatAppResult));
            return notifySendResponse;
        }
        String pushUrl = FastBeeConstant.URL.WX_PUBLIC_ACCOUNT_TEMPLATE_SEND_URL_PREFIX + weChatAppResult.getAccessToken();

        // 组装发送参数
        WeChatPublicAccountPushVO weChatPublicAccountPushVO = new WeChatPublicAccountPushVO();
        weChatPublicAccountPushVO.setData(sendMap);
        weChatPublicAccountPushVO.setTemplateId(wechatMsgParams.getTemplateId());
        if (StringUtils.isNotEmpty(wechatMsgParams.getRedirectUrl())) {
            weChatPublicAccountPushVO.setUrl(wechatMsgParams.getRedirectUrl());
        }
        if (StringUtils.isNotEmpty(wechatMsgParams.getPagePath())) {
            WeChatPublicAccountPushVO.MiniProgram miniProgram = new WeChatPublicAccountPushVO.MiniProgram();
            miniProgram.setAppId(wechatMsgParams.getAppid());
            miniProgram.setPagePath(wechatMsgParams.getPagePath());
            weChatPublicAccountPushVO.setMiniProgram(miniProgram);
        }
        // 获取用户id
        List<String> userIdList = StringUtils.str2List(notifyVO.getSendAccount(), ",", true, true);
        List<SocialUser> socialUserList = socialUserService.listWechatPublicAccountOpenId(userIdList);
        Map<Long, String> userMap = socialUserList.stream().collect(Collectors.toMap(SocialUser::getSysUserId, SocialUser::getOpenId, (o, n) -> n));

        List<String> resultContentList = new ArrayList<>();
        for (String userId : userIdList) {
            String openId = userMap.get(Long.valueOf(userId));
            if (StringUtils.isEmpty(openId)) {
                resultContentList.add("userId:[" + userId + "]，status:[0]，resultContent:[该用户未绑定微信，请先绑定后重试！]");
                notifySendResponse.setStatus(0);
                continue;
            }
            weChatPublicAccountPushVO.setTouser(openId);
            notifySendResponse = this.weChatPostPush(JSON.toJSONString(weChatPublicAccountPushVO), pushUrl);
            resultContentList.add("userId:[" + userId + "]，status:[" + notifySendResponse.getStatus() +"]，resultContent:["  + notifySendResponse.getResultContent() + "]");
        }
        String content = JSONObject.parseObject(notifyVO.getNotifyTemplate().getMsgParams()).get("content").toString();
        String sendContent = StringUtils.strReplaceVariable("{{", "}}", content, mapVariable);
        notifySendResponse.setSendContent(sendContent);
        notifySendResponse.setResultContent(StringUtils.join(resultContentList, "; "));
        return notifySendResponse;
    }

    /**
     * 企业微信应用消息发送
     * @param notifyVO 发送vo类
     * @return com.fastbee.common.core.notify.NotifySendResponse
     */
    private NotifySendResponse weComApplySend(NotifyVO notifyVO) {
        NotifySendResponse notifySendResponse = new NotifySendResponse();
        if (StringUtils.isEmpty(notifyVO.getSendAccount())) {
            notifySendResponse.setStatus(0);
            notifySendResponse.setResultContent("发送成员账号为空，请先配置！");
            return notifySendResponse;
        }
        WeChatConfigParams weChatConfigParams = JSONObject.parseObject(notifyVO.getNotifyChannel().getConfigContent(), WeChatConfigParams.class);
        if (StringUtils.isEmpty(weChatConfigParams.getCorpId()) || StringUtils.isEmpty(weChatConfigParams.getCorpSecret())) {
            notifySendResponse.setStatus(0);
            notifySendResponse.setResultContent("企业微信应用消息渠道配置信息为空，请先配置！");
            return notifySendResponse;
        }
        WeComMsgParams weComMsgParams = JSONObject.parseObject(notifyVO.getNotifyTemplate().getMsgParams(), WeComMsgParams.class);
        // 获取accessToken，优先从缓存获取，不能频繁的获取
        String accessToken;
        Object accessTokenRedis = redisCache.getCacheObject(FastBeeConstant.REDIS.NOTIFY_WECOM_APPLY_ACCESSTOKEN + weChatConfigParams.getAgentId());
        if (Objects.nonNull(accessTokenRedis)) {
            accessToken = accessTokenRedis.toString();
        } else {
            String s = HttpUtils.sendGet(FastBeeConstant.URL.WECOM_GET_ACCESSTOKEN + "?corpid=" + weChatConfigParams.getCorpId() + "&corpsecret=" + weChatConfigParams.getCorpSecret());
            JSONObject accessTokenJson = JSONObject.parseObject(s);
            if (!"0".equals(accessTokenJson.get("errcode").toString())) {
                notifySendResponse.setStatus(0);
                notifySendResponse.setResultContent(s);
                return notifySendResponse;
            }
            accessToken = accessTokenJson.get("access_token").toString();
            redisCache.setCacheObject(FastBeeConstant.REDIS.NOTIFY_WECOM_APPLY_ACCESSTOKEN + weChatConfigParams.getAgentId(), accessToken, 2, TimeUnit.HOURS);
        }
        // 通过手机号获取企业微信用户名
//        JSONObject phoneReq = new JSONObject();
//        phoneReq.put("mobile", notifyVO.getSendAccount());
//        String phoneRes = HttpUtils.sendPost("https://qyapi.weixin.qq.com/cgi-bin/user/getuserid?access_token=" + accessToken, phoneReq.toString());
//        JSONObject phoneResJson = JSONObject.parseObject(phoneRes);
//        if (!"0".equals(phoneResJson.get("errcode").toString())) {
//            notifySendResponse.setStatus(0);
//            notifySendResponse.setResultContent(phoneRes);
//            return notifySendResponse;
//        }
//        String userid = phoneResJson.get("userid").toString();
        // 构建消息内容
        String sendContent = StringUtils.strReplaceVariable("${", "}", weComMsgParams.getContent(), notifyVO.getMap());
        notifySendResponse.setSendContent(sendContent);
        JSONObject msg = this.createWeComMsg(weComMsgParams, sendContent);
        // 多个用户用|分隔
        List<String> userIdList = StringUtils.str2List(notifyVO.getSendAccount(), ",", true, true);
        String userIdStr = String.join("|", userIdList);
        msg.put("touser", userIdStr);
        msg.put("agentid", weChatConfigParams.getAgentId());
        // 发送
        String sendUrl = FastBeeConstant.URL.WECOM_APPLY_SEND + accessToken;
        String result = HttpUtils.sendPost(sendUrl, msg.toString());
        notifySendResponse.setStatus("0".equals(JSONObject.parseObject(result).get("errcode").toString()) ? 1 : 0);
        notifySendResponse.setResultContent(result);
        return notifySendResponse;
    }

    /**
     * 企业微信群机器人发送
     * @param notifyVO 发送配置类
     * @return com.fastbee.common.core.notify.NotifySendResponse
     */
    private NotifySendResponse weComRobotSend(NotifyVO notifyVO) {
        NotifySendResponse notifySendResponse = new NotifySendResponse();
        NotifyChannel notifyChannel = notifyVO.getNotifyChannel();
        NotifyTemplate notifyTemplate = notifyVO.getNotifyTemplate();
        WeChatConfigParams weChatConfigParams = JSONObject.parseObject(notifyChannel.getConfigContent(), WeChatConfigParams.class);
        if (StringUtils.isEmpty(weChatConfigParams.getWebHook())) {
            notifySendResponse.setStatus(0);
            notifySendResponse.setResultContent("企业微信群机器人webHook为空，请先去通知渠道下配置！");
            return notifySendResponse;
        }
        WeComMsgParams weComMsgParams = JSONObject.parseObject(notifyTemplate.getMsgParams(), WeComMsgParams.class);
        String sendContent = StringUtils.strReplaceVariable("${", "}", weComMsgParams.getContent(), notifyVO.getMap());
        JSONObject sendJson = this.createWeComMsg(weComMsgParams, sendContent);
        String s = HttpUtils.sendPost(weChatConfigParams.getWebHook(), sendJson.toString());
        notifySendResponse.setSendContent(sendJson.toJSONString());
        notifySendResponse.setStatus("0".equals(JSONObject.parseObject(s).get("errcode").toString()) ? 1 : 0);
        notifySendResponse.setResultContent(s);
        return notifySendResponse;
    }

    /**
     * 构建企业微信发送参数
     * @param weComMsgParams 消息配置参数
     * @param: sendContent
     * @return java.lang.String
     */
    private JSONObject createWeComMsg(WeComMsgParams weComMsgParams, String sendContent) {
        JSONObject req = new JSONObject();
        String msgType = weComMsgParams.getMsgType();
        req.put("msgtype", msgType);
        switch (msgType) {
            case "text":
                JSONObject text = new JSONObject();
                text.put("content", sendContent);
                req.put("text", text);
                break;
            case "markdown":
                JSONObject markdown = new JSONObject();
                markdown.put("content", sendContent);
                req.put("markdown", markdown);
                break;
            case "news":
                JSONObject articles = new JSONObject();
                articles.put("title", weComMsgParams.getTitle());
                articles.put("description", sendContent);
                articles.put("url", weComMsgParams.getUrl());
                articles.put("picurl", weComMsgParams.getPicUrl());
                JSONObject news = new JSONObject();
                news.put("articles", articles);
                req.put("news", news);
                break;
            default:
                break;
        }
        return req;
    }

    /**
     * 小程序发送
     * @param notifyVO 发送vo类
     * @return com.fastbee.common.core.notify.NotifySendResponse
     */
    private NotifySendResponse weChatMiniSend(NotifyVO notifyVO) {
        // 微信小程序
        NotifySendResponse notifySendResponse = new NotifySendResponse();
        if (StringUtils.isEmpty(notifyVO.getSendAccount())) {
            notifySendResponse.setStatus(0);
            notifySendResponse.setResultContent("发送用户id为空，请先配置发送用户id！");
            return notifySendResponse;
        }
        LinkedHashMap<String, String> map = notifyVO.getMap();
        LinkedHashMap<String,String> mapVariable = new LinkedHashMap<>();
        Map<String, Object> sendMap = new HashMap<>(5);
        for (Map.Entry<String, String> m : map.entrySet()) {
            sendMap.put(m.getKey(), new TemplateDataVo(m.getValue()));
            mapVariable.put(m.getKey() + ".DATA", m.getValue());
        }
        List<String> userIdList = StringUtils.str2List(notifyVO.getSendAccount(), ",", true, true);
        List<String> resultContentList = new ArrayList<>();
        for (String userId : userIdList) {
            WeChatMiniPushVO weChatMiniPushVO = this.createWeChatMiniPushVO(notifyVO.getNotifyChannel(), notifyVO.getNotifyTemplate(), Long.valueOf(userId));
            if (Objects.isNull(weChatMiniPushVO)) {
                resultContentList.add("userId:[" + userId + "]，status:[0]，resultContent:[获取微信小程序推送配置信息失败！]");
                notifySendResponse.setStatus(0);
                continue;
            }
            if (StringUtils.isNotEmpty(weChatMiniPushVO.getErrorMsg())) {
                resultContentList.add("userId:[" + userId + "]，status:[0]，resultContent:["  + weChatMiniPushVO.getErrorMsg() + "]");
                notifySendResponse.setStatus(0);
                continue;
            }
            WxMssVo wxMssVo = weChatMiniPushVO.getWxMssVo();
            wxMssVo.setData(sendMap);
            notifySendResponse = this.weChatPostPush(JSON.toJSONString(wxMssVo), weChatMiniPushVO.getUrl());
            resultContentList.add("userId:[" + userId + "]，status:[" + notifySendResponse.getStatus() +"]，resultContent:["  + notifySendResponse.getResultContent() + "]");
        }
        String content = JSONObject.parseObject(notifyVO.getNotifyTemplate().getMsgParams()).get("content").toString();
        String sendContent = StringUtils.strReplaceVariable("{{", "}}", content, mapVariable);
        notifySendResponse.setSendContent(sendContent);
        notifySendResponse.setResultContent(StringUtils.join(resultContentList, "; "));
        return notifySendResponse;
    }

    /**
     * 推送消息给指定的用户 --微信小程序服务号推送
     * @param json 推送参数
     * @return 推送结果
     */
    @Override
    public NotifySendResponse weChatPostPush(String json, String url) {
        NotifySendResponse notifySendResponse = new NotifySendResponse();
        if(restTemplate==null){
            restTemplate = new RestTemplate();
        }
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        HttpEntity<String> httpEntity = new HttpEntity<>(json, headers);
        ResponseEntity<String> responseEntity =
                restTemplate.postForEntity(url, httpEntity, String.class);
        log.warn("小程序推送结果={}", responseEntity.getBody());
        String response = responseEntity.getBody();
        notifySendResponse.setStatus("0".equals(JSONObject.parseObject(response).get("errcode").toString()) ? 1 : 0);
        notifySendResponse.setResultContent(response);
        return notifySendResponse;
    }

    @Override
    public WeChatMiniPushVO createWeChatMiniPushVO(NotifyChannel notifyChannel, NotifyTemplate notifyTemplate, Long userId) {
        WeChatMiniPushVO weChatMiniPushVO = new WeChatMiniPushVO();
        //获取微信与用户关联信息
        SocialUser socialUser = socialUserService.selectByUserIdAndSourceClient(userId, SocialPlatformType.WECHAT_OPEN_MINI_PROGRAM.sourceClient);
        if (Objects.isNull(socialUser) || StringUtils.isEmpty(socialUser.getOpenId())) {
            weChatMiniPushVO.setErrorMsg("该用户未绑定微信小程序，请先绑定后重试");
            return weChatMiniPushVO;
        }
        //获取openId
        String openId = socialUser.getOpenId();
        //拼接推送的模版
        WxMssVo wxMssVo = new WxMssVo();
        //用户openid
        wxMssVo.setTouser(openId);
        if (notifyTemplate == null) {
            weChatMiniPushVO.setErrorMsg("推送模板为空,请先配置微信小程序推送模板");
            return weChatMiniPushVO;
        }
        //获取微信服务号推送的配置参数
        if (notifyChannel == null) {
            weChatMiniPushVO.setErrorMsg("推送渠道为空,请检查微信小程序推送渠道");
            return weChatMiniPushVO;
        }
        WeChatConfigParams weChatConfigParams = JSONObject.parseObject(notifyChannel.getConfigContent(), WeChatConfigParams.class);
        if (StringUtils.isEmpty(weChatConfigParams.getAppId()) || StringUtils.isEmpty(weChatConfigParams.getAppSecret())) {
            weChatMiniPushVO.setErrorMsg("微信小程序渠道配置信息为空，请先配置！");
            return weChatMiniPushVO;
        }
        //获取access_token
        WeChatAppResult weChatAppResult = WechatUtils.getAccessToken(weChatConfigParams.getAppId(), weChatConfigParams.getAppSecret());
        if (weChatAppResult == null || StringUtils.isEmpty(weChatAppResult.getAccessToken())) {
            weChatMiniPushVO.setErrorMsg("获取用户调用凭据失败，请重新登录！");
            return weChatMiniPushVO;
        }
        //微信推送URL
        String url = FastBeeConstant.URL.WX_MINI_PROGRAM_PUSH_URL_PREFIX + "?access_token=" + weChatAppResult.getAccessToken();
        WechatMsgParams msgParams = JSONObject.parseObject(notifyTemplate.getMsgParams(), WechatMsgParams.class);
        //模版id
        wxMssVo.setTemplateId(msgParams.getTemplateId());
        //推送路径
        if (StringUtils.isNotEmpty(msgParams.getRedirectUrl())){
            wxMssVo.setPage(msgParams.getRedirectUrl());
        }
        weChatMiniPushVO.setWxMssVo(wxMssVo);
        weChatMiniPushVO.setUrl(url);
        return weChatMiniPushVO;
    }

}
