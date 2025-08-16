package com.fastbee.pay.core.domain.dataobject.demo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fastbee.common.core.domain.BaseDO;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 订单信息, 实际根据自己的业务系统设计
 * 用id关联 pay 系统的支付与退款
 *
 * @author fastbee
 */
@TableName("order_info")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class OrderInfo extends BaseDO {

    /**
     * 订单编号，自增
     */
    @TableId
    private Long id;
    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 商户显示订单编号
     */
    private String orderNo;
    /**
     * 商品编号
     */
    private Long spuId;
    /**
     * 商品名称
     */
    private String spuName;
    /**
     * 价格，单位：分
     */
    private Integer price;

    // ========== 支付相关字段 ==========
    /**
     * 是否支付
     */
    private Boolean payStatus;
    /**
     * 支付订单编号
     * 对接 pay-module-biz 支付服务的支付订单编号，即 PayOrder 的 id 编号
     */
    private Long payOrderId;
    /**
     * 付款时间
     */
    private LocalDateTime payTime;
    /**
     * 支付渠道
     * 对应 PayChannelEnum 枚举
     */
    private String payChannelCode;

    // ========== 退款相关字段 ==========
    /**
     * 支付退款单号
     */
    private Long payRefundId;
    /**
     * 退款金额，单位：分
     */
    private Integer refundPrice;
    /**
     * 退款完成时间
     */
    private LocalDateTime refundTime;

}
