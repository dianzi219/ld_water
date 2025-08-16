package com.fastbee.notify.core.wechat.vo;

import lombok.Data;

/**
 * @author fastb
 * @version 1.0
 * @description: 获取微信小程序服务通知推送类，推送内容变量参数需自己组装
 * @date 2023-12-28 16:38
 */
@Data
public class WeChatMiniPushVO {

    private WxMssVo wxMssVo;

    private String url;

    private String errorMsg;
}
