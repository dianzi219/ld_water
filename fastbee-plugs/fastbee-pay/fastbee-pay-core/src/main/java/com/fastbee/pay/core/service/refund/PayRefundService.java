package com.fastbee.pay.core.service.refund;


import com.fastbee.common.core.domain.PageResult;
import com.fastbee.pay.api.api.refund.dto.PayRefundCreateReqDTO;
import com.fastbee.pay.core.controller.admin.refund.vo.PayRefundExportReqVO;
import com.fastbee.pay.core.controller.admin.refund.vo.PayRefundPageReqVO;
import com.fastbee.pay.core.domain.dataobject.refund.PayRefund;
import com.fastbee.pay.framework.client.dto.refund.PayRefundRespDTO;

import java.util.List;

/**
 * 退款订单 Service 接口
 *
 * @author aquan
 */
public interface PayRefundService {

    /**
     * 获得退款订单
     *
     * @param id 编号
     * @return 退款订单
     */
    PayRefund getRefund(Long id);

    /**
     * 获得指定应用的退款数量
     *
     * @param appId 应用编号
     * @return 退款数量
     */
    Long getRefundCountByAppId(Long appId);

    /**
     * 获得退款订单分页
     *
     * @param pageReqVO 分页查询
     * @return 退款订单分页
     */
    PageResult<PayRefund> getRefundPage(PayRefundPageReqVO pageReqVO);

    /**
     * 获得退款订单列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 退款订单列表
     */
    List<PayRefund> getRefundList(PayRefundExportReqVO exportReqVO);

    /**
     * 创建退款申请
     *
     * @param reqDTO 退款申请信息
     * @return 退款单号
     */
    Long createPayRefund(PayRefundCreateReqDTO reqDTO);

    /**
     * 渠道的退款通知
     *
     * @param channelId  渠道编号
     * @param notify     通知
     */
    void notifyRefund(Long channelId, PayRefundRespDTO notify);

    /**
     * 同步渠道退款的退款状态
     *
     * @return 同步到状态的退款数量，包括退款成功、退款失败
     */
    int syncRefund();

    Long selectCountByAppIdAndOrderId(Long appId, Long orderId, Integer status);

    PayRefund selectByAppIdAndNo(Long appId, String no);

    int updateByIdAndStatus(Long id, Integer status, PayRefund update);

}
