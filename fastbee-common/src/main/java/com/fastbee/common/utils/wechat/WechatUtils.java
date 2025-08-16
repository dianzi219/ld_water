package com.fastbee.common.utils.wechat;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.constant.CacheConstants;
import com.fastbee.common.constant.FastBeeConstant;
import com.fastbee.common.core.redis.RedisCache;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.common.utils.http.HttpUtils;
import com.fastbee.common.utils.spring.SpringUtils;
import com.fastbee.common.wechat.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author fastb
 * @version 1.0
 * @description: 微信相关工具类
 * @date 2024-01-08 17:36
 */
public class WechatUtils {

    /**
     * 网站、移动应用获取微信用户accessToken
     * @param code 用户登录code
     * @param appId 微信平台appId
     * @param secret 微信平台密钥
     * @return WeChatAppResult
     */
    public static WeChatAppResult getAccessTokenOpenId(String code, String appId, String secret) {
        String url = FastBeeConstant.URL.WX_GET_ACCESS_TOKEN_URL_PREFIX + "?appid=" + appId + "&secret=" + secret + "&code=" + code + "&grant_type=authorization_code";
        String s = HttpUtils.sendGet(url);
        return JSON.parseObject(s, WeChatAppResult.class);
    }

    /**
     * 获取微信用户信息
     * @param accessToken 接口调用凭证
     * @param openId 用户唯一标识
     * @return WeChatUserInfo
     */
    public static WeChatUserInfo getWeChatUserInfo(String accessToken, String openId) {
        String url = FastBeeConstant.URL.WX_GET_USER_INFO_URL_PREFIX + "?access_token=" + accessToken + "&openid=" + openId;
        String s = HttpUtils.sendGet(url);
        return JSON.parseObject(s, WeChatUserInfo.class);
    }

    /**
     * 小程序获取微信用户登录信息
     * @param code 用户凭证
     * @param appId 微信平台appId
     * @param secret 微信平台密钥
     * @return 结果
     */
    public static WeChatMiniProgramResult codeToSession(String code, String appId, String secret) {
        String url = FastBeeConstant.URL.WX_MINI_PROGRAM_GET_USER_SESSION_URL_PREFIX + "?appid=" + appId + "&secret=" + secret + "&js_code=" + code + "&grant_type=authorization_code";
        String s = HttpUtils.sendGet(url);
        return JSON.parseObject(s, WeChatMiniProgramResult.class);
    }

    /**
     * 小程序获取微信用户手机号
     * @param code 凭证
     * @param accessToken 微信用户token
     * @return 手机号信息
     */
    public static WeChatPhoneInfo getWechatUserPhoneInfo(String code, String accessToken) {
        String url = FastBeeConstant.URL.WX_GET_USER_PHONE_URL_PREFIX + accessToken;
        HashMap<String, String> map = new HashMap<>();
        map.put("code", code);
        String s = HttpUtils.sendPost(url, JSONObject.toJSONString(map));
        return JSON.parseObject(s, WeChatPhoneInfo.class);
    }

    /**
     * 小程序获、公众号取微信accessToken
     * @param appId 微信平台appId
     * @param secret 微信平台密钥
     * @return WeChatAppResult
     */
    public static WeChatAppResult getAccessToken(String appId, String secret) {
        // 加个缓存
        WeChatAppResult wechatAppResultRedis = SpringUtils.getBean(RedisCache.class).getCacheObject(CacheConstants.WECHAT_GET_ACCESS_TOKEN_APPID + appId);
        if (ObjectUtil.isNotNull(wechatAppResultRedis)) {
            return wechatAppResultRedis;
        }
        String url = FastBeeConstant.URL.WX_MINI_PROGRAM_GET_ACCESS_TOKEN_URL_PREFIX + "&appid=" + appId + "&secret=" + secret;
        String s = HttpUtils.sendGet(url);
        WeChatAppResult weChatAppResult = JSON.parseObject(s, WeChatAppResult.class);
        if (ObjectUtil.isNotNull(weChatAppResult) && StringUtils.isNotEmpty(weChatAppResult.getAccessToken())) {
            SpringUtils.getBean(RedisCache.class).setCacheObject(CacheConstants.WECHAT_GET_ACCESS_TOKEN_APPID + appId, weChatAppResult, 1, TimeUnit.HOURS);
        }
        return weChatAppResult;
    }

    /**
     * 微信公众号获取微信用户信息
     * @param accessToken 接口调用凭证
     * @param openId 用户唯一标识
     * @return WeChatUserInfo
     */
    public static WeChatUserInfo getWeChatPublicAccountUserInfo(String accessToken, String openId) {
        String url = FastBeeConstant.URL.WX_PUBLIC_ACCOUNT_GET_USER_INFO_URL_PREFIX + "?access_token=" + accessToken + "&openid=" + openId + "&lang=zh_CN";
        String s = HttpUtils.sendGet(url);
        return JSON.parseObject(s, WeChatUserInfo.class);
    }

    public static String responseText(WxCallBackXmlBO wxCallBackXmlBO, String content) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<xml>");
        stringBuilder.append("<ToUserName><![CDATA[" + wxCallBackXmlBO.getFromUserName() + "]]></ToUserName>");
        stringBuilder.append("<FromUserName><![CDATA[" + wxCallBackXmlBO.getToUserName() + "]]></FromUserName>");
        stringBuilder.append("<CreateTime>" + (LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli() / 1000) + "</CreateTime>");
        stringBuilder.append("<MsgType><![CDATA[text]]></MsgType>");
        stringBuilder.append("<Content><![CDATA[" + content + "]]></Content>"); //替换空格，文本信息内容不能包含有空格 .Replace(" ", string.Empty)
        stringBuilder.append("</xml>");
        return stringBuilder.toString();
    }
}
