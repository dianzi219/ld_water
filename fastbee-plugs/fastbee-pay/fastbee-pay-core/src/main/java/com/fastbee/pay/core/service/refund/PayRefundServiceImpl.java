package com.fastbee.pay.core.service.refund;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fastbee.common.core.domain.PageResult;
import com.fastbee.framework.mybatis.LambdaQueryWrapperX;
import com.fastbee.pay.api.api.refund.dto.PayRefundCreateReqDTO;
import com.fastbee.pay.api.enums.notify.PayNotifyTypeEnum;
import com.fastbee.pay.api.enums.order.PayOrderStatusEnum;
import com.fastbee.pay.api.enums.refund.PayRefundStatusEnum;
import com.fastbee.pay.core.controller.admin.refund.vo.PayRefundExportReqVO;
import com.fastbee.pay.core.controller.admin.refund.vo.PayRefundPageReqVO;
import com.fastbee.pay.core.convert.refund.PayRefundConvert;
import com.fastbee.pay.core.domain.dataobject.app.PayApp;
import com.fastbee.pay.core.domain.dataobject.channel.PayChannel;
import com.fastbee.pay.core.domain.dataobject.order.PayOrder;
import com.fastbee.pay.core.domain.dataobject.refund.PayRefund;
import com.fastbee.pay.core.domain.mapper.refund.PayRefundMapper;
import com.fastbee.pay.core.domain.redis.no.PayNoRedisDAO;
import com.fastbee.pay.core.framework.pay.config.PayProperties;
import com.fastbee.pay.core.service.app.PayAppService;
import com.fastbee.pay.core.service.channel.PayChannelService;
import com.fastbee.pay.core.service.notify.PayNotifyService;
import com.fastbee.pay.core.service.order.PayOrderService;
import com.fastbee.pay.framework.client.PayClient;
import com.fastbee.pay.framework.client.PayClientFactory;
import com.fastbee.pay.framework.client.dto.refund.PayRefundRespDTO;
import com.fastbee.pay.framework.client.dto.refund.PayRefundUnifiedReqDTO;
import com.fastbee.pay.framework.enums.refund.PayRefundStatusRespEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static com.fastbee.common.exception.ServiceExceptionUtil.exception;
import static com.fastbee.common.utils.json.JsonUtils.toJsonString;
import static com.fastbee.pay.api.enums.ErrorCodeConstants.CHANNEL_NOT_FOUND;
import static com.fastbee.pay.api.enums.ErrorCodeConstants.ORDER_NOT_FOUND;
import static com.fastbee.pay.api.enums.ErrorCodeConstants.ORDER_REFUND_FAIL_STATUS_ERROR;
import static com.fastbee.pay.api.enums.ErrorCodeConstants.REFUND_EXISTS;
import static com.fastbee.pay.api.enums.ErrorCodeConstants.REFUND_HAS_REFUNDING;
import static com.fastbee.pay.api.enums.ErrorCodeConstants.REFUND_NOT_FOUND;
import static com.fastbee.pay.api.enums.ErrorCodeConstants.REFUND_PRICE_EXCEED;
import static com.fastbee.pay.api.enums.ErrorCodeConstants.REFUND_STATUS_IS_NOT_WAITING;


/**
 * 退款订单 Service 实现类
 *
 * @author jason
 */
@Service
@Slf4j
@Validated
public class PayRefundServiceImpl implements PayRefundService {

    @Resource
    private PayProperties payProperties;

    @Resource
    private PayClientFactory payClientFactory;

    @Resource
    private PayRefundMapper payRefundMapper;
    @Resource
    private PayNoRedisDAO payNoRedisDAO;

    @Resource
    private PayOrderService payOrderService;
    @Resource
    private PayAppService payAppService;
    @Resource
    private PayChannelService payChannelService;
    @Resource
    private PayNotifyService payNotifyService;

    @Override
    public PayRefund getRefund(Long id) {
        return payRefundMapper.selectById(id);
    }

    @Override
    public Long getRefundCountByAppId(Long appId) {
        return payRefundMapper.selectCount(PayRefund::getAppId, appId);
    }

