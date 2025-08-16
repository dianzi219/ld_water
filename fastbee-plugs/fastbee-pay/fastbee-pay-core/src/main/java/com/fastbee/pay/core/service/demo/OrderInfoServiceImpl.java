package com.fastbee.pay.core.service.demo;

import cn.hutool.core.lang.Assert;
import com.fastbee.common.core.domain.PageParam;
import com.fastbee.common.core.domain.PageResult;
import com.fastbee.framework.mybatis.LambdaQueryWrapperX;
import com.fastbee.pay.api.api.order.PayOrderApi;
import com.fastbee.pay.api.api.order.dto.PayOrderCreateReqDTO;
import com.fastbee.pay.api.api.order.dto.PayOrderRespDTO;
import com.fastbee.pay.api.api.refund.PayRefundApi;
import com.fastbee.pay.api.api.refund.dto.PayRefundCreateReqDTO;
import com.fastbee.pay.api.api.refund.dto.PayRefundRespDTO;
import com.fastbee.pay.api.enums.order.PayOrderStatusEnum;
import com.fastbee.pay.api.enums.refund.PayRefundStatusEnum;
import com.fastbee.pay.core.controller.admin.demo.vo.OrderInfoCreateReqVO;
import com.fastbee.pay.core.domain.dataobject.demo.OrderInfo;
import com.fastbee.pay.core.domain.mapper.demo.OrderInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static cn.hutool.core.util.ObjectUtil.notEqual;
import static com.fastbee.common.exception.ServiceExceptionUtil.exception;
import static com.fastbee.common.utils.ServletUtils.getClientIP;
import static com.fastbee.common.utils.date.LocalDateTimeUtils.addTime;
import static com.fastbee.common.utils.json.JsonUtils.toJsonString;
import static com.fastbee.pay.api.enums.ErrorCodeConstants.*;

/**
 * 示例订单 Service 实现类
 *
 * @author fastbee
 */
@Service
@Validated
@Slf4j
public class OrderInfoServiceImpl implements OrderInfoService {

    /**
     * 接入的实力应用编号
     * 从 [支付管理 -> 应用信息] 里添加
     */
    private static final Long PAY_APP_ID = 7L;

    /**
     * 商品信息 Map
     * key：商品编号
     * value：[商品名、商品价格]
     */
    private final Map<Long, Object[]> spuNames = new HashMap<>();

    @Resource
    private PayOrderApi payOrderApi;
    @Resource
    private PayRefundApi payRefundApi;

    @Resource
    private OrderInfoMapper OrderInfoMapper;

    public OrderInfoServiceImpl() {
        spuNames.put(1L, new Object[]{"华为手机", 1});
        spuNames.put(2L, new Object[]{"小米电视", 10});
        spuNames.put(3L, new Object[]{"苹果手表", 100});
        spuNames.put(4L, new Object[]{"华硕笔记本", 1000});
        spuNames.put(5L, new Object[]{"蔚来汽车", 200000});
    }

    @Override
    public Long createOrder(Long userId, OrderInfoCreateReqVO createReqVO) {
        // 1.1 获得商品
        Object[] spu = spuNames.get(createReqVO.getSpuId());
        Assert.notNull(spu, "商品({}) 不存在", createReqVO.getSpuId());
        String spuName = (String) spu[0];
        Integer price = (Integer) spu[1];
        // 1.2 插入 demo 订单
        OrderInfo demoOrder = new OrderInfo().setUserId(userId)
                .setSpuId(createReqVO.getSpuId()).setSpuName(spuName)
                .setPrice(price).setPayStatus(false).setRefundPrice(0);
        OrderInfoMapper.insert(demoOrder);

        // 2.1 创建支付单
        Long payOrderId = payOrderApi.createOrder(new PayOrderCreateReqDTO()
                .setAppId(PAY_APP_ID).setUserIp(getClientIP()) // 支付应用
                .setMerchantOrderId(demoOrder.getId().toString()) // 业务的订单编号
                .setSubject(spuName).setBody("").setPrice(price) // 价格信息
                .setExpireTime(addTime(Duration.ofHours(2L)))); // 支付的过期时间
        // 2.2 更新支付单到 demo 订单
        OrderInfoMapper.updateById(new OrderInfo().setId(demoOrder.getId())
                .setPayOrderId(payOrderId));
        // 返回
        return demoOrder.getId();
    }

