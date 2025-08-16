package com.fastbee.pay.core.controller.admin.refund.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@ApiModel(description = "管理后台 - 退款订单分页查询 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayRefundPageItemRespVO extends PayRefundBaseVO {

    @ApiModelProperty(value = "支付订单编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "应用名称", required = true, example = "我是fastbee")
    private String  appName;

    @ApiModelProperty(value = "创建时间", required = true)
    private LocalDateTime createTime;

}
