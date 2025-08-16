package com.fastbee.pay.core.domain.dataobject.notify;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fastbee.common.core.domain.BaseDO;
import com.fastbee.pay.api.enums.notify.PayNotifyStatusEnum;
import lombok.*;

/**
 * 商户支付、退款等的通知 Log
 * 每次通知时，都会在该表中，记录一次 Log，方便排查问题
 *
 * @author fastbee
 */
@TableName("pay_notify_log")
@KeySequence("pay_notify_log_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayNotifyLog extends BaseDO {

    /**
     * 日志编号，自增
     */
    private Long id;
    /**
     * 通知任务编号
     * 关联 {@link PayNotifyTask#getId()}
     */
    private Long taskId;
    /**
     * 第几次被通知
     * 对应到 {@link PayNotifyTask#getNotifyTimes()}
     */
    private Integer notifyTimes;
    /**
     * HTTP 响应结果
     */
    private String response;
    /**
     * 支付通知状态
     * 外键 {@link PayNotifyStatusEnum}
     */
    private Integer status;

}
