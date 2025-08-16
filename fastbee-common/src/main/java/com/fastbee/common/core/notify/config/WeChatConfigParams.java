package com.fastbee.common.core.notify.config;

import lombok.Data;

/**
 * 微信推送配置参数
 * @author gsb
 * @date 2023/12/11 17:11
 */
@Data
public class WeChatConfigParams {

    private String appId;

    private String appSecret;

    private String corpId;

    private String corpSecret;

    private String agentId;

    private String webHook;

}
