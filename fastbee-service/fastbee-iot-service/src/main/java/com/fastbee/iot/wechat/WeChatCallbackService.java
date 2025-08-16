package com.fastbee.iot.wechat;

/**
 * 微信回调相关服务类
 * @author fastb
 * @date 2024-03-12 15:26
 * @version 1.0
 */
public interface WeChatCallbackService {

    /**
     * 处理微信公众号回调信息
     * @param xmlStr xml消息体
     * @return java.lang.String
     */
    String handleCallback(String xmlStr);
}
