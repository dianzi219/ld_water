package com.fastbee.pay.core.controller.admin.channel.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@ApiModel(description = "管理后台 - 支付渠道 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayChannelRespVO extends PayChannelBaseVO {

    @ApiModelProperty(value = "商户编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true, example = "1024")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "渠道编码", required = true, example = "alipay_pc")
    private String code;

    @ApiModelProperty(value = "配置", required = true)
    private String config;

}
