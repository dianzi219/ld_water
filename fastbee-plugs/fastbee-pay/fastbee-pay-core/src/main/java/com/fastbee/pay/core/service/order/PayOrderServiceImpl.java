package com.fastbee.pay.core.service.order;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fastbee.common.core.domain.PageResult;
import com.fastbee.common.utils.date.LocalDateTimeUtils;
import com.fastbee.framework.mybatis.LambdaQueryWrapperX;
import com.fastbee.pay.api.api.order.dto.PayOrderCreateReqDTO;
import com.fastbee.pay.api.enums.notify.PayNotifyTypeEnum;
import com.fastbee.pay.api.enums.order.PayOrderStatusEnum;
import com.fastbee.pay.core.controller.admin.order.vo.PayOrderExportReqVO;
import com.fastbee.pay.core.controller.admin.order.vo.PayOrderPageReqVO;
import com.fastbee.pay.core.controller.admin.order.vo.PayOrderSubmitReqVO;
import com.fastbee.pay.core.controller.admin.order.vo.PayOrderSubmitRespVO;
import com.fastbee.pay.core.convert.order.PayOrderConvert;
import com.fastbee.pay.core.domain.dataobject.app.PayApp;
import com.fastbee.pay.core.domain.dataobject.channel.PayChannel;
import com.fastbee.pay.core.domain.dataobject.order.PayOrder;
import com.fastbee.pay.core.domain.dataobject.order.PayOrderExtension;
import com.fastbee.pay.core.domain.mapper.order.PayOrderMapper;
import com.fastbee.pay.core.domain.redis.no.PayNoRedisDAO;
import com.fastbee.pay.core.framework.pay.config.PayProperties;
import com.fastbee.pay.core.service.app.PayAppService;
import com.fastbee.pay.core.service.channel.PayChannelService;
import com.fastbee.pay.core.service.notify.PayNotifyService;
import com.fastbee.pay.core.util.MoneyUtils;
import com.fastbee.pay.framework.client.PayClient;
import com.fastbee.pay.framework.client.PayClientFactory;
import com.fastbee.pay.framework.client.dto.order.PayOrderRespDTO;
import com.fastbee.pay.framework.client.dto.order.PayOrderUnifiedReqDTO;
import com.fastbee.pay.framework.enums.order.PayOrderStatusRespEnum;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.fastbee.common.exception.ServiceExceptionUtil.exception;
import static com.fastbee.common.utils.json.JsonUtils.toJsonString;
import static com.fastbee.pay.api.enums.ErrorCodeConstants.CHANNEL_NOT_FOUND;
import static com.fastbee.pay.api.enums.ErrorCodeConstants.ORDER_EXTENSION_IS_PAID;
import static com.fastbee.pay.api.enums.ErrorCodeConstants.ORDER_EXTENSION_NOT_FOUND;
import static com.fastbee.pay.api.enums.ErrorCodeConstants.ORDER_EXTENSION_STATUS_IS_NOT_WAITING;
import static com.fastbee.pay.api.enums.ErrorCodeConstants.ORDER_IS_EXPIRED;
import static com.fastbee.pay.api.enums.ErrorCodeConstants.ORDER_NOT_FOUND;
import static com.fastbee.pay.api.enums.ErrorCodeConstants.ORDER_REFUND_FAIL_STATUS_ERROR;
import static com.fastbee.pay.api.enums.ErrorCodeConstants.ORDER_STATUS_IS_NOT_WAITING;
import static com.fastbee.pay.api.enums.ErrorCodeConstants.ORDER_STATUS_IS_SUCCESS;
import static com.fastbee.pay.api.enums.ErrorCodeConstants.ORDER_SUBMIT_CHANNEL_ERROR;
import static com.fastbee.pay.api.enums.ErrorCodeConstants.REFUND_PRICE_EXCEED;


/**
 * 支付订单 Service 实现类
 *
 * @author aquan
 */
@Service
@Validated
@Slf4j
public class PayOrderServiceImpl implements PayOrderService {

    @Resource
    private PayProperties payProperties;

    @Resource
    private PayClientFactory payClientFactory;

