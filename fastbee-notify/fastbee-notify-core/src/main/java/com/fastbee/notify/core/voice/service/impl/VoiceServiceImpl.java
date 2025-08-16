package com.fastbee.notify.core.voice.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.aliyun.dyvmsapi20170525.Client;
import com.aliyun.dyvmsapi20170525.models.SingleCallByTtsRequest;
import com.aliyun.dyvmsapi20170525.models.SingleCallByTtsResponse;
import com.aliyun.teautil.models.RuntimeOptions;
import com.fastbee.common.core.notify.NotifySendResponse;
import com.fastbee.common.core.notify.config.VoiceConfigParams;
import com.fastbee.common.core.notify.msg.VoiceMsgParams;
import com.fastbee.common.enums.NotifyChannelProviderEnum;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.notify.core.voice.config.VoiceConfig;
import com.fastbee.notify.core.voice.service.VoiceService;
import com.fastbee.notify.vo.NotifyVO;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.vms.v20200902.VmsClient;
import com.tencentcloudapi.vms.v20200902.models.SendTtsVoiceRequest;
import com.tencentcloudapi.vms.v20200902.models.SendTtsVoiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author fastb
 * @version 1.0
 * @description: 语音通知发送业务类
 * @date 2023-12-26 9:54
 */
@Slf4j
@Service
public class VoiceServiceImpl implements VoiceService {

    @Override
    public NotifySendResponse send(NotifyVO notifyVO) {
        NotifySendResponse notifySendResponse = new NotifySendResponse();
        VoiceConfigParams configParams = JSONObject.parseObject(notifyVO.getNotifyChannel().getConfigContent(), VoiceConfigParams.class);
        VoiceMsgParams msgParams = JSONObject.parseObject(notifyVO.getNotifyTemplate().getMsgParams(), VoiceMsgParams.class);
        LinkedHashMap<String, String> map = notifyVO.getMap();
        NotifyChannelProviderEnum notifyChannelProviderEnum = notifyVO.getNotifyChannelProviderEnum();
        if (StringUtils.isEmpty(notifyVO.getSendAccount())) {
            notifySendResponse.setStatus(0);
            notifySendResponse.setResultContent("发送电话不能为空，请先配置！");
            return notifySendResponse;
        }
        String phoneStr = notifyVO.getSendAccount();
        List<String> phoneList = StringUtils.str2List(phoneStr, ",", true, true);
        String sendContent = "";
        List<String> resultList = new ArrayList<>();
        for (String phone : phoneList) {
            switch (notifyChannelProviderEnum) {
                case VOICE_ALIBABA:
                    sendContent = StringUtils.strReplaceVariable("${", "}", msgParams.getContent(), map);
                    try {
                        notifySendResponse = singleCallByTts(configParams, msgParams, map, phone);
                    } catch (Exception e) {
                        log.error("阿里云语音通知异常，phone:{}, exception:{}", phone, e.toString());
                    }
                    break;
                case VOICE_TENCENT:
                    sendContent = StringUtils.strReplaceVariable("{", "}", msgParams.getContent(), map);
                    notifySendResponse = this.sendTtsVoice(configParams, msgParams, map, phone);
                    break;
                default:
                    break;
            }
            resultList.add("phone:[" + phone + "]，status:[" + notifySendResponse.getStatus() + "]，resultContent:["  + notifySendResponse.getResultContent() + "]");
        }
        notifySendResponse.setSendContent(sendContent);
        notifySendResponse.setResultContent(StringUtils.join(resultList, ";  "));
        return notifySendResponse;
    }



    /**
     * @description: 阿里云文本转语音通知
     * @param: configParams 渠道服务商配置参数
     * @param: msgParams 模版通知内容
     * @param: map 变量参数
     * @param: phone 通知电话
     * @return: com.aliyun.dyvmsapi20170525.models.SingleCallByTtsResponse
     */
    public NotifySendResponse singleCallByTts(VoiceConfigParams configParams, VoiceMsgParams msgParams, LinkedHashMap<String, String> map, String phone) throws Exception {
        NotifySendResponse notifySendResponse = new NotifySendResponse();
        try {
            Client client = VoiceConfig.createClient(configParams.getAccessKeyId(), configParams.getAccessKeySecret());
            SingleCallByTtsRequest singleCallByTtsRequest = new SingleCallByTtsRequest()
                    .setTtsCode(msgParams.getTemplateId())
                    .setCalledNumber(phone)
                    .setTtsParam(JSON.toJSONString(map))
                    .setPlayTimes(StringUtils.isNotEmpty(msgParams.getPlayTimes()) ? Integer.parseInt(msgParams.getPlayTimes()) : 1)
                    .setVolume(StringUtils.isNotEmpty(msgParams.getVolume()) ? Integer.parseInt(msgParams.getVolume()) : 50)
                    .setSpeed(StringUtils.isNotEmpty(msgParams.getSpeed()) ? Integer.parseInt(msgParams.getSpeed()) : 0);
            RuntimeOptions runtimeOptions = new RuntimeOptions();
            SingleCallByTtsResponse singleCallByTtsResponse = client.singleCallByTtsWithOptions(singleCallByTtsRequest, runtimeOptions);
            notifySendResponse.setStatus("OK".equals(singleCallByTtsResponse.getBody().getCode()) ? 1 : 0);
            notifySendResponse.setResultContent(JSON.toJSONString(singleCallByTtsResponse.getBody()));
        } catch (Exception e) {
            notifySendResponse.setStatus(0);
            notifySendResponse.setResultContent(e.toString());
        }
        return notifySendResponse;
    }

