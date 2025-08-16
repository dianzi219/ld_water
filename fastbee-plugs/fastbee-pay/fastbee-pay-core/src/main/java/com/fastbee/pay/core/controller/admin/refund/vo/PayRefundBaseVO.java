package com.fastbee.pay.core.controller.admin.refund.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
* 退款订单 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class PayRefundBaseVO {

    @ApiModelProperty(value = "外部退款号", required = true, example = "110")
    private String no;

    @ApiModelProperty(value = "应用编号", required = true, example = "1024")
    private Long appId;

    @ApiModelProperty(value = "渠道编号", required = true, example = "2048")
    private Long channelId;

    @ApiModelProperty(value = "渠道编码", required = true, example = "wx_app")
    private String channelCode;

    @ApiModelProperty(value = "订单编号", required = true, example = "1024")
    private Long orderId;

    // ========== 商户相关字段 ==========

    @ApiModelProperty(value = "商户订单编号", required = true, example = "225")
    private String merchantOrderId;

    @ApiModelProperty(value = "商户退款订单号", required = true, example = "512")
    private String merchantRefundId;

    @ApiModelProperty(value = "异步通知地址", required = true)
    private String notifyUrl;

    // ========== 退款相关字段 ==========

    @ApiModelProperty(value = "退款状态", required = true, example = "0")
    private Integer status;

    @ApiModelProperty(value = "支付金额", required = true, example = "100")
    private Long payPrice;

    @ApiModelProperty(value = "退款金额,单位分", required = true, example = "200")
    private Long refundPrice;

    @ApiModelProperty(value = "退款原因", required = true, example = "我要退了")
    private String reason;

    @ApiModelProperty(value = "用户 IP", required = true, example = "127.0.0.1")
    private String userIp;

    // ========== 渠道相关字段 ==========

    @ApiModelProperty(value = "渠道订单号", required = true, example = "233")
    private String channelOrderNo;

    @ApiModelProperty(value = "渠道退款单号", example = "2022")
    private String channelRefundNo;

    @ApiModelProperty(value = "退款成功时间")
    private LocalDateTime successTime;

    @ApiModelProperty(value = "调用渠道的错误码")
    private String channelErrorCode;

    @ApiModelProperty(value = "调用渠道的错误提示")
    private String channelErrorMsg;

    @ApiModelProperty(value = "支付渠道的额外参数")
    private String channelNotifyData;

}
