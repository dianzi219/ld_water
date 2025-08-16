package com.fastbee.pay.core.controller.admin.order.vo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static com.fastbee.common.utils.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;


/**
 * 支付订单 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 *
 * @author aquan
 */
@Data
public class PayOrderBaseVO {

    @ApiModelProperty(value = "应用编号", required = true, example = "1024")
    @NotNull(message = "应用编号不能为空")
    private Long appId;

    @ApiModelProperty(value = "渠道编号", example = "2048")
    private Long channelId;

    @ApiModelProperty(value = "渠道编码", example = "wx_app")
    private String channelCode;

    @ApiModelProperty(value = "商户订单编号", required = true, example = "888")
    @NotNull(message = "商户订单编号不能为空")
    private String merchantOrderId;

    @ApiModelProperty(value = "商品标题", required = true, example = "土豆")
    @NotNull(message = "商品标题不能为空")
    private String subject;

    @ApiModelProperty(value = "商品描述", required = true, example = "我是土豆")
    @NotNull(message = "商品描述不能为空")
    private String body;

    @ApiModelProperty(value = "异步通知地址", required = true, example = "http://127.0.0.1:48080/pay/notify")
    @NotNull(message = "异步通知地址不能为空")
    private String notifyUrl;

    @ApiModelProperty(value = "支付金额，单位：分", required = true, example = "10")
    @NotNull(message = "支付金额，单位：分不能为空")
    private Long price;

    @ApiModelProperty(value = "渠道手续费，单位：百分比", example = "10")
    private Double channelFeeRate;

    @ApiModelProperty(value = "渠道手续金额，单位：分", example = "100")
    private Integer channelFeePrice;

    @ApiModelProperty(value = "支付状态", required = true, example = "1")
    @NotNull(message = "支付状态不能为空")
    private Integer status;

    @ApiModelProperty(value = "用户 IP", required = true, example = "127.0.0.1")
    @NotNull(message = "用户 IP不能为空")
    private String userIp;

    @ApiModelProperty(value = "订单失效时间", required = true)
    @NotNull(message = "订单失效时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime expireTime;

    @ApiModelProperty(value = "订单支付成功时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime successTime;

    @ApiModelProperty(value = "支付成功的订单拓展单编号", example = "50")
    private Long extensionId;

    @ApiModelProperty(value = "支付订单号", example = "2048888")
    private String no;

    @ApiModelProperty(value = "退款总金额，单位：分", required = true, example = "10")
    @NotNull(message = "退款总金额，单位：分不能为空")
    private Long refundPrice;

    @ApiModelProperty(value = "渠道用户编号", example = "2048")
    private String channelUserId;

    @ApiModelProperty(value = "渠道订单号", example = "4096")
    private String channelOrderNo;

}
