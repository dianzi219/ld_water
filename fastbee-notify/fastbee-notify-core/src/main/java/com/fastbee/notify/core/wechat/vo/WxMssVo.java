package com.fastbee.notify.core.wechat.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Map;

/*
 * 小程序推送所需数据
 * */
@Data
public class WxMssVo {
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
     * 点击模板卡片后的跳转页面，仅限本小程序内的页面。支持带参数,（示例index?foo=bar）。该字段不填则模板无跳转。
     */
    @JSONField(name = "page")
    private String page = "pages/index/index";
    /**
     * 模板内容，格式形如 { "key1": { "value": any }, "key2": { "value": any } }
     */
    @JSONField(name = "data")
    private Map<String, Object> data;
    /**
     * 跳转小程序类型：developer为开发版；trial为体验版；formal为正式版；默认为正式版
     */
    @JSONField(name = "miniprogram_state")
    private String miniprogramState;
    /**
     * 进入小程序查看”的语言类型，支持zh_CN(简体中文)、en_US(英文)、zh_HK(繁体中文)、zh_TW(繁体中文)，默认为zh_CN
     */
    @JSONField(name = "lang")
    private String lang;
    /**
     * 默认正式版 和 简体中文
     */
    public WxMssVo() {
        this.miniprogramState = "formal";
        this.lang = "zh_CN";
    }
}
