package com.fastbee.pay.core.convert.app;

import com.fastbee.common.core.domain.PageResult;
import com.fastbee.common.utils.collection.CollectionUtils;
import com.fastbee.pay.core.controller.admin.app.vo.PayAppCreateReqVO;
import com.fastbee.pay.core.controller.admin.app.vo.PayAppPageItemRespVO;
import com.fastbee.pay.core.controller.admin.app.vo.PayAppRespVO;
import com.fastbee.pay.core.controller.admin.app.vo.PayAppUpdateReqVO;
import com.fastbee.pay.core.domain.dataobject.app.PayApp;
import com.fastbee.pay.core.domain.dataobject.channel.PayChannel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 支付应用信息 Convert
 *
 * @author fastbee
 */
@Mapper
public interface PayAppConvert {

    PayAppConvert INSTANCE = Mappers.getMapper(PayAppConvert.class);

    PayAppPageItemRespVO pageConvert (PayApp bean);

    PayApp convert(PayAppCreateReqVO bean);

    PayApp convert(PayAppUpdateReqVO bean);

    PayAppRespVO convert(PayApp bean);

    List<PayAppRespVO> convertList(List<PayApp> list);

    PageResult<PayAppPageItemRespVO> convertPage(PageResult<PayApp> page);

    default PageResult<PayAppPageItemRespVO> convertPage(PageResult<PayApp> pageResult, List<PayChannel> channels) {
        PageResult<PayAppPageItemRespVO> voPageResult = convertPage(pageResult);
        // 处理 channel 关系
        Map<Long, Set<String>> appIdChannelMap = CollectionUtils.convertMultiMap2(channels, PayChannel::getAppId, PayChannel::getCode);
        voPageResult.getList().forEach(app -> app.setChannelCodes(appIdChannelMap.get(app.getId())));
        return voPageResult;
    }

}
