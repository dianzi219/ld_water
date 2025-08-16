package com.fastbee.iot.wechat.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.XmlUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.enums.SocialPlatformType;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.common.utils.uuid.IdUtils;
import com.fastbee.common.utils.wechat.WechatUtils;
import com.fastbee.common.wechat.WeChatAppResult;
import com.fastbee.common.wechat.WeChatUserInfo;
import com.fastbee.iot.domain.SocialPlatform;
import com.fastbee.iot.domain.SocialUser;
import com.fastbee.iot.service.ISocialPlatformService;
import com.fastbee.iot.service.ISocialUserService;
import com.fastbee.iot.wechat.WeChatCallbackService;
import com.fastbee.common.wechat.WxCallBackXmlBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

/**
 * @author fastb
 * @version 1.0
 * @description: 微信回调相关处理器
 * @date 2024-03-08 14:36
 */
@Slf4j
@Service
public class WeChatCallbackServiceImpl implements WeChatCallbackService {

    @Resource
    private ISocialPlatformService socialPlatformService;
    @Resource
    private ISocialUserService socialUserService;

    @Override
    public String handleCallback(String xmlStr) {
        WxCallBackXmlBO wxCallBackXmlBO;
        String resMsg = "";
        try {
            Map<String, Object> map = XmlUtil.xmlToMap(xmlStr);
            wxCallBackXmlBO = JSONObject.parseObject(JSON.toJSONString(map), WxCallBackXmlBO.class);
        } catch (Exception e) {
            log.error("微信回调传递的xml解析报错：{}，{}", xmlStr, e);
            return "";
        }
        if (ObjectUtil.isNull(wxCallBackXmlBO) || StringUtils.isEmpty(wxCallBackXmlBO.getMsgType())) {
            log.error("微信回调传递的xml中MsgType解析为空：{}", xmlStr);
            return "";
        }
        String msgType = wxCallBackXmlBO.getMsgType().toLowerCase();
        switch (msgType) {
            // 文本搜索关键词匹配
            case "text":
                break;
            // 图片信息 直接转入人工客服
            case "image":
                break;
            //物理地址信息
            case "location":
                break;
            // 事件推送（订阅、取消订阅）
            case "event":
                resMsg = this.handleEvent(wxCallBackXmlBO);
                break;
            // 语音识别
            case "voice":
                break;
            default:
                break;
        }
        return resMsg;
    }

    private String handleEvent(WxCallBackXmlBO wxCallBackXmlBo) {
        String resMsg = "";
        //事件类型
        String eventStr = wxCallBackXmlBo.getEvent();
        if (org.apache.commons.lang3.StringUtils.isNotBlank(eventStr)) {
            switch (org.apache.commons.lang3.StringUtils.lowerCase(eventStr)) {
                // 关键词
                case "click":
                    break;
                // 订阅 + 扫描二维码获取的场景值
                case "subscribe":
                    resMsg = this.handleEventSubscribe(wxCallBackXmlBo);
                    break;
                // 取消订阅
                case "unsubscribe":
                    resMsg = this.handleEventUnSubscribe(wxCallBackXmlBo);
                    break;
                // 扫描二维码
                case "scan":
                    break;
                default:
                    break;
            }
        }
        return resMsg;
    }

    private String handleEventUnSubscribe(WxCallBackXmlBO wxCallBackXmlBo) {
        // 解绑微信公众号关注
        String openId = wxCallBackXmlBo.getFromUserName();
        socialUserService.deleteByOpenIdAndSourceClient(openId, SocialPlatformType.WECHAT_OPEN_PUBLIC_ACCOUNT.getSourceClient());
        return "";
    }

    private String handleEventSubscribe(WxCallBackXmlBO wxCallBackXmlBo) {
        // 获取微信公众号appid和secret
        String sourceClient = SocialPlatformType.WECHAT_OPEN_PUBLIC_ACCOUNT.getSourceClient();
        SocialPlatform socialPlatform = socialPlatformService.selectSocialPlatformByPlatform(SocialPlatformType.WECHAT_OPEN_PUBLIC_ACCOUNT.getSourceClient());
        if (ObjectUtil.isNull(socialPlatform)) {
            log.info("请先配置微信开放平台公众号！");
            return "";
        }
        String openId = wxCallBackXmlBo.getFromUserName();
        // 公众号获取accessToken信息
        WeChatAppResult weChatAppResult = WechatUtils.getAccessToken(socialPlatform.getClientId(), socialPlatform.getSecretKey());
        if (ObjectUtil.isNull(weChatAppResult)) {
            log.info("公众号获取accessToken失败");
            return "";
        }
        if (StringUtils.isEmpty(weChatAppResult.getAccessToken())) {
            log.info("公众号获取accessToken失败:{}", JSON.toJSONString(weChatAppResult));
            return "";
        }
        String accessToken = weChatAppResult.getAccessToken();
        WeChatUserInfo weChatUserInfo = WechatUtils.getWeChatPublicAccountUserInfo(accessToken, openId);
        if (null != weChatUserInfo.getErrCode()) {
            log.info("公众号获取微信用户信息失败：{}", JSON.toJSONString(weChatUserInfo));
            return "";
        }
        SocialUser socialUser = new SocialUser();
        socialUser.setStatus("1").setSource(sourceClient).setSourceClient(sourceClient).setOpenId(openId)
                .setUnionId(weChatUserInfo.getUnionId()).setAccessToken(accessToken).setAvatar(weChatUserInfo.getHeadImgUrl())
                .setNickname(weChatUserInfo.getNickname()).setUuid(IdUtils.randomUUID()).setCreateBy("System");
        socialUser.setCreateTime(new Date());
        socialUserService.insertSocialUser(socialUser);
        // 返回消息
        return WechatUtils.responseText(wxCallBackXmlBo, socialPlatform.getRemark());
    }

}
