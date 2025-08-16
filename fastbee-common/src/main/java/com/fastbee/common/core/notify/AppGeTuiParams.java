package com.fastbee.common.core.notify;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dromara.sms4j.provider.config.BaseConfig;

/**
 * 个推参数配置
 * @author gsb
 * @date 2023/12/11 17:14
 */
@Data
@Accessors(chain = true)
public class AppGeTuiParams extends BaseConfig {

    @ApiModelProperty("appId")
    private String appId;

    @ApiModelProperty("appKey")
    private String appKey;

    @ApiModelProperty("秘钥")
    private String masterSecret;

    @ApiModelProperty("模板参数")
    private String params;

    @Override
    public String getSupplier() {
        return null;
    }
}
