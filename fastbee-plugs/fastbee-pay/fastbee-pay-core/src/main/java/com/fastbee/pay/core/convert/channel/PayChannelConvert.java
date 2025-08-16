package com.fastbee.pay.core.convert.channel;

import com.fastbee.common.core.domain.PageResult;
import com.fastbee.pay.core.controller.admin.channel.vo.PayChannelCreateReqVO;
import com.fastbee.pay.core.controller.admin.channel.vo.PayChannelRespVO;
import com.fastbee.pay.core.controller.admin.channel.vo.PayChannelUpdateReqVO;
import com.fastbee.pay.core.domain.dataobject.channel.PayChannel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PayChannelConvert {

    PayChannelConvert INSTANCE = Mappers.getMapper(PayChannelConvert.class);

    @Mapping(target = "config",ignore = true)
    PayChannel convert(PayChannelCreateReqVO bean);

    @Mapping(target = "config",ignore = true)
    PayChannel convert(PayChannelUpdateReqVO bean);

//    @Mapping(target = "config",expression = "java(cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString(bean.getConfig()))")
//    PayChannelRespVO convert(PayChannel bean);

//    PageResult<PayChannelRespVO> convertPage(PageResult<PayChannel> page);

}