    public NotifySendResponse sendTtsVoice(VoiceConfigParams configParams, VoiceMsgParams msgParams, LinkedHashMap<String, String> map, String phone) {
        NotifySendResponse notifySendResponse = new NotifySendResponse();
        try {
            /* 必要步骤：
             * 实例化一个认证对象，入参需要传入腾讯云账户密钥对secretId，secretKey。
             * 这里采用的是从环境变量读取的方式，需要在环境变量中先设置这两个值。
             * 您也可以直接在代码中写死密钥对，但是小心不要将代码复制、上传或者分享给他人，
             * 以免泄露密钥对危及您的财产安全。
             * CAM密匙查询: https://console.cloud.tencent.com/cam/capi*/
            Credential cred = new Credential(configParams.getAccessKeyId(), configParams.getAccessKeySecret());


            // 实例化一个http选项，可选，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            // 设置代理
//             httpProfile.setProxyHost("host");
//             httpProfile.setProxyPort(port);
            // SDK默认使用POST方法。
            // 如果您一定要使用GET方法，可以在这里设置。GET方法无法处理一些较大的请求
            httpProfile.setReqMethod("POST");
            /* SDK有默认的超时时间，非必要请不要进行调整
             * 如有需要请在代码中查阅以获取最新的默认值 */
            httpProfile.setConnTimeout(60);
            /* SDK会自动指定域名。通常是不需要特地指定域名的，但是如果您访问的是金融区的服务
             * 则必须手动指定域名，例如vms的上海金融区域名： vms.ap-shanghai-fsi.tencentcloudapi.com */
            httpProfile.setEndpoint("vms.tencentcloudapi.com");


            /* 非必要步骤:
             * 实例化一个客户端配置对象，可以指定超时时间等配置 */
            ClientProfile clientProfile = new ClientProfile();
            /* SDK默认用TC3-HMAC-SHA256进行签名
             * 非必要请不要修改这个字段 */
            clientProfile.setSignMethod("TC3-HMAC-SHA256");
            clientProfile.setHttpProfile(httpProfile);
            /* 实例化要请求产品(以vms为例)的client对象
             * 第二个参数是地域信息，可以直接填写字符串ap-guangzhou，或者引用预设的常量 */
            VmsClient client = new VmsClient(cred, "ap-guangzhou", clientProfile);
            /* 实例化一个请求对象，根据调用的接口和实际情况，可以进一步设置请求参数
             * 您可以直接查询SDK源码确定接口有哪些属性可以设置
             * 属性可能是基本类型，也可能引用了另一个数据结构
             * 推荐使用IDE进行开发，可以方便的跳转查阅各个接口和数据结构的文档说明 */
            SendTtsVoiceRequest req = new SendTtsVoiceRequest();


            /* 填充请求参数,这里request对象的成员变量即对应接口的入参
             * 您可以通过官网接口文档或跳转到request对象的定义处查看请求参数的定义
             * 基本类型的设置:
             * 帮助链接：
             * 语音消息控制台：https://console.cloud.tencent.com/vms
             * vms helper：https://cloud.tencent.com/document/product/1128/37720 */


            // 模板 ID，必须填写在控制台审核通过的模板 ID，可登录 [语音消息控制台] 查看模板 ID
            String templateId = msgParams.getTemplateId();
            req.setTemplateId(templateId);


            // 模板参数，若模板没有参数，请提供为空数组
            String[] templateParamSet = map.values().toArray(new String[0]);;
            req.setTemplateParamSet(templateParamSet);


            /* 被叫手机号码，采用 e.164 标准，格式为+[国家或地区码][用户号码]
             * 例如：+8613711112222，其中前面有一个+号，86为国家码，13711112222为手机号 */
            String calledNumber = "+86" + phone;
            req.setCalledNumber(calledNumber);


            // 在 [语音控制台] 添加应用后生成的实际SdkAppid，示例如1400006666
            String voiceSdkAppid = msgParams.getSdkAppId();
            req.setVoiceSdkAppid(voiceSdkAppid);


            // 播放次数，可选，最多3次，默认2次
            Long playTimes = 2L;
            req.setPlayTimes(playTimes);


            // 用户的 session 内容，腾讯 server 回包中会原样返回
            String sessionContext = phone;
            req.setSessionContext(sessionContext);


            /* 通过 client 对象调用 SendTtsVoice 方法发起请求。注意请求方法名与请求对象是对应的
             * 返回的 res 是一个 SendTtsVoiceResponse 类的实例，与请求对象对应 */
            SendTtsVoiceResponse response = client.SendTtsVoice(req);
            notifySendResponse.setStatus(1);
            notifySendResponse.setResultContent(JSON.toJSONString(response));


        } catch (TencentCloudSDKException e) {
//            log.error("腾讯云语音通知异常，phone:{}, exception:{}", phone, e.toString());
            notifySendResponse.setStatus(0);
            notifySendResponse.setResultContent(e.toString());
        }
        return notifySendResponse;
    }

}
