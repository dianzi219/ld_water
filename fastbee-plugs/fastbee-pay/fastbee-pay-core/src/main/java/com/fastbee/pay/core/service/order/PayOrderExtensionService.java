package com.fastbee.pay.core.service.order;


import com.fastbee.pay.core.domain.dataobject.order.PayOrderExtension;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付订单扩展 Service 接口
 *
 * @author aquan
 */
public interface PayOrderExtensionService {

    PayOrderExtension selectByNo(String no);

    int updateByIdAndStatus(Long id, Integer status, PayOrderExtension update);

    List<PayOrderExtension> selectListByOrderId(Long orderId);

    List<PayOrderExtension> selectListByStatusAndCreateTimeGe(Integer status, LocalDateTime minCreateTime);

    PayOrderExtension selectById(Long id);

    void insert(PayOrderExtension payOrderExtension);
}
