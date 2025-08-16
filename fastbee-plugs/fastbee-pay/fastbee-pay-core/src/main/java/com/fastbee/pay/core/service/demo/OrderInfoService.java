package com.fastbee.pay.core.service.demo;

import com.fastbee.common.core.domain.PageParam;
import com.fastbee.common.core.domain.PageResult;
import com.fastbee.pay.core.controller.admin.demo.vo.OrderInfoCreateReqVO;
import com.fastbee.pay.core.domain.dataobject.demo.OrderInfo;

import javax.validation.Valid;

/**
 * 示例订单 Service 接口
 *
 * @author fastbee
 */
public interface OrderInfoService {

    /**
     * 创建示例订单
     *
     * @param userId      用户编号
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOrder(Long userId, @Valid OrderInfoCreateReqVO createReqVO);

    /**
     * 获得示例订单
     *
     * @param id 编号
     * @return 示例订单
     */
    OrderInfo getOrder(Long id);

    /**
     * 获得示例订单分页
     *
     * @param pageReqVO 分页查询
     * @return 示例订单分页
     */
    PageResult<OrderInfo> getOrderPage(PageParam pageReqVO);

    /**
     * 更新示例订单为已支付
     *
     * @param id 编号
     * @param payOrderId 支付订单号
     */
    void updateOrderPaid(Long id, Long payOrderId);

    /**
     * 发起示例订单的退款
     *
     * @param id 编号
     * @param userIp 用户编号
     */
    void refundOrder(Long id, String userIp);

    /**
     * 更新示例订单为已退款
     *
     * @param id 编号
     * @param payRefundId 退款订单号
     */
    void updateOrderRefunded(Long id, Long payRefundId);

}
