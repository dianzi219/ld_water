package com.fastbee.pay.core.job.order;

import cn.hutool.core.util.StrUtil;
import com.fastbee.pay.core.job.notify.JobHandler;
import com.fastbee.pay.core.service.order.PayOrderService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 支付订单的过期 Job
 * 支付超过过期时间时，支付渠道是不会通知进行过期，所以需要定时进行过期关闭。
 *
 * @author fastbee
 */
@Component
//@TenantJob
public class PayOrderExpireJob implements JobHandler {

    @Resource
    private PayOrderService orderService;

    @Override
    public String execute(String param) {
        int count = orderService.expireOrder();
        return StrUtil.format("支付过期 {} 个", count);
    }

}
