package com.fastbee.pay.core.convert.demo;

import com.fastbee.common.core.domain.PageResult;
import com.fastbee.pay.core.controller.admin.demo.vo.OrderInfoCreateReqVO;
import com.fastbee.pay.core.controller.admin.demo.vo.OrderInfoRespVO;
import com.fastbee.pay.core.domain.dataobject.demo.OrderInfo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 示例订单 Convert
 *
 * @author fastbee
 */
@Mapper
public interface OrderInfoConvert {

    OrderInfoConvert INSTANCE = Mappers.getMapper(OrderInfoConvert.class);

    OrderInfo convert(OrderInfoCreateReqVO bean);

    OrderInfoRespVO convert(OrderInfo bean);

    PageResult<OrderInfoRespVO> convertPage(PageResult<OrderInfo> page);

}
