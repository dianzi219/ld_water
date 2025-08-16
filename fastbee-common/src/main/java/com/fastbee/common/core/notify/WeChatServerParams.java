package com.fastbee.common.core.notify;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dromara.sms4j.provider.config.BaseConfig;

/**
 * 微信服务号推送参数
 * @author gsb
 * @date 2023/12/11 17:11
 */
@Data
@Accessors(chain = true)
public class WeChatServerParams {

    @ApiModelProperty("appId")
    private String appId;

    @ApiModelProperty("app秘钥")
    private String secret;

    @ApiModelProperty("模板ID")
    private String templateId;

    @ApiModelProperty("跳转地址")
    private String page;

    @ApiModelProperty("模板参数")
    private String params;

}
