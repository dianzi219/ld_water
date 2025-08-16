package com.fastbee.pay.core.domain.dataobject.order;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fastbee.common.core.domain.BaseDO;
import com.fastbee.pay.api.enums.order.PayOrderStatusEnum;
import com.fastbee.pay.core.domain.dataobject.app.PayApp;
import com.fastbee.pay.core.domain.dataobject.channel.PayChannel;
import com.fastbee.pay.framework.enums.channel.PayChannelEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 支付订单 DO
 *
 * @author fastbee
 */
@TableName("pay_order")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class PayOrder extends BaseDO {

    /**
     * 订单编号，数据库自增
     */
    private Long id;
    /**
     * 应用编号
     * 关联 {@link PayApp#getId()}
     */
    private Long appId;
    /**
     * 渠道编号
     * 关联 {@link PayChannel#getId()}
     */
    private Long channelId;
    /**
     * 渠道编码
     * 枚举 {@link PayChannelEnum}
     */
    private String channelCode;

    // ========== 商户相关字段 ==========

    /**
     * 商户订单编号
     * 例如说，内部系统 A 的订单号，需要保证每个 PayApp 唯一
     */
    private String merchantOrderId;
    /**
     * 商品标题
     */
    private String subject;
    /**
     * 商品描述信息
     */
    private String body;
    /**
     * 异步通知地址
     */
    private String notifyUrl;

    // ========== 订单相关字段 ==========

    /**
     * 支付金额，单位：分
     */
    private Integer price;
    /**
     * 渠道手续费，单位：百分比
     * 冗余 {@link PayChannel#getFeeRate()}
     */
    private Double channelFeeRate;
    /**
     * 渠道手续金额，单位：分
     */
    private Integer channelFeePrice;
    /**
     * 支付状态
     * 枚举 {@link PayOrderStatusEnum}
     */
    private Integer status;
    /**
     * 用户 IP
     */
    private String userIp;
    /**
     * 订单失效时间
     */
    private LocalDateTime expireTime;
    /**
     * 订单支付成功时间
     */
    private LocalDateTime successTime;
    /**
     * 支付成功的订单拓展单编号
     * 关联 {@link PayOrderExtension#getId()}
     */
    private Long extensionId;
    /**
     * 支付成功的外部订单号
     * 关联 {@link PayOrderExtension#getNo()}
     */
    private String no;

    // ========== 退款相关字段 ==========
    /**
     * 退款总金额，单位：分
     */
    private Integer refundPrice;

    // ========== 渠道相关字段 ==========
    /**
     * 渠道用户编号
     * 例如说，微信 openid、支付宝账号
     */
    private String channelUserId;
    /**
     * 渠道订单号
     */
    private String channelOrderNo;

}
