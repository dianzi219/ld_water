package com.fastbee.pay.core.domain.mapper.demo;

import com.fastbee.framework.mybatis.mapper.BaseMapperX;
import com.fastbee.pay.core.domain.dataobject.demo.OrderInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 示例订单 Mapper
 *
 * @author fastbee
 */
@Mapper
public interface OrderInfoMapper extends BaseMapperX<OrderInfo> {

}
