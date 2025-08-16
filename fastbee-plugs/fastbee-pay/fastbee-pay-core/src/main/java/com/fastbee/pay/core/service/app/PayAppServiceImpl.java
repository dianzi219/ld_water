package com.fastbee.pay.core.service.app;

import com.fastbee.common.core.domain.PageResult;
import com.fastbee.common.enums.CommonStatusEnum;
import com.fastbee.framework.mybatis.LambdaQueryWrapperX;
import com.fastbee.pay.api.enums.ErrorCodeConstants;
import com.fastbee.pay.core.controller.admin.app.vo.PayAppCreateReqVO;
import com.fastbee.pay.core.controller.admin.app.vo.PayAppPageReqVO;
import com.fastbee.pay.core.controller.admin.app.vo.PayAppUpdateReqVO;
import com.fastbee.pay.core.convert.app.PayAppConvert;
import com.fastbee.pay.core.domain.dataobject.app.PayApp;
import com.fastbee.pay.core.domain.mapper.app.PayAppMapper;
import com.fastbee.pay.core.service.order.PayOrderService;
import com.fastbee.pay.core.service.refund.PayRefundService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.fastbee.common.exception.ServiceExceptionUtil.exception;
import static com.fastbee.pay.api.enums.ErrorCodeConstants.*;

/**
 * 支付应用 Service 实现类
 *
 * @author aquan
 */
@Service
@Validated
public class PayAppServiceImpl implements PayAppService {

    @Resource
    private PayAppMapper payAppMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖报错
    private PayOrderService payOrderService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖报错
    private PayRefundService payRefundService;

    @Override
    public Long createApp(PayAppCreateReqVO createReqVO) {
        // 插入
        PayApp app = PayAppConvert.INSTANCE.convert(createReqVO);
        payAppMapper.insert(app);
        // 返回
        return app.getId();
    }

    @Override
    public void updateApp(PayAppUpdateReqVO updateReqVO) {
        // 校验存在
        validateAppExists(updateReqVO.getId());
        // 更新
        PayApp updateObj = PayAppConvert.INSTANCE.convert(updateReqVO);
        payAppMapper.updateById(updateObj);
    }

    @Override
    public void updateAppStatus(Long id, Integer status) {
        // 校验商户存在
        validateAppExists(id);
        // 更新状态
        payAppMapper.updateById(new PayApp().setId(id).setStatus(status));
    }

    @Override
    public void deleteApp(Long id) {
        // 校验存在
        validateAppExists(id);
        // 校验关联数据是否存在
        if (payOrderService.getOrderCountByAppId(id) > 0) {
            throw exception(APP_EXIST_ORDER_CANT_DELETE);
        }
        if (payRefundService.getRefundCountByAppId(id) > 0) {
            throw exception(APP_EXIST_REFUND_CANT_DELETE);
        }

        // 删除
        payAppMapper.deleteById(id);
    }

    private void validateAppExists(Long id) {
        if (payAppMapper.selectById(id) == null) {
            throw exception(APP_NOT_FOUND);
        }
    }

    @Override
    public PayApp getApp(Long id) {
        return payAppMapper.selectById(id);
    }

    @Override
    public List<PayApp> getAppList(Collection<Long> ids) {
        return payAppMapper.selectBatchIds(ids);
    }

    @Override
    public List<PayApp> getAppList() {
         return payAppMapper.selectList();
    }

    @Override
    public PageResult<PayApp> getAppPage(PayAppPageReqVO pageReqVO) {
        return payAppMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<PayApp>()
                .likeIfPresent(PayApp::getName, pageReqVO.getName())
                .eqIfPresent(PayApp::getStatus, pageReqVO.getStatus())
                .betweenIfPresent(PayApp::getCreateTime, pageReqVO.getCreateTime())
                .orderByDesc(PayApp::getId));
    }

    @Override
    public PayApp validPayApp(Long id) {
        PayApp app = payAppMapper.selectById(id);
        // 校验是否存在
        if (app == null) {
            throw exception(ErrorCodeConstants.APP_NOT_FOUND);
        }
        // 校验是否禁用
        if (CommonStatusEnum.DISABLE.getStatus().equals(app.getStatus())) {
            throw exception(ErrorCodeConstants.APP_IS_DISABLE);
        }
        return app;
    }

}
