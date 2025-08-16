package com.fastbee.common.enums;

import com.fastbee.common.core.notify.NotifyConfigVO;
import com.fastbee.common.core.notify.config.DingTalkConfigParams;
import com.fastbee.common.core.notify.config.EmailConfigParams;
import com.fastbee.common.core.notify.config.VoiceConfigParams;
import com.fastbee.common.core.notify.config.WeChatConfigParams;
import com.fastbee.common.core.notify.msg.*;
import com.fastbee.common.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.dromara.sms4j.aliyun.config.AlibabaConfig;
import org.dromara.sms4j.cloopen.config.CloopenConfig;
import org.dromara.sms4j.ctyun.config.CtyunConfig;
import org.dromara.sms4j.emay.config.EmayConfig;
import org.dromara.sms4j.huawei.config.HuaweiConfig;
import org.dromara.sms4j.jdcloud.config.JdCloudConfig;
import org.dromara.sms4j.netease.config.NeteaseConfig;
import org.dromara.sms4j.tencent.config.TencentConfig;
import org.dromara.sms4j.yunpian.config.YunpianConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fastb
 * @version 1.0
 * @description: 通知渠道枚举
 * @date 2023-12-18 11:52
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum NotifyChannelProviderEnum {

    /**** 短信 ******/
    SMS_ALIBABA("sms", "alibaba", "阿里云短信", AlibabaConfig.class, AlibabaConfig.class),
    SMS_TENCENT("sms", "tencent", "腾讯云短信", TencentConfig.class, TencentConfig.class),
    SMS_CTYUN("sms", "ctyun","天翼云短信", CtyunConfig.class, CtyunConfig.class),
    SMS_HUAWEI("sms", "huawei", "华为云短信", HuaweiConfig.class, HuaweiConfig.class),
    SMS_YUNPIAN("sms", "yunpian", "云片短信", YunpianConfig.class, YunpianConfig.class),
    SMS_EMAY("sms", "emay","亿美软通短信", EmayConfig.class, EmayConfig.class),
    SMS_CLOOPEN("sms", "cloopen","容连云短信", CloopenConfig.class, CloopenConfig.class),
    SMS_JDCLOUD("sms", "jdcloud", "京东云短信", JdCloudConfig.class, JdCloudConfig.class),
    SMS_NETEASE("sms", "netease", "网易云短信", NeteaseConfig.class, NeteaseConfig.class),

    /****** 语音 ******/
    VOICE_ALIBABA("voice", "alibaba", "阿里云语音", VoiceConfigParams.class, VoiceMsgParams.class),
    VOICE_TENCENT("voice", "tencent", "腾讯云语音", VoiceMsgParams.class, VoiceMsgParams.class),

    /****** 邮箱 ******/
    EMAIL_QQ("email", "qq", "QQ邮箱", EmailConfigParams.class, EmailMsgParams.class),
    EMAIL_163("email", "163", "163邮箱", EmailConfigParams.class, EmailMsgParams.class),

    /****** 微信 *****/
    WECHAT_MINI_PROGRAM("wechat", "mini_program", "微信小程序(订阅消息)", WeChatConfigParams.class, WechatMsgParams.class),
    WECHAT_WECOM_APPLY("wechat", "wecom_apply", "企业微信应用消息", WeChatConfigParams.class, WechatMsgParams.class),
    WECHAT_WECOM_ROBOT("wechat", "wecom_robot", "企业微信群机器人", WeChatConfigParams.class, WechatMsgParams.class),
    WECHAT_PUBLIC_ACCOUNT("wechat", "public_account", "微信公众号", WeChatConfigParams.class, WechatMsgParams.class),

    /****** 钉钉 *****/
    DING_TALK_WORK("dingtalk", "work", "钉钉工作消息", DingTalkConfigParams.class, DingTalkMsgParams.class),
    DING_TALK_GROUP_ROBOT("dingtalk", "group_robot", "钉钉群机器人", DingTalkConfigParams.class, DingTalkMsgParams.class);

    /**
     * 渠道编码
     */
    private String channelType;

    /**
     * 渠道编码
     */
    private String provider;

    /**
     * 描述
     */
    private String desc;

    /**
     * 渠道配置类
     */
    private Class configContentClass;

    /**
     * 模板配置类
     */
    private Class msgParamsClass;


    public static NotifyChannelProviderEnum getNotifyChannelCodeEnum(String channelCode) {
        for (NotifyChannelProviderEnum channelCodeEnum : NotifyChannelProviderEnum.values()) {
            if (channelCode.equals(channelCodeEnum.channelType)) {
                return channelCodeEnum;
            }
        }
        return null;
    }

    public static NotifyChannelProviderEnum getByChannelTypeAndProvider(String channelType, String provider) {
        for (NotifyChannelProviderEnum channelCodeEnum : NotifyChannelProviderEnum.values()) {
            if (channelType.equals(channelCodeEnum.channelType) && provider.equals(channelCodeEnum.getProvider())) {
                return channelCodeEnum;
            }
        }
        return null;
    }

    /**
     * @description: 获取通知渠道配置信息
     * @param: type
     * @return: java.lang.Object
     */
    public static List<NotifyConfigVO> getConfigContent(NotifyChannelProviderEnum type) {
        List<NotifyConfigVO> configVOList = new ArrayList<>();
        // 务必保证属性（attribute）参数名和各渠道对应配置类configContentClass里的属性名一致
        switch (type) {
            case SMS_ALIBABA:
                configVOList.add(new NotifyConfigVO("accessKeyId", "accessKeyId", "string", ""));
                configVOList.add(new NotifyConfigVO("accessKeySecret", "accessKeySecret", "string", ""));
                break;
//                return new SmsAliConfigParams();
            case SMS_TENCENT:
                configVOList.add(new NotifyConfigVO("accessKeyId", "accessKeyId", "string", ""));
                configVOList.add(new NotifyConfigVO("accessKeySecret", "accessKeySecret", "string", ""));
                break;
//                return new SmsAliConfigParams();
//            case SMS_CTYUN:
//                return new SmsAliConfigParams();
//            case SMS_HUAWEI:
//                return new SmsAliConfigParams();
//            case SMS_YUNPIAN:
//                return new SmsAliConfigParams();
//            case SMS_EMAY:
//                return new SmsAliConfigParams();
//            case SMS_CLOOPEN:
//                return new SmsAliConfigParams();
//            case SMS_JDCLOUD:
//                return new SmsAliConfigParams();
//            case SMS_NETEASE:
//                return new SmsAliConfigParams();
            case VOICE_ALIBABA:
            case VOICE_TENCENT:
                configVOList.add(new NotifyConfigVO("accessKeyId", "accessKeyId", "string", ""));
                configVOList.add(new NotifyConfigVO("accessKeySecret", "accessKeySecret", "string", ""));
                break;
            case EMAIL_QQ:
            case EMAIL_163:
                if (EMAIL_QQ.equals(type)) {
                    configVOList.add(new NotifyConfigVO("smtpServer", "服务器地址", "string","smtp.qq.com"));
                }
                if (EMAIL_163.equals(type)) {
                    configVOList.add(new NotifyConfigVO("smtpServer", "服务器地址", "string","smtp.163.com"));
                }
                configVOList.add(new NotifyConfigVO("port", "端口号", "string", "465"));
                configVOList.add(new NotifyConfigVO("username", "发件人账号", "string", ""));
                configVOList.add(new NotifyConfigVO("password", "发件秘钥", "string", ""));
                configVOList.add(new NotifyConfigVO("sslEnable", "是否启动ssl", "boolean", "true"));
                configVOList.add(new NotifyConfigVO("authEnable", "开启验证", "boolean", "true"));
                configVOList.add(new NotifyConfigVO("retryInterval", "重试间隔（秒）", "int", "5"));
                configVOList.add(new NotifyConfigVO("maxRetries", "重试次数", "int","1"));
                break;
            case WECHAT_MINI_PROGRAM:
            case WECHAT_PUBLIC_ACCOUNT:
                configVOList.add(new NotifyConfigVO("appId", "appId", "string",""));
                configVOList.add(new NotifyConfigVO("appSecret", "appSecret", "string",""));
                break;
            case WECHAT_WECOM_APPLY:
                configVOList.add(new NotifyConfigVO("corpId", "企业ID", "string",""));
                configVOList.add(new NotifyConfigVO("corpSecret", "应用Secret", "string",""));
                configVOList.add(new NotifyConfigVO("agentId", "应用agentId", "string",""));
                break;
            case WECHAT_WECOM_ROBOT:
                configVOList.add(new NotifyConfigVO("webHook", "webHook", "string",""));
                break;
            case DING_TALK_WORK:
                configVOList.add(new NotifyConfigVO("appKey", "appKey", "string",""));
                configVOList.add(new NotifyConfigVO("appSecret", "appSecret", "string",""));
                configVOList.add(new NotifyConfigVO("agentId", "agentId", "string",""));
                break;
            case DING_TALK_GROUP_ROBOT:
                configVOList.add(new NotifyConfigVO("webHook", "webHook", "string",""));
                break;
            default:
                return configVOList;
        }
        return configVOList;
    }

    /**
     * @description: 获取通知模板配置信息
     * @param: type
     * @return: java.lang.Object
     */
    public static List<NotifyConfigVO> getMsgParams(NotifyChannelProviderEnum type, String msgType) {
        List<NotifyConfigVO> configVOList = new ArrayList<>();
        switch (type) {
            // 短信：配置参数来源于sms4j，务必属性字段名和sms4j配置参数字段名一致
            case SMS_ALIBABA:
                configVOList.add(new NotifyConfigVO("sendAccount", "发送电话号", "string",""));
                configVOList.add(new NotifyConfigVO("templateId", "模板CODE", "string",""));
                configVOList.add(new NotifyConfigVO("signature", "签名", "string",""));
                configVOList.add(new NotifyConfigVO("content", "模板内容", "string",""));
                break;
            case SMS_TENCENT:
                configVOList.add(new NotifyConfigVO("sendAccount", "发送电话号", "string",""));
                configVOList.add(new NotifyConfigVO("templateId", "模板ID", "string",""));
                configVOList.add(new NotifyConfigVO("signature", "签名", "string",""));
                configVOList.add(new NotifyConfigVO("sdkAppId", "应用SDKAppID", "string",""));
                configVOList.add(new NotifyConfigVO("content", "模板内容", "string",""));
                break;
            // 邮箱
            case EMAIL_QQ:
            case EMAIL_163:
                configVOList.add(new NotifyConfigVO("sendAccount", "发送邮箱号", "string",""));
                configVOList.add(new NotifyConfigVO("title", "标题", "string",""));
                configVOList.add(new NotifyConfigVO("attachment", "附件", "file",""));
                configVOList.add(new NotifyConfigVO("content", "邮箱正文", "text",""));
                break;
            case WECHAT_MINI_PROGRAM:
                configVOList.add(new NotifyConfigVO("sendAccount", "发送用户ID", "string",""));
                configVOList.add(new NotifyConfigVO("templateId", "模板ID", "string",""));
                configVOList.add(new NotifyConfigVO("redirectUrl", "跳转链接", "string",""));
                configVOList.add(new NotifyConfigVO("content", "模板内容", "string",""));
                break;
            case WECHAT_PUBLIC_ACCOUNT:
                configVOList.add(new NotifyConfigVO("templateId", "模板ID", "string",""));
                configVOList.add(new NotifyConfigVO("redirectUrl", "跳转链接", "string",""));
                configVOList.add(new NotifyConfigVO("appid", "跳转小程序appid", "string",""));
                configVOList.add(new NotifyConfigVO("pagePath", "跳转小程序路径", "string",""));
                configVOList.add(new NotifyConfigVO("content", "模板内容", "string",""));
            case WECHAT_WECOM_APPLY:
            case WECHAT_WECOM_ROBOT:
                if (StringUtils.isEmpty(msgType)) {
                    return configVOList;
                }
                if (type.equals(WECHAT_WECOM_APPLY)) {
                    configVOList.add(new NotifyConfigVO("sendAccount", "发送成员账号", "string",""));
                }
                switch (msgType) {
                    case "text":
                    case "markdown":
                        configVOList.add(new NotifyConfigVO("content", "消息内容", "string",""));
                        break;
                    case "news":
                        configVOList.add(new NotifyConfigVO("title", "消息标题", "string",""));
                        configVOList.add(new NotifyConfigVO("content", "消息内容", "string",""));
                        configVOList.add(new NotifyConfigVO("url", "跳转链接", "string",""));
                        configVOList.add(new NotifyConfigVO("picUrl", "图片链接", "file",""));
                        break;
                    default:
                        break;
                }
                break;
            // 语音
            case VOICE_ALIBABA:
                configVOList.add(new NotifyConfigVO("sendAccount", "发送电话号", "string",""));
                configVOList.add(new NotifyConfigVO("templateId", "模板ID", "string",""));
                configVOList.add(new NotifyConfigVO("content", "模板内容", "string",""));
                configVOList.add(new NotifyConfigVO("playTimes", "播放次数 (1~3)", "int","2"));
                configVOList.add(new NotifyConfigVO("volume", "播放音量 (0-100)", "string","50"));
                configVOList.add(new NotifyConfigVO("speed", "语速控制 (-500-500)", "string","0"));
                break;
            case VOICE_TENCENT:
                configVOList.add(new NotifyConfigVO("sendAccount", "发送电话号", "string",""));
                configVOList.add(new NotifyConfigVO("sdkAppId", "应用SDKAppID", "string",""));
                configVOList.add(new NotifyConfigVO("templateId", "模板ID", "string",""));
                configVOList.add(new NotifyConfigVO("content", "模板内容", "string",""));
                break;
            // 钉钉
            case DING_TALK_WORK:
            case DING_TALK_GROUP_ROBOT:
                if (StringUtils.isEmpty(msgType)) {
                    return configVOList;
                }
                switch (msgType) {
                    case "text":
                        if (NotifyChannelProviderEnum.DING_TALK_WORK.equals(type)) {
                            configVOList.add(new NotifyConfigVO("deptId", "部门id", "string",""));
                            configVOList.add(new NotifyConfigVO("sendAllEnable", "发送所有人", "boolean","false"));
                            configVOList.add(new NotifyConfigVO("sendAccount", "员工UserID", "string",""));
                        }
                        configVOList.add(new NotifyConfigVO("content", "消息内容", "string",""));
                        break;
                    case "link":
                        if (NotifyChannelProviderEnum.DING_TALK_WORK.equals(type)) {
                            configVOList.add(new NotifyConfigVO("deptId", "部门id", "string",""));
                            configVOList.add(new NotifyConfigVO("sendAllEnable", "发送所有人", "boolean","false"));
                            configVOList.add(new NotifyConfigVO("sendAccount", "员工UserID", "string",""));
                        }
                        configVOList.add(new NotifyConfigVO("title", "消息标题", "string",""));
                        configVOList.add(new NotifyConfigVO("content", "消息内容", "string",""));
                        configVOList.add(new NotifyConfigVO("messageUrl", "消息链接", "string",""));
                        configVOList.add(new NotifyConfigVO("picUrl", "图片链接", "file",""));
                        break;
                    case "markdown":
                        if (NotifyChannelProviderEnum.DING_TALK_WORK.equals(type)) {
                            configVOList.add(new NotifyConfigVO("deptId", "部门id", "string",""));
                            configVOList.add(new NotifyConfigVO("sendAllEnable", "发送所有人", "boolean","false"));
                            configVOList.add(new NotifyConfigVO("sendAccount", "员工UserID", "string",""));
                        }
                        configVOList.add(new NotifyConfigVO("title", "消息标题", "string",""));
                        configVOList.add(new NotifyConfigVO("content", "消息内容", "string",""));
                        break;
                    default:
                        break;
                }
                break;
            default:
                return configVOList;
        }
        return configVOList;
    }
}