    @Override
    public PageResult<PayRefund> getRefundPage(PayRefundPageReqVO pageReqVO) {
        return payRefundMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<PayRefund>()
                .eqIfPresent(PayRefund::getAppId, pageReqVO.getAppId())
                .eqIfPresent(PayRefund::getChannelCode, pageReqVO.getChannelCode())
                .likeIfPresent(PayRefund::getMerchantOrderId, pageReqVO.getMerchantOrderId())
                .likeIfPresent(PayRefund::getMerchantRefundId, pageReqVO.getMerchantRefundId())
                .likeIfPresent(PayRefund::getChannelOrderNo, pageReqVO.getChannelOrderNo())
                .likeIfPresent(PayRefund::getChannelRefundNo, pageReqVO.getChannelRefundNo())
                .eqIfPresent(PayRefund::getStatus, pageReqVO.getStatus())
                .betweenIfPresent(PayRefund::getCreateTime, pageReqVO.getCreateTime())
                .orderByDesc(PayRefund::getId));
    }

    @Override
    public List<PayRefund> getRefundList(PayRefundExportReqVO exportReqVO) {
        return payRefundMapper.selectList(new LambdaQueryWrapperX<PayRefund>()
                .eqIfPresent(PayRefund::getAppId, exportReqVO.getAppId())
                .eqIfPresent(PayRefund::getChannelCode, exportReqVO.getChannelCode())
                .likeIfPresent(PayRefund::getMerchantOrderId, exportReqVO.getMerchantOrderId())
                .likeIfPresent(PayRefund::getMerchantRefundId, exportReqVO.getMerchantRefundId())
                .likeIfPresent(PayRefund::getChannelOrderNo, exportReqVO.getChannelOrderNo())
                .likeIfPresent(PayRefund::getChannelRefundNo, exportReqVO.getChannelRefundNo())
                .eqIfPresent(PayRefund::getStatus, exportReqVO.getStatus())
                .betweenIfPresent(PayRefund::getCreateTime, exportReqVO.getCreateTime())
                .orderByDesc(PayRefund::getId));
    }

    @Override
    public Long createPayRefund(PayRefundCreateReqDTO reqDTO) {
        // 1.1 校验 App
        PayApp app = payAppService.validPayApp(reqDTO.getAppId());
        // 1.2 校验支付订单
        PayOrder order = validatePayOrderCanRefund(reqDTO);
        // 1.3 校验支付渠道是否有效
        PayChannel channel = payChannelService.validPayChannel(order.getChannelId());
        PayClient client = payClientFactory.getPayClient(channel.getId());
        if (client == null) {
            log.error("[refund][渠道编号({}) 找不到对应的支付客户端]", channel.getId());
            throw exception(CHANNEL_NOT_FOUND);
        }
        // 1.4 校验退款订单是否已经存在
        PayRefund refund = payRefundMapper.selectOne(new LambdaQueryWrapperX<PayRefund>()
                .eq(PayRefund::getAppId, app.getId())
                .eq(PayRefund::getMerchantRefundId, reqDTO.getMerchantRefundId()));
        if (refund != null) {
            throw exception(REFUND_EXISTS);
        }

        // 2.1 插入退款单
        String no = payNoRedisDAO.generate(payProperties.getRefundNoPrefix());
        refund = PayRefundConvert.INSTANCE.convert(reqDTO)
                .setNo(no).setOrderId(order.getId()).setOrderNo(order.getNo())
                .setChannelId(order.getChannelId()).setChannelCode(order.getChannelCode())
                // 商户相关的字段
                .setNotifyUrl(app.getRefundNotifyUrl())
                // 渠道相关字段
                .setChannelOrderNo(order.getChannelOrderNo())
                // 退款相关字段
                .setStatus(PayRefundStatusEnum.WAITING.getStatus())
                .setPayPrice(order.getPrice()).setRefundPrice(reqDTO.getPrice());
        payRefundMapper.insert(refund);
        try {
            // 2.2 向渠道发起退款申请
            PayRefundUnifiedReqDTO unifiedReqDTO = new PayRefundUnifiedReqDTO()
                    .setPayPrice(order.getPrice())
                    .setRefundPrice(reqDTO.getPrice())
                    .setOutTradeNo(order.getNo())
                    .setOutRefundNo(refund.getNo())
                    .setNotifyUrl(genChannelRefundNotifyUrl(channel))
                    .setReason(reqDTO.getReason());
            PayRefundRespDTO refundRespDTO = client.unifiedRefund(unifiedReqDTO);
            // 2.3 处理退款返回
            getSelf().notifyRefund(channel, refundRespDTO);
        } catch (Throwable e) {
            // 注意：这里仅打印异常，不进行抛出。
            // 原因是：虽然调用支付渠道进行退款发生异常（网络请求超时），实际退款成功。这个结果，后续通过退款回调、或者退款轮询补偿可以拿到。
            // 最终，在异常的情况下，支付中心会异步回调业务的退款回调接口，提供退款结果
            log.error("[createPayRefund][退款 id({}) requestDTO({}) 发生异常]",
                    refund.getId(), reqDTO, e);
        }

        // 返回退款编号
        return refund.getId();
    }

    /**
     * 校验支付订单是否可以退款
     *
     * @param reqDTO 退款申请信息
     * @return 支付订单
     */
    private PayOrder validatePayOrderCanRefund(PayRefundCreateReqDTO reqDTO) {
        PayOrder order = payOrderService.getOrder(reqDTO.getAppId(), reqDTO.getMerchantOrderId());
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        // 校验状态，必须是已支付、或者已退款
        if (!PayOrderStatusEnum.isSuccessOrRefund(order.getStatus())) {
            throw exception(ORDER_REFUND_FAIL_STATUS_ERROR);
        }

        // 校验金额，退款金额不能大于原定的金额
        if (reqDTO.getPrice() + order.getRefundPrice() > order.getPrice()){
            throw exception(REFUND_PRICE_EXCEED);
        }
        // 是否有退款中的订单
        if (this.selectCountByAppIdAndOrderId(reqDTO.getAppId(), order.getId(),
                PayRefundStatusEnum.WAITING.getStatus()) > 0) {
            throw exception(REFUND_HAS_REFUNDING);
        }
        return order;
    }

    /**
     * 根据支付渠道的编码，生成支付渠道的回调地址
     *
     * @param channel 支付渠道
     * @return 支付渠道的回调地址  配置地址 + "/" + channel id
     */
    private String genChannelRefundNotifyUrl(PayChannel channel) {
        return payProperties.getRefundNotifyUrl() + "/" + channel.getId();
    }

    @Override
    public void notifyRefund(Long channelId, PayRefundRespDTO notify) {
        // 校验支付渠道是否有效
        PayChannel channel = payChannelService.validPayChannel(channelId);
        // 更新退款订单
//        TenantUtils.execute(channel.getTenantId(), () -> getSelf().notifyRefund(channel, notify));
    }

    /**
     * 通知并更新订单的退款结果
     *
     * @param channel 支付渠道
     * @param notify 通知
     */
    @Transactional(rollbackFor = Exception.class)  // 注意，如果是方法内调用该方法，需要通过 getSelf().notifyRefund(channel, notify) 调用，否则事务不生效
    public void notifyRefund(PayChannel channel, PayRefundRespDTO notify) {
        // 情况一：退款成功
        if (PayRefundStatusRespEnum.isSuccess(notify.getStatus())) {
            notifyRefundSuccess(channel, notify);
            return;
        }
        // 情况二：退款失败
        if (PayRefundStatusRespEnum.isFailure(notify.getStatus())) {
            notifyRefundFailure(channel, notify);
        }
    }

    private void notifyRefundSuccess(PayChannel channel, PayRefundRespDTO notify) {
        // 1.1 查询 PayRefund
        PayRefund refund = this.selectByAppIdAndNo(
                channel.getAppId(), notify.getOutRefundNo());
        if (refund == null) {
            throw exception(REFUND_NOT_FOUND);
        }
        if (PayRefundStatusEnum.isSuccess(refund.getStatus())) { // 如果已经是成功，直接返回，不用重复更新
            log.info("[notifyRefundSuccess][退款订单({}) 已经是退款成功，无需更新]", refund.getId());
            return;
        }
        if (!PayRefundStatusEnum.WAITING.getStatus().equals(refund.getStatus())) {
            throw exception(REFUND_STATUS_IS_NOT_WAITING);
        }
        // 1.2 更新 PayRefund
        PayRefund updateRefundObj = new PayRefund()
                .setSuccessTime(notify.getSuccessTime())
                .setChannelRefundNo(notify.getChannelRefundNo())
                .setStatus(PayRefundStatusEnum.SUCCESS.getStatus())
                .setChannelNotifyData(toJsonString(notify));
        int updateCounts = this.updateByIdAndStatus(refund.getId(), refund.getStatus(), updateRefundObj);
        if (updateCounts == 0) { // 校验状态，必须是等待状态
            throw exception(REFUND_STATUS_IS_NOT_WAITING);
        }
        log.info("[notifyRefundSuccess][退款订单({}) 更新为退款成功]", refund.getId());

        // 2. 更新订单
        payOrderService.updateOrderRefundPrice(refund.getOrderId(), refund.getRefundPrice());

        // 3. 插入退款通知记录
        payNotifyService.createPayNotifyTask(PayNotifyTypeEnum.REFUND.getType(),
                refund.getId());
    }

    private void notifyRefundFailure(PayChannel channel, PayRefundRespDTO notify) {
        // 1.1 查询 PayRefund
        PayRefund refund = this.selectByAppIdAndNo(
                channel.getAppId(), notify.getOutRefundNo());
        if (refund == null) {
            throw exception(REFUND_NOT_FOUND);
        }
        if (PayRefundStatusEnum.isFailure(refund.getStatus())) { // 如果已经是成功，直接返回，不用重复更新
            log.info("[notifyRefundSuccess][退款订单({}) 已经是退款关闭，无需更新]", refund.getId());
            return;
        }
        if (!PayRefundStatusEnum.WAITING.getStatus().equals(refund.getStatus())) {
            throw exception(REFUND_STATUS_IS_NOT_WAITING);
        }
        // 1.2 更新 PayRefund
        PayRefund updateRefundObj = new PayRefund()
                .setChannelRefundNo(notify.getChannelRefundNo())
                .setStatus(PayRefundStatusEnum.FAILURE.getStatus())
                .setChannelNotifyData(toJsonString(notify))
                .setChannelErrorCode(notify.getChannelErrorCode()).setChannelErrorMsg(notify.getChannelErrorMsg());
        int updateCounts = this.updateByIdAndStatus(refund.getId(), refund.getStatus(), updateRefundObj);
        if (updateCounts == 0) { // 校验状态，必须是等待状态
            throw exception(REFUND_STATUS_IS_NOT_WAITING);
        }
        log.info("[notifyRefundFailure][退款订单({}) 更新为退款失败]", refund.getId());

        // 2. 插入退款通知记录
        payNotifyService.createPayNotifyTask(PayNotifyTypeEnum.REFUND.getType(),
                refund.getId());
    }

    @Override
    public int syncRefund() {
        // 1. 查询指定创建时间内的待退款订单
        List<PayRefund> refunds = payRefundMapper.selectList(PayRefund::getStatus, PayRefundStatusEnum.WAITING.getStatus());
        if (CollUtil.isEmpty(refunds)) {
            return 0;
        }
        // 2. 遍历执行
        int count = 0;
        for (PayRefund refund : refunds) {
            count += syncRefund(refund) ? 1 : 0;
        }
        return count;
    }

    @Override
    public Long selectCountByAppIdAndOrderId(Long appId, Long orderId, Integer status) {
        return payRefundMapper.selectCount(new LambdaQueryWrapperX<PayRefund>()
                .eq(PayRefund::getAppId, appId)
                .eq(PayRefund::getOrderId, orderId)
                .eq(PayRefund::getStatus, status));
    }

    @Override
    public PayRefund selectByAppIdAndNo(Long appId, String no) {
        return payRefundMapper.selectOne(new LambdaQueryWrapperX<PayRefund>()
                .eq(PayRefund::getAppId, appId)
                .eq(PayRefund::getNo, no));
    }

    @Override
    public int updateByIdAndStatus(Long id, Integer status, PayRefund update) {
        return payRefundMapper.update(update, new LambdaQueryWrapper<PayRefund>()
                .eq(PayRefund::getId, id).eq(PayRefund::getStatus, status));
    }

    /**
     * 同步单个退款订单
     *
     * @param refund 退款订单
     * @return 是否同步到
     */
    private boolean syncRefund(PayRefund refund) {
        try {
            // 1.1 查询退款订单信息
            PayClient payClient = payClientFactory.getPayClient(refund.getChannelId());
            if (payClient == null) {
                log.error("[syncRefund][渠道编号({}) 找不到对应的支付客户端]", refund.getChannelId());
                return false;
            }
            PayRefundRespDTO respDTO = payClient.getRefund(refund.getOrderNo(), refund.getNo());
            // 1.2 回调退款结果
            notifyRefund(refund.getChannelId(), respDTO);

            // 2. 如果同步到，则返回 true
            return PayRefundStatusEnum.isSuccess(respDTO.getStatus())
                    || PayRefundStatusEnum.isFailure(respDTO.getStatus());
        } catch (Throwable e) {
            log.error("[syncRefund][refund({}) 同步退款状态异常]", refund.getId(), e);
            return false;
        }
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private PayRefundServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
