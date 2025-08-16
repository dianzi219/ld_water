package com.fastbee.notify.core.wechat.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Map;

/**
 * @author fastb
 * @version 1.0
 * @description: 微信公众号推送参数
 * @date 2024-03-09 14:10
 */
@Data
public class WeChatPublicAccountPushVO {
    /**
     * 接收者（用户）的 openid
     */
    @JSONField(name = "touser")
    private String touser;
    /**
     * 所需下发的订阅模板id
     */
    @JSONField(name = "template_id")
    private String templateId;
    /**
     * 模板跳转链接（海外账号没有跳转能力）
     */
    @JSONField(name = "url")
    private String url;
    /**
     * 跳小程序所需数据，不需跳小程序可不用传该数据
     */
    @JSONField(name = "miniprogram")
    private MiniProgram miniProgram;
    /**
     * 防重入id。对于同一个openid + client_msg_id, 只发送一条消息,10分钟有效,超过10分钟不保证效果。若无防重入需求，可不填
     */
    @JSONField(name = "client_msg_id")
    private String clientMsgId;
    /**
     * 模板内容，格式形如 { "key1": { "value": any }, "key2": { "value": any } }
     */
    @JSONField(name = "data")
    private Map<String, Object> data;

    @Data
    public static class MiniProgram {

        /**
         * 所需跳转到的小程序appid
         */
        @JSONField(name = "appid")
        private String appId;

        /**
         * 所需跳转到小程序的具体页面路径，支持带参数,（示例index?foo=bar），要求该小程序已发布，暂不支持小游戏
         */
        @JSONField(name = "pagepath")
        private String pagePath;
    }
}
