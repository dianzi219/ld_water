package com.fastbee.pay.core.service.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fastbee.pay.core.domain.dataobject.order.PayOrderExtension;
import com.fastbee.pay.core.domain.mapper.order.PayOrderExtensionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;


/**
 * 支付订单 Service 实现类
 *
 * @author aquan
 */
@Service
@Validated
@Slf4j
public class PayOrderExtensionServiceImpl implements PayOrderExtensionService {

    @Resource
    private PayOrderExtensionMapper payOrderExtensionMapper;

    @Override
    public PayOrderExtension selectByNo(String no) {
        return payOrderExtensionMapper.selectOne(PayOrderExtension::getNo, no);
    }

    @Override
    public int updateByIdAndStatus(Long id, Integer status, PayOrderExtension update) {
        return payOrderExtensionMapper.update(update, new LambdaQueryWrapper<PayOrderExtension>()
                .eq(PayOrderExtension::getId, id).eq(PayOrderExtension::getStatus, status));
    }

    @Override
    public List<PayOrderExtension> selectListByOrderId(Long orderId) {
        return payOrderExtensionMapper.selectList(PayOrderExtension::getOrderId, orderId);
    }

    @Override
    public List<PayOrderExtension> selectListByStatusAndCreateTimeGe(Integer status, LocalDateTime minCreateTime) {
        return payOrderExtensionMapper.selectList(new LambdaQueryWrapper<PayOrderExtension>()
                .eq(PayOrderExtension::getStatus, status)
                .ge(PayOrderExtension::getCreateTime, minCreateTime));
    }

    @Override
    public PayOrderExtension selectById(Long id) {
        return payOrderExtensionMapper.selectById(id);
    }

    @Override
    public void insert(PayOrderExtension payOrderExtension) {
        payOrderExtensionMapper.insert(payOrderExtension);
    }
}