    @Override
    public OrderInfo getOrder(Long id) {
        return OrderInfoMapper.selectById(id);
    }

    @Override
    public PageResult<OrderInfo> getOrderPage(PageParam pageReqVO) {
        return OrderInfoMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<OrderInfo>()
                .orderByDesc(OrderInfo::getId));
    }

    @Override
    public void updateOrderPaid(Long id, Long payOrderId) {
        // 校验并获得支付订单（可支付）
        PayOrderRespDTO payOrder = validateDemoOrderCanPaid(id, payOrderId);

        // 更新 OrderInfo 状态为已支付
        int updateCount = OrderInfoMapper.update(new OrderInfo().setPayStatus(true).setPayTime(LocalDateTime.now())
                .setPayChannelCode(payOrder.getChannelCode()), new LambdaQueryWrapperX<OrderInfo>()
                .eq(OrderInfo::getId, id).eq(OrderInfo::getPayStatus, false));
        if (updateCount == 0) {
            throw exception(DEMO_ORDER_UPDATE_PAID_STATUS_NOT_UNPAID);
        }
    }

    /**
     * 校验交易订单满足被支付的条件
     * 1. 交易订单未支付
     * 2. 支付单已支付
     *
     * @param id 交易订单编号
     * @param payOrderId 支付订单编号
     * @return 交易订单
     */
    private PayOrderRespDTO validateDemoOrderCanPaid(Long id, Long payOrderId) {
        // 1.1 校验订单是否存在
        OrderInfo order = OrderInfoMapper.selectById(id);
        if (order == null) {
            throw exception(DEMO_ORDER_NOT_FOUND);
        }
        // 1.2 校验订单未支付
        if (order.getPayStatus()) {
            log.error("[validateDemoOrderCanPaid][order({}) 不处于待支付状态，请进行处理！order 数据是：{}]",
                    id, toJsonString(order));
            throw exception(DEMO_ORDER_UPDATE_PAID_STATUS_NOT_UNPAID);
        }
        // 1.3 校验支付订单匹配
        if (notEqual(order.getPayOrderId(), payOrderId)) { // 支付单号
            log.error("[validateDemoOrderCanPaid][order({}) 支付单不匹配({})，请进行处理！order 数据是：{}]",
                    id, payOrderId, toJsonString(order));
            throw exception(DEMO_ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR);
        }

        // 2.1 校验支付单是否存在
        PayOrderRespDTO payOrder = payOrderApi.getOrder(payOrderId);
        if (payOrder == null) {
            log.error("[validateDemoOrderCanPaid][order({}) payOrder({}) 不存在，请进行处理！]", id, payOrderId);
            throw exception(ORDER_NOT_FOUND);
        }
        // 2.2 校验支付单已支付
        if (!PayOrderStatusEnum.isSuccess(payOrder.getStatus())) {
            log.error("[validateDemoOrderCanPaid][order({}) payOrder({}) 未支付，请进行处理！payOrder 数据是：{}]",
                    id, payOrderId, toJsonString(payOrder));
            throw exception(DEMO_ORDER_UPDATE_PAID_FAIL_PAY_ORDER_STATUS_NOT_SUCCESS);
        }
        // 2.3 校验支付金额一致
        if (notEqual(payOrder.getPrice(), order.getPrice())) {
            log.error("[validateDemoOrderCanPaid][order({}) payOrder({}) 支付金额不匹配，请进行处理！order 数据是：{}，payOrder 数据是：{}]",
                    id, payOrderId, toJsonString(order), toJsonString(payOrder));
            throw exception(DEMO_ORDER_UPDATE_PAID_FAIL_PAY_PRICE_NOT_MATCH);
        }
        // 2.4 校验支付订单匹配（二次）
        if (notEqual(payOrder.getMerchantOrderId(), id.toString())) {
            log.error("[validateDemoOrderCanPaid][order({}) 支付单不匹配({})，请进行处理！payOrder 数据是：{}]",
                    id, payOrderId, toJsonString(payOrder));
            throw exception(DEMO_ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR);
        }
        return payOrder;
    }

    @Override
    public void refundOrder(Long id, String userIp) {
        // 1. 校验订单是否可以退款
        OrderInfo order = validateDemoOrderCanRefund(id);

        // 2.1 生成退款单号
        // 一般来说，用户发起退款的时候，都会单独插入一个售后维权表，然后使用该表的 id 作为 refundId
        // 这里我们是个简单的 demo，所以没有售后维权表，直接使用订单 id + "-refund" 来演示
        String refundId = order.getId() + "-refund";
        // 2.2 创建退款单
        Long payRefundId = payRefundApi.createRefund(new PayRefundCreateReqDTO()
                .setAppId(PAY_APP_ID).setUserIp(getClientIP()) // 支付应用
                .setMerchantOrderId(String.valueOf(order.getId())) // 支付单号
                .setMerchantRefundId(refundId)
                .setReason("想退钱").setPrice(order.getPrice()));// 价格信息
        // 2.3 更新退款单到 demo 订单
        OrderInfoMapper.updateById(new OrderInfo().setId(id)
                .setPayRefundId(payRefundId).setRefundPrice(order.getPrice()));
    }

    private OrderInfo validateDemoOrderCanRefund(Long id) {
        // 校验订单是否存在
        OrderInfo order = OrderInfoMapper.selectById(id);
        if (order == null) {
            throw exception(DEMO_ORDER_NOT_FOUND);
        }
        // 校验订单是否支付
        if (!order.getPayStatus()) {
            throw exception(DEMO_ORDER_REFUND_FAIL_NOT_PAID);
        }
        // 校验订单是否已退款
        if (order.getPayRefundId() != null) {
            throw exception(DEMO_ORDER_REFUND_FAIL_REFUNDED);
        }
        return order;
    }

    @Override
    public void updateOrderRefunded(Long id, Long payRefundId) {
        // 1. 校验并获得退款订单（可退款）
        PayRefundRespDTO payRefund = validateDemoOrderCanRefunded(id, payRefundId);
        // 2.2 更新退款单到 demo 订单
        OrderInfoMapper.updateById(new OrderInfo().setId(id)
                .setRefundTime(payRefund.getSuccessTime()));
    }

    private PayRefundRespDTO validateDemoOrderCanRefunded(Long id, Long payRefundId) {
        // 1.1 校验示例订单
        OrderInfo order = OrderInfoMapper.selectById(id);
        if (order == null) {
            throw exception(DEMO_ORDER_NOT_FOUND);
        }
        // 1.2 校验退款订单匹配
        if (Objects.equals(order.getPayOrderId(), payRefundId)) {
            log.error("[validateDemoOrderCanRefunded][order({}) 退款单不匹配({})，请进行处理！order 数据是：{}]",
                    id, payRefundId, toJsonString(order));
            throw exception(DEMO_ORDER_REFUND_FAIL_REFUND_ORDER_ID_ERROR);
        }

        // 2.1 校验退款订单
        PayRefundRespDTO payRefund = payRefundApi.getRefund(payRefundId);
        if (payRefund == null) {
            throw exception(DEMO_ORDER_REFUND_FAIL_REFUND_NOT_FOUND);
        }
        // 2.2
        if (!PayRefundStatusEnum.isSuccess(payRefund.getStatus())) {
            throw exception(DEMO_ORDER_REFUND_FAIL_REFUND_NOT_SUCCESS);
        }
        // 2.3 校验退款金额一致
        if (notEqual(payRefund.getRefundPrice(), order.getPrice())) {
            log.error("[validateDemoOrderCanRefunded][order({}) payRefund({}) 退款金额不匹配，请进行处理！order 数据是：{}，payRefund 数据是：{}]",
                    id, payRefundId, toJsonString(order), toJsonString(payRefund));
            throw exception(DEMO_ORDER_REFUND_FAIL_REFUND_PRICE_NOT_MATCH);
        }
        // 2.4 校验退款订单匹配（二次）
        if (notEqual(payRefund.getMerchantOrderId(), id.toString())) {
            log.error("[validateDemoOrderCanRefunded][order({}) 退款单不匹配({})，请进行处理！payRefund 数据是：{}]",
                    id, payRefundId, toJsonString(payRefund));
            throw exception(DEMO_ORDER_REFUND_FAIL_REFUND_ORDER_ID_ERROR);
        }
        return payRefund;
    }

}
