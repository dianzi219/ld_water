package com.fastbee.pay.api.enums;

import com.fastbee.common.exception.ErrorCode;
import com.fastbee.common.utils.MessageUtils;


/**
 * Pay 错误码 Core 枚举类
 * pay 系统，使用 1-007-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== APP 模块 1007000000 ==========
    ErrorCode APP_NOT_FOUND = new ErrorCode(1007000000, MessageUtils.message("app.not.found"));
    ErrorCode APP_IS_DISABLE = new ErrorCode(1007000002, MessageUtils.message("app.is.disable"));
    ErrorCode APP_EXIST_ORDER_CANT_DELETE =  new ErrorCode(1007000003, MessageUtils.message("app.exist.order.cant.delete"));
    ErrorCode APP_EXIST_REFUND_CANT_DELETE =  new ErrorCode(1007000004, MessageUtils.message("app.exist.refund.cant.delete"));

    // ========== CHANNEL 模块 1007001000 ==========
    ErrorCode CHANNEL_NOT_FOUND = new ErrorCode(1007001000, MessageUtils.message("channel.not.found"));
    ErrorCode CHANNEL_IS_DISABLE = new ErrorCode(1007001001, MessageUtils.message("channel.is.disable"));
    ErrorCode CHANNEL_EXIST_SAME_CHANNEL_ERROR = new ErrorCode(1007001004, MessageUtils.message("channel.exists.same.channel.error"));

    // ========== ORDER 模块 1007002000 ==========
    ErrorCode ORDER_NOT_FOUND = new ErrorCode(1007002000, MessageUtils.message("order.not.found"));
    ErrorCode ORDER_STATUS_IS_NOT_WAITING = new ErrorCode(1007002001, MessageUtils.message("order.status.is.not.waiting"));
    ErrorCode ORDER_STATUS_IS_SUCCESS = new ErrorCode(1007002002, MessageUtils.message("order.status.is.success"));
    ErrorCode ORDER_IS_EXPIRED = new ErrorCode(1007002003, MessageUtils.message("order.is.expired"));
    ErrorCode ORDER_SUBMIT_CHANNEL_ERROR = new ErrorCode(1007002004, MessageUtils.message("order.submit.channel.error"));
    ErrorCode ORDER_REFUND_FAIL_STATUS_ERROR = new ErrorCode(1007002005, MessageUtils.message("order.refund.fail.status.error"));

    // ========== ORDER 模块(拓展单) 1007003000 ==========
    ErrorCode ORDER_EXTENSION_NOT_FOUND = new ErrorCode(1007003000, MessageUtils.message("order.extension.not.found"));
    ErrorCode ORDER_EXTENSION_STATUS_IS_NOT_WAITING = new ErrorCode(1007003001, MessageUtils.message("order.extension.status.is.not.waiting"));
    ErrorCode ORDER_EXTENSION_IS_PAID = new ErrorCode(1007003002, MessageUtils.message("order.extension.is.paid"));

    // ========== 支付模块(退款) 1007006000 ==========
    ErrorCode REFUND_PRICE_EXCEED = new ErrorCode(1007006000, MessageUtils.message("refund.price.exceed"));
    ErrorCode REFUND_HAS_REFUNDING = new ErrorCode(1007006002, MessageUtils.message("refund.has.refunding"));
    ErrorCode REFUND_EXISTS = new ErrorCode(1007006003, MessageUtils.message("refund.exists"));
    ErrorCode REFUND_NOT_FOUND = new ErrorCode(1007006004, MessageUtils.message("refund.not.found"));
    ErrorCode REFUND_STATUS_IS_NOT_WAITING = new ErrorCode(1007006005, MessageUtils.message("refund.statue.is.not.waiting"));

    // ========== 示例订单 1007900000 ==========
    ErrorCode DEMO_ORDER_NOT_FOUND = new ErrorCode(1007900000, MessageUtils.message("demo.order.not.found"));
    ErrorCode DEMO_ORDER_UPDATE_PAID_STATUS_NOT_UNPAID = new ErrorCode(1007900001, MessageUtils.message("demo.order.update.paid.status.not.unpaid"));
    ErrorCode DEMO_ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR = new ErrorCode(1007900002, MessageUtils.message("demo.order.update.paid.fail.pay.order.id.error"));
    ErrorCode DEMO_ORDER_UPDATE_PAID_FAIL_PAY_ORDER_STATUS_NOT_SUCCESS = new ErrorCode(1007900003, MessageUtils.message("demo.order.update.paid.fail.pay.order.status.not.success"));
    ErrorCode DEMO_ORDER_UPDATE_PAID_FAIL_PAY_PRICE_NOT_MATCH = new ErrorCode(1007900004, MessageUtils.message("demo.order.update.paid.fail.pay.price.not.match"));
    ErrorCode DEMO_ORDER_REFUND_FAIL_NOT_PAID = new ErrorCode(1007900005, MessageUtils.message("demo.order.refund.fail.not.paid"));
    ErrorCode DEMO_ORDER_REFUND_FAIL_REFUNDED = new ErrorCode(1007900006, MessageUtils.message("demo.order.refund.fail.refunded"));
    ErrorCode DEMO_ORDER_REFUND_FAIL_REFUND_NOT_FOUND = new ErrorCode(1007900007, MessageUtils.message("demo.order.refund.fail.refund.not.found"));
    ErrorCode DEMO_ORDER_REFUND_FAIL_REFUND_NOT_SUCCESS = new ErrorCode(1007900008, MessageUtils.message("demo.order.refund.fail.refund.not.success"));
    ErrorCode DEMO_ORDER_REFUND_FAIL_REFUND_ORDER_ID_ERROR = new ErrorCode(1007900009, MessageUtils.message("demo.order.refund.fail.refund.order.id.error"));
    ErrorCode DEMO_ORDER_REFUND_FAIL_REFUND_PRICE_NOT_MATCH = new ErrorCode(1007900010, MessageUtils.message("demo.order.refund.fail.refund.price.not.match"));

}
