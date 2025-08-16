package com.fastbee.pay.core.controller.admin.channel.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@ApiModel(description = "管理后台 - 支付渠道 创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayChannelCreateReqVO extends PayChannelBaseVO {

    @ApiModelProperty(value = "渠道编码", required = true, example = "alipay_pc")
    @NotNull(message = "渠道编码不能为空")
    private String code;

    @ApiModelProperty(value = "渠道配置的 json 字符串")
    @NotBlank(message = "渠道配置不能为空")
    private String config;

}
