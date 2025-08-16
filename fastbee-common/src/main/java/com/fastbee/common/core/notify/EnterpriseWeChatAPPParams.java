package com.fastbee.common.core.notify;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dromara.sms4j.provider.config.BaseConfig;

/**
 * 企业微信(应用消息)
 * @author gsb
 * @date 2023/12/11 17:25
 */
@Data
@Accessors(chain = true)
public class EnterpriseWeChatAPPParams extends BaseConfig {

    @ApiModelProperty("企业ID")
    private String corpId;
    @ApiModelProperty("企业应用私钥OA")
    private String corpSecret;
    @ApiModelProperty("企业应用的id")
    private Integer agentId;
    //@ApiModelProperty("token")
    //private String token;
    //@ApiModelProperty("aes秘钥")
    //private String aesKey;

    @ApiModelProperty("模板参数")
    private String params;

    @Override
    public String getSupplier() {
        return null;
    }
}
