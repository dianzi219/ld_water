package com.fastbee.pay.core.service.app;

import com.fastbee.common.core.domain.PageResult;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.collection.CollectionUtils;
import com.fastbee.pay.core.controller.admin.app.vo.PayAppCreateReqVO;
import com.fastbee.pay.core.controller.admin.app.vo.PayAppPageReqVO;
import com.fastbee.pay.core.controller.admin.app.vo.PayAppUpdateReqVO;
import com.fastbee.pay.core.domain.dataobject.app.PayApp;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 支付应用 Service 接口
 *
 * @author fastbee
 */
public interface PayAppService {

    /**
     * 创建支付应用
     *
     * @param createReqVO 创建
     * @return 编号
     */
    Long createApp(@Valid PayAppCreateReqVO createReqVO);

    /**
     * 更新支付应用
     *
     * @param updateReqVO 更新
     */
    void updateApp(@Valid PayAppUpdateReqVO updateReqVO);

    /**
     * 修改应用状态
     *
     * @param id     应用编号
     * @param status 状态
     */
    void updateAppStatus(Long id, Integer status);

    /**
     * 删除支付应用
     *
     * @param id 编号
     */
    void deleteApp(Long id);

    /**
     * 获得支付应用
     *
     * @param id 编号
     * @return 支付应用
     */
    PayApp getApp(Long id);

    /**
     * 获得支付应用列表
     *
     * @param ids 编号
     * @return 支付应用列表
     */
    List<PayApp> getAppList(Collection<Long> ids);

    /**
     * 获得支付应用列表
     *
     * @return 支付应用列表
     */
    List<PayApp> getAppList();

    /**
     * 获得支付应用分页
     *
     * @param pageReqVO 分页查询
     * @return 支付应用分页
     */
    PageResult<PayApp> getAppPage(PayAppPageReqVO pageReqVO);

    /**
     * 获得指定编号的商户 Map
     *
     * @param ids 应用编号集合
     * @return 商户 Map
     */
    default Map<Long, PayApp> getAppMap(Collection<Long> ids) {
        List<PayApp> list =  getAppList(ids);
        return CollectionUtils.convertMap(list, PayApp::getId);
    }

    /**
     * 支付应用的合法性
     *
     * 如果不合法，抛出 {@link ServiceException} 业务异常
     *
     * @param id 应用编号
     * @return 应用
     */
    PayApp validPayApp(Long id);

}
