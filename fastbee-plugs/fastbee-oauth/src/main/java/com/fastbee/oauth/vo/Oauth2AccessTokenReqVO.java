package com.fastbee.oauth.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * @author fastb
 * @version 1.0
 * @description: TODO
 * @date 2024-03-21 11:13
 */
@Data
public class Oauth2AccessTokenReqVO {

    @JSONField(name = "grant_type")
    private String grantType;

    private String code;

    @JSONField(name = "redirect_uri")
    private String redirectUri;

    private String scope;

    private String state;

}
