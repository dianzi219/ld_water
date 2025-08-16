package com.fastbee.pay.core.domain.mapper.order;

import com.fastbee.framework.mybatis.mapper.BaseMapperX;
import com.fastbee.pay.core.domain.dataobject.order.PayOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PayOrderMapper extends BaseMapperX<PayOrder> {

}