    @Resource
    private PayOrderMapper payOrderMapper;
    @Resource
    private PayOrderExtensionService payOrderExtensionService;
    @Resource
    private PayNoRedisDAO noRedisDAO;

    @Resource
    private PayAppService appService;
    @Resource
    private PayChannelService channelService;
    @Resource
    private PayNotifyService notifyService;

    @Override
    public PayOrder getOrder(Long id) {
        return payOrderMapper.selectById(id);
    }

    @Override
    public PayOrder getOrder(Long appId, String merchantOrderId) {
        return this.selectByAppIdAndMerchantOrderId(appId, merchantOrderId);
    }

    @Override
    public Long getOrderCountByAppId(Long appId) {
        return payOrderMapper.selectCount(PayOrder::getAppId, appId);
    }

    @Override
    public PageResult<PayOrder> getOrderPage(PayOrderPageReqVO pageReqVO) {
        return payOrderMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<PayOrder>()
                .eqIfPresent(PayOrder::getAppId, pageReqVO.getAppId())
                .eqIfPresent(PayOrder::getChannelCode, pageReqVO.getChannelCode())
                .likeIfPresent(PayOrder::getMerchantOrderId, pageReqVO.getMerchantOrderId())
                .likeIfPresent(PayOrder::getChannelOrderNo, pageReqVO.getChannelOrderNo())
                .likeIfPresent(PayOrder::getNo, pageReqVO.getNo())
                .eqIfPresent(PayOrder::getStatus, pageReqVO.getStatus())
                .betweenIfPresent(PayOrder::getCreateTime, pageReqVO.getCreateTime())
                .orderByDesc(PayOrder::getId));
    }

    @Override
    public List<PayOrder> getOrderList(PayOrderExportReqVO exportReqVO) {
        return payOrderMapper.selectList(new LambdaQueryWrapperX<PayOrder>()
                .eqIfPresent(PayOrder::getAppId, exportReqVO.getAppId())
                .eqIfPresent(PayOrder::getChannelCode, exportReqVO.getChannelCode())
                .likeIfPresent(PayOrder::getMerchantOrderId, exportReqVO.getMerchantOrderId())
                .likeIfPresent(PayOrder::getChannelOrderNo, exportReqVO.getChannelOrderNo())
                .likeIfPresent(PayOrder::getNo, exportReqVO.getNo())
                .eqIfPresent(PayOrder::getStatus, exportReqVO.getStatus())
                .betweenIfPresent(PayOrder::getCreateTime, exportReqVO.getCreateTime())
                .orderByDesc(PayOrder::getId));
    }

    @Override
    public Long createOrder(PayOrderCreateReqDTO reqDTO) {
        // 校验 App
        PayApp app = appService.validPayApp(reqDTO.getAppId());

        // 查询对应的支付交易单是否已经存在。如果是，则直接返回
        PayOrder order = this.selectByAppIdAndMerchantOrderId(
                reqDTO.getAppId(), reqDTO.getMerchantOrderId());
        if (order != null) {
            log.warn("[createOrder][appId({}) merchantOrderId({}) 已经存在对应的支付单({})]", order.getAppId(),
                    order.getMerchantOrderId(), toJsonString(order)); // 理论来说，不会出现这个情况
            return order.getId();
        }

        // 创建支付交易单
        order = PayOrderConvert.INSTANCE.convert(reqDTO).setAppId(app.getId())
                // 商户相关字段
                .setNotifyUrl(app.getOrderNotifyUrl())
                // 订单相关字段
                .setStatus(PayOrderStatusEnum.WAITING.getStatus())
                // 退款相关字段
                .setRefundPrice(0);
        payOrderMapper.insert(order);
        return order.getId();
    }

    @Override // 注意，这里不能添加事务注解，避免调用支付渠道失败时，将 PayOrderExtension 回滚了
    public PayOrderSubmitRespVO submitOrder(PayOrderSubmitReqVO reqVO, String userIp) {
        // 1.1 获得 PayOrder ，并校验其是否存在
        PayOrder order = validateOrderCanSubmit(reqVO.getId());
        // 1.32 校验支付渠道是否有效
        PayChannel channel = validateChannelCanSubmit(order.getAppId(), reqVO.getChannelCode());
        PayClient client = payClientFactory.getPayClient(channel.getId());

        // 2. 插入 PayOrderExtension
        String no = noRedisDAO.generate(payProperties.getOrderNoPrefix());
        PayOrderExtension orderExtension = PayOrderConvert.INSTANCE.convert(reqVO, userIp)
                .setOrderId(order.getId()).setNo(no)
                .setChannelId(channel.getId()).setChannelCode(channel.getCode())
                .setStatus(PayOrderStatusEnum.WAITING.getStatus());
        payOrderExtensionService.insert(orderExtension);

        // 3. 调用三方接口
        PayOrderUnifiedReqDTO unifiedOrderReqDTO = PayOrderConvert.INSTANCE.convert2(reqVO, userIp)
                // 商户相关的字段
                .setOutTradeNo(orderExtension.getNo()) // 注意，此处使用的是 PayOrderExtension.no 属性！
                .setSubject(order.getSubject()).setBody(order.getBody())
                .setNotifyUrl(genChannelOrderNotifyUrl(channel))
                .setReturnUrl(reqVO.getReturnUrl())
                // 订单相关字段
                .setPrice(order.getPrice()).setExpireTime(order.getExpireTime());
        PayOrderRespDTO unifiedOrderResp = client.unifiedOrder(unifiedOrderReqDTO);

        // 4. 如果调用直接支付成功，则直接更新支付单状态为成功。例如说：付款码支付，免密支付时，就直接验证支付成功
        if (unifiedOrderResp != null) {
            getSelf().notifyOrder(channel, unifiedOrderResp);
            // 如有渠道错误码，则抛出业务异常，提示用户
            if (StrUtil.isNotEmpty(unifiedOrderResp.getChannelErrorCode())) {
                throw exception(ORDER_SUBMIT_CHANNEL_ERROR, unifiedOrderResp.getChannelErrorCode(),
                        unifiedOrderResp.getChannelErrorMsg());
            }
            // 此处需要读取最新的状态
            order = payOrderMapper.selectById(order.getId());
        }
        return PayOrderConvert.INSTANCE.convert(order, unifiedOrderResp);
    }

    private PayOrder validateOrderCanSubmit(Long id) {
        PayOrder order = payOrderMapper.selectById(id);
        if (order == null) { // 是否存在
            throw exception(ORDER_NOT_FOUND);
        }
        if (PayOrderStatusEnum.isSuccess(order.getStatus())) { // 校验状态，发现已支付
            throw exception(ORDER_STATUS_IS_SUCCESS);
        }
        if (!PayOrderStatusEnum.WAITING.getStatus().equals(order.getStatus())) { // 校验状态，必须是待支付
            throw exception(ORDER_STATUS_IS_NOT_WAITING);
        }
        if (LocalDateTimeUtils.beforeNow(order.getExpireTime())) { // 校验是否过期
            throw exception(ORDER_IS_EXPIRED);
        }

        // 【重要】校验是否支付拓展单已支付，只是没有回调、或者数据不正常
        validateOrderActuallyPaid(id);
        return order;
    }

    /**
     * 校验支付订单实际已支付
     *
     * @param id 支付编号
     */
    @VisibleForTesting
    void validateOrderActuallyPaid(Long id) {
        List<PayOrderExtension> orderExtensions = payOrderExtensionService.selectListByOrderId(id);
        orderExtensions.forEach(orderExtension -> {
            // 情况一：校验数据库中的 orderExtension 是不是已支付
            if (PayOrderStatusEnum.isSuccess(orderExtension.getStatus())) {
                log.warn("[validateOrderCanSubmit][order({}) 的 extension({}) 已支付，可能是数据不一致]",
                        id, orderExtension.getId());
                throw exception(ORDER_EXTENSION_IS_PAID);
            }
            // 情况二：调用三方接口，查询支付单状态，是不是已支付
            PayClient payClient = payClientFactory.getPayClient(orderExtension.getChannelId());
            if (payClient == null) {
                log.error("[validateOrderCanSubmit][渠道编号({}) 找不到对应的支付客户端]", orderExtension.getChannelId());
                return;
            }
            PayOrderRespDTO respDTO = payClient.getOrder(orderExtension.getNo());
            if (respDTO != null && PayOrderStatusRespEnum.isSuccess(respDTO.getStatus())) {
                log.warn("[validateOrderCanSubmit][order({}) 的 PayOrderRespDTO({}) 已支付，可能是回调延迟]",
                        id, toJsonString(respDTO));
                throw exception(ORDER_EXTENSION_IS_PAID);
            }
        });
    }

    private PayChannel validateChannelCanSubmit(Long appId, String channelCode) {
        // 校验 App
        appService.validPayApp(appId);
        // 校验支付渠道是否有效
        PayChannel channel = channelService.validPayChannel(appId, channelCode);
        PayClient client = payClientFactory.getPayClient(channel.getId());
        if (client == null) {
            log.error("[validatePayChannelCanSubmit][渠道编号({}) 找不到对应的支付客户端]", channel.getId());
            throw exception(CHANNEL_NOT_FOUND);
        }
        return channel;
    }

    /**
     * 根据支付渠道的编码，生成支付渠道的回调地址
     *
     * @param channel 支付渠道
     * @return 支付渠道的回调地址  配置地址 + "/" + channel id
     */
    private String genChannelOrderNotifyUrl(PayChannel channel) {
        return payProperties.getOrderNotifyUrl() + "/" + channel.getId();
    }

    @Override
    public void notifyOrder(Long channelId, PayOrderRespDTO notify) {
        // 校验支付渠道是否有效
        PayChannel channel = channelService.validPayChannel(channelId);
        // 更新支付订单为已支付
//        TenantUtils.execute(channel.getTenantId(), () -> getSelf().notifyOrder(channel, notify));
    }

    /**
     * 通知并更新订单的支付结果
     *
     * @param channel 支付渠道
     * @param notify 通知
     */
    @Transactional(rollbackFor = Exception.class) // 注意，如果是方法内调用该方法，需要通过 getSelf().notifyPayOrder(channel, notify) 调用，否则事务不生效
    public void notifyOrder(PayChannel channel, PayOrderRespDTO notify) {
        // 情况一：支付成功的回调
        if (PayOrderStatusRespEnum.isSuccess(notify.getStatus())) {
            notifyOrderSuccess(channel, notify);
            return;
        }
        // 情况二：支付失败的回调
        if (PayOrderStatusRespEnum.isClosed(notify.getStatus())) {
            notifyOrderClosed(channel, notify);
        }
        // 情况三：WAITING：无需处理
        // 情况四：REFUND：通过退款回调处理
    }

    private void notifyOrderSuccess(PayChannel channel, PayOrderRespDTO notify) {
        // 1. 更新 PayOrderExtension 支付成功
        PayOrderExtension orderExtension = updateOrderSuccess(notify);
        // 2. 更新 PayOrder 支付成功
        Boolean paid = updateOrderSuccess(channel, orderExtension, notify);
        if (paid) { // 如果之前已经成功回调，则直接返回，不用重复记录支付通知记录；例如说：支付平台重复回调
            return;
        }

        // 3. 插入支付通知记录
        notifyService.createPayNotifyTask(PayNotifyTypeEnum.ORDER.getType(),
                orderExtension.getOrderId());
    }

    /**
     * 更新 PayOrderExtension 支付成功
     *
     * @param notify 通知
     * @return PayOrderExtension 对象
     */
    private PayOrderExtension updateOrderSuccess(PayOrderRespDTO notify) {
        // 1. 查询 PayOrderExtension
        PayOrderExtension orderExtension = payOrderExtensionService.selectByNo(notify.getOutTradeNo());
        if (orderExtension == null) {
            throw exception(ORDER_EXTENSION_NOT_FOUND);
        }
        if (PayOrderStatusEnum.isSuccess(orderExtension.getStatus())) { // 如果已经是成功，直接返回，不用重复更新
            log.info("[updateOrderExtensionSuccess][orderExtension({}) 已经是已支付，无需更新]", orderExtension.getId());
            return orderExtension;
        }
        if (ObjectUtil.notEqual(orderExtension.getStatus(), PayOrderStatusEnum.WAITING.getStatus())) { // 校验状态，必须是待支付
            throw exception(ORDER_EXTENSION_STATUS_IS_NOT_WAITING);
        }

        // 2. 更新 PayOrderExtension
        int updateCounts = payOrderExtensionService.updateByIdAndStatus(orderExtension.getId(), orderExtension.getStatus(),
                PayOrderExtension.builder().status(PayOrderStatusEnum.SUCCESS.getStatus()).channelNotifyData(toJsonString(notify)).build());
        if (updateCounts == 0) { // 校验状态，必须是待支付
            throw exception(ORDER_EXTENSION_STATUS_IS_NOT_WAITING);
        }
        log.info("[updateOrderExtensionSuccess][orderExtension({}) 更新为已支付]", orderExtension.getId());
        return orderExtension;
    }

    /**
     * 更新 PayOrder 支付成功
     *
     * @param channel 支付渠道
     * @param orderExtension 支付拓展单
     * @param notify 通知回调
     * @return 是否之前已经成功回调
     */
    private Boolean updateOrderSuccess(PayChannel channel, PayOrderExtension orderExtension,
                                                         PayOrderRespDTO notify) {
        // 1. 判断 PayOrder 是否处于待支付
        PayOrder order = payOrderMapper.selectById(orderExtension.getOrderId());
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        if (PayOrderStatusEnum.isSuccess(order.getStatus()) // 如果已经是成功，直接返回，不用重复更新
                && Objects.equals(order.getExtensionId(), orderExtension.getId())) {
            log.info("[updateOrderExtensionSuccess][order({}) 已经是已支付，无需更新]", order.getId());
            return true;
        }
        if (!PayOrderStatusEnum.WAITING.getStatus().equals(order.getStatus())) { // 校验状态，必须是待支付
            throw exception(ORDER_STATUS_IS_NOT_WAITING);
        }

        // 2. 更新 PayOrder
        int updateCounts = this.updateByIdAndStatus(order.getId(), PayOrderStatusEnum.WAITING.getStatus(),
                PayOrder.builder().status(PayOrderStatusEnum.SUCCESS.getStatus())
                        .channelId(channel.getId()).channelCode(channel.getCode())
                        .successTime(notify.getSuccessTime()).extensionId(orderExtension.getId()).no(orderExtension.getNo())
                        .channelOrderNo(notify.getChannelOrderNo()).channelUserId(notify.getChannelUserId())
                        .channelFeeRate(channel.getFeeRate()).channelFeePrice(MoneyUtils.calculateRatePrice(order.getPrice(), channel.getFeeRate()))
                        .build());
        if (updateCounts == 0) { // 校验状态，必须是待支付
            throw exception(ORDER_STATUS_IS_NOT_WAITING);
        }
        log.info("[updateOrderExtensionSuccess][order({}) 更新为已支付]", order.getId());
        return false;
    }

    private void notifyOrderClosed(PayChannel channel, PayOrderRespDTO notify) {
        updateOrderExtensionClosed(channel, notify);
    }

    private void updateOrderExtensionClosed(PayChannel channel, PayOrderRespDTO notify) {
        // 1. 查询 PayOrderExtension
        PayOrderExtension orderExtension = payOrderExtensionService.selectByNo(notify.getOutTradeNo());
        if (orderExtension == null) {
            throw exception(ORDER_EXTENSION_NOT_FOUND);
        }
        if (PayOrderStatusEnum.isClosed(orderExtension.getStatus())) { // 如果已经是关闭，直接返回，不用重复更新
            log.info("[updateOrderExtensionClosed][orderExtension({}) 已经是支付关闭，无需更新]", orderExtension.getId());
            return;
        }
        // 一般出现先是支付成功，然后支付关闭，都是全部退款导致关闭的场景。这个情况，我们不更新支付拓展单，只通过退款流程，更新支付单
        if (PayOrderStatusEnum.isSuccess(orderExtension.getStatus())) {
            log.info("[updateOrderExtensionClosed][orderExtension({}) 是已支付，无需更新为支付关闭]", orderExtension.getId());
            return;
        }
        if (ObjectUtil.notEqual(orderExtension.getStatus(), PayOrderStatusEnum.WAITING.getStatus())) { // 校验状态，必须是待支付
            throw exception(ORDER_EXTENSION_STATUS_IS_NOT_WAITING);
        }

        // 2. 更新 PayOrderExtension
        int updateCounts = payOrderExtensionService.updateByIdAndStatus(orderExtension.getId(), orderExtension.getStatus(),
                PayOrderExtension.builder().status(PayOrderStatusEnum.CLOSED.getStatus()).channelNotifyData(toJsonString(notify))
                        .channelErrorCode(notify.getChannelErrorCode()).channelErrorMsg(notify.getChannelErrorMsg()).build());
        if (updateCounts == 0) { // 校验状态，必须是待支付
            throw exception(ORDER_EXTENSION_STATUS_IS_NOT_WAITING);
        }
        log.info("[updateOrderExtensionClosed][orderExtension({}) 更新为支付关闭]", orderExtension.getId());
    }

    @Override
    public void updateOrderRefundPrice(Long id, Integer incrRefundPrice) {
        PayOrder order = payOrderMapper.selectById(id);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        if (!PayOrderStatusEnum.isSuccessOrRefund(order.getStatus())) {
            throw exception(ORDER_REFUND_FAIL_STATUS_ERROR);
        }
        if (order.getRefundPrice() + incrRefundPrice > order.getPrice()) {
            throw exception(REFUND_PRICE_EXCEED);
        }

        // 更新订单
        PayOrder updateObj = new PayOrder()
                .setRefundPrice(order.getRefundPrice() + incrRefundPrice)
                .setStatus(PayOrderStatusEnum.REFUND.getStatus());
        int updateCount = this.updateByIdAndStatus(id, order.getStatus(), updateObj);
        if (updateCount == 0) {
            throw exception(ORDER_REFUND_FAIL_STATUS_ERROR);
        }
    }

    @Override
    public PayOrderExtension getOrderExtension(Long id) {
        return payOrderExtensionService.selectById(id);
    }

    @Override
    public int syncOrder(LocalDateTime minCreateTime) {
        // 1. 查询指定创建时间内的待支付订单
        List<PayOrderExtension> orderExtensions = payOrderExtensionService.selectListByStatusAndCreateTimeGe(
                PayOrderStatusEnum.WAITING.getStatus(), minCreateTime);
        if (CollUtil.isEmpty(orderExtensions)) {
            return 0;
        }
        // 2. 遍历执行
        int count = 0;
        for (PayOrderExtension orderExtension : orderExtensions) {
            count += syncOrder(orderExtension) ? 1 : 0;
        }
        return count;
    }

    /**
     * 同步单个支付拓展单
     *
     * @param orderExtension 支付拓展单
     * @return 是否已支付
     */
    private boolean syncOrder(PayOrderExtension orderExtension) {
        try {
            // 1.1 查询支付订单信息
            PayClient payClient = payClientFactory.getPayClient(orderExtension.getChannelId());
            if (payClient == null) {
                log.error("[syncOrder][渠道编号({}) 找不到对应的支付客户端]", orderExtension.getChannelId());
                return false;
            }
            PayOrderRespDTO respDTO = payClient.getOrder(orderExtension.getNo());
            // 1.2 回调支付结果
            notifyOrder(orderExtension.getChannelId(), respDTO);

            // 2. 如果是已支付，则返回 true
            return PayOrderStatusRespEnum.isSuccess(respDTO.getStatus());
        } catch (Throwable e) {
            log.error("[syncOrder][orderExtension({}) 同步支付状态异常]", orderExtension.getId(), e);
            return false;
        }
    }

    @Override
    public int expireOrder() {
        // 1. 查询过期的待支付订单
        List<PayOrder> orders = payOrderMapper.selectList(new LambdaQueryWrapper<PayOrder>()
                .eq(PayOrder::getStatus, PayOrderStatusEnum.WAITING.getStatus())
                .lt(PayOrder::getExpireTime, LocalDateTime.now()));
        if (CollUtil.isEmpty(orders)) {
            return 0;
        }

        // 2. 遍历执行
        int count = 0;
        for (PayOrder order : orders) {
            count += expireOrder(order) ? 1 : 0;
        }
        return count;
    }

    @Override
    public PayOrder selectByAppIdAndMerchantOrderId(Long appId, String merchantOrderId) {
        return payOrderMapper.selectOne(PayOrder::getAppId, appId,
                PayOrder::getMerchantOrderId, merchantOrderId);
    }

    @Override
    public int updateByIdAndStatus(Long id, Integer status, PayOrder update) {
        return payOrderMapper.update(update, new LambdaQueryWrapper<PayOrder>()
                .eq(PayOrder::getId, id).eq(PayOrder::getStatus, status));
    }

    /**
     * 同步单个支付单
     *
     * @param order 支付单
     * @return 是否已过期
     */
    private boolean expireOrder(PayOrder order) {
        try {
            // 1. 需要先处理关联的支付拓展单，避免错误的过期已支付 or 已退款的订单
            List<PayOrderExtension> orderExtensions = payOrderExtensionService.selectListByOrderId(order.getId());
            for (PayOrderExtension orderExtension : orderExtensions) {
                if (PayOrderStatusEnum.isClosed(orderExtension.getStatus())) {
                    continue;
                }
                // 情况一：校验数据库中的 orderExtension 是不是已支付
                if (PayOrderStatusEnum.isSuccess(orderExtension.getStatus())) {
                    log.error("[expireOrder][order({}) 的 extension({}) 已支付，可能是数据不一致]",
                            order.getId(), orderExtension.getId());
                    return false;
                }
                // 情况二：调用三方接口，查询支付单状态，是不是已支付/已退款
                PayClient payClient = payClientFactory.getPayClient(orderExtension.getChannelId());
                if (payClient == null) {
                    log.error("[expireOrder][渠道编号({}) 找不到对应的支付客户端]", orderExtension.getChannelId());
                    return false;
                }
                PayOrderRespDTO respDTO = payClient.getOrder(orderExtension.getNo());
                if (PayOrderStatusRespEnum.isRefund(respDTO.getStatus())) {
                    // 补充说明：按道理，应该是 WAITING => SUCCESS => REFUND 状态，如果直接 WAITING => REFUND 状态，说明中间丢了过程
                    // 此时，需要人工介入，手工补齐数据，保持 WAITING => SUCCESS => REFUND 的过程
                    log.error("[expireOrder][extension({}) 的 PayOrderRespDTO({}) 已退款，可能是回调延迟]",
                            orderExtension.getId(), toJsonString(respDTO));
                    return false;
                }
                if (PayOrderStatusRespEnum.isSuccess(respDTO.getStatus())) {
                    notifyOrder(orderExtension.getChannelId(), respDTO);
                    return false;
                }
                // 兜底逻辑：将支付拓展单更新为已关闭
                PayOrderExtension updateObj = new PayOrderExtension().setStatus(PayOrderStatusEnum.CLOSED.getStatus())
                        .setChannelNotifyData(toJsonString(respDTO));
                if (payOrderExtensionService.updateByIdAndStatus(orderExtension.getId(), PayOrderStatusEnum.WAITING.getStatus(),
                        updateObj) == 0) {
                    log.error("[expireOrder][extension({}) 更新为支付关闭失败]", orderExtension.getId());
                    return false;
                }
                log.info("[expireOrder][extension({}) 更新为支付关闭成功]", orderExtension.getId());
            }

            // 2. 都没有上述情况，可以安心更新为已关闭
            PayOrder updateObj = new PayOrder().setStatus(PayOrderStatusEnum.CLOSED.getStatus());
            if (this.updateByIdAndStatus(order.getId(), order.getStatus(), updateObj) == 0) {
                log.error("[expireOrder][order({}) 更新为支付关闭失败]", order.getId());
                return false;
            }
            log.info("[expireOrder][order({}) 更新为支付关闭失败]", order.getId());
            return true;
        } catch (Throwable e) {
            log.error("[expireOrder][order({}) 过期订单异常]", order.getId(), e);
            return false;
        }
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private PayOrderServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
