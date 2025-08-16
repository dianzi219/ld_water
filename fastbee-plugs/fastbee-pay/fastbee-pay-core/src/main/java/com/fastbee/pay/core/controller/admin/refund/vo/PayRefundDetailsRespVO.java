package com.fastbee.pay.core.controller.admin.refund.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@ApiModel(description = "管理后台 - 退款订单详情 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayRefundDetailsRespVO extends PayRefundBaseVO {

    @ApiModelProperty(value = "支付退款编号", required = true)
    private Long id;

    @ApiModelProperty(value = "应用名称", required = true, example = "我是fastbee")
    private String appName;

    @ApiModelProperty(value = "支付订单", required = true)
    private Order order;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModel(value = "管理后台 - 支付订单")
    @Data
    public static class Order {

        @ApiModelProperty(value = "商品标题", required = true, example = "土豆")
        private String subject;

    }

}
