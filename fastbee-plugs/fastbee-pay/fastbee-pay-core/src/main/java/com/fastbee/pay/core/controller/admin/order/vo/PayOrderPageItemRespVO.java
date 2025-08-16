package com.fastbee.pay.core.controller.admin.order.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@ApiModel(description = "管理后台 - 支付订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayOrderPageItemRespVO extends PayOrderBaseVO {

    @ApiModelProperty(value = "支付订单编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "应用名称", example = "wx_pay")
    private String  appName;

}
