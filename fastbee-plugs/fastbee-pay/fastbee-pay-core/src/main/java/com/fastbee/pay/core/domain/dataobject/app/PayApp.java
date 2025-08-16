package com.fastbee.pay.core.domain.dataobject.app;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fastbee.common.core.domain.BaseDO;
import com.fastbee.common.enums.CommonStatusEnum;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 支付应用 DO
 * 一个商户下，可能会有多个支付应用。例如说，京东有京东商城、京东到家等等
 * 不过一般来说，一个商户，只有一个应用哈~
 * 即 PayMerchantDO : PayApp = 1 : n
 *
 * @author fastbee
 */
@TableName("pay_app")
//@KeySequence("pay_app_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class PayApp extends BaseDO {

    /**
     * 应用编号，数据库自增
     */
    @TableId
    private Long id;
    /**
     * 应用名
     */
    private String name;
    /**
     * 状态
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;
    /**
     * 支付结果的回调地址
     */
    private String orderNotifyUrl;
    /**
     * 退款结果的回调地址
     */
    private String refundNotifyUrl;

}
