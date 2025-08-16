package com.fastbee.pay.framework.client.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 支付系统异常 Exception
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PayException extends RuntimeException {

    public PayException(Throwable cause) {
        super(cause);
    }

}
