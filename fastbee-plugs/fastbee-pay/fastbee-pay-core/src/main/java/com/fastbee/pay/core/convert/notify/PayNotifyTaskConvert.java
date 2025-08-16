package com.fastbee.pay.core.convert.notify;

import com.fastbee.common.core.domain.PageResult;
import com.fastbee.common.utils.MapUtils;
import com.fastbee.pay.core.controller.admin.notify.vo.PayNotifyTaskDetailRespVO;
import com.fastbee.pay.core.controller.admin.notify.vo.PayNotifyTaskRespVO;
import com.fastbee.pay.core.domain.dataobject.app.PayApp;
import com.fastbee.pay.core.domain.dataobject.notify.PayNotifyLog;
import com.fastbee.pay.core.domain.dataobject.notify.PayNotifyTask;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

/**
 * 支付通知 Convert
 *
 * @author fastbee
 */
@Mapper
public interface PayNotifyTaskConvert {

    PayNotifyTaskConvert INSTANCE = Mappers.getMapper(PayNotifyTaskConvert.class);

    PayNotifyTaskRespVO convert(PayNotifyTask bean);

    default PageResult<PayNotifyTaskRespVO> convertPage(PageResult<PayNotifyTask> page, Map<Long, PayApp> appMap){
        PageResult<PayNotifyTaskRespVO> result = convertPage(page);
        result.getList().forEach(order -> MapUtils.findAndThen(appMap, order.getAppId(), app -> order.setAppName(app.getName())));
        return result;
    }
    PageResult<PayNotifyTaskRespVO> convertPage(PageResult<PayNotifyTask> page);

    default PayNotifyTaskDetailRespVO convert(PayNotifyTask task, PayApp app, List<PayNotifyLog> logs) {
        PayNotifyTaskDetailRespVO respVO = convert(task, logs);
        if (app != null) {
            respVO.setAppName(app.getName());
        }
        return respVO;
    }
    PayNotifyTaskDetailRespVO convert(PayNotifyTask task, List<PayNotifyLog> logs);
}
