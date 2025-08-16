package com.fastbee.pay.core.service.channel;

import com.fastbee.common.exception.ServiceException;
import com.fastbee.pay.core.controller.admin.channel.vo.PayChannelCreateReqVO;
import com.fastbee.pay.core.controller.admin.channel.vo.PayChannelUpdateReqVO;
import com.fastbee.pay.core.domain.dataobject.channel.PayChannel;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 支付渠道 Service 接口
 *
 * @author aquan
 */
public interface PayChannelService {

    /**
     * 创建支付渠道
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createChannel(@Valid PayChannelCreateReqVO createReqVO);

    /**
     * 更新支付渠道
     *
     * @param updateReqVO 更新信息
     */
    void updateChannel(@Valid PayChannelUpdateReqVO updateReqVO);

    /**
     * 删除支付渠道
     *
     * @param id 编号
     */
    void deleteChannel(Long id);

    /**
     * 获得支付渠道
     *
     * @param id 编号
     * @return 支付渠道
     */
    PayChannel getChannel(Long id);

    /**
     * 根据支付应用 ID 集合，获得支付渠道列表
     *
     * @param appIds 应用编号集合
     * @return 支付渠道列表
     */
    List<PayChannel> getChannelListByAppIds(Collection<Long> appIds);

    /**
     * 根据条件获取渠道
     *
     * @param appId      应用编号
     * @param code       渠道编码
     * @return 数量
     */
    PayChannel getChannelByAppIdAndCode(Long appId, String code);

    /**
     * 支付渠道的合法性
     *
     * 如果不合法，抛出 {@link ServiceException} 业务异常
     *
     * @param id 渠道编号
     * @return 渠道信息
     */
    PayChannel validPayChannel(Long id);

    /**
     * 支付渠道的合法性
     *
     * 如果不合法，抛出 {@link ServiceException} 业务异常
     *
     * @param appId 应用编号
     * @param code 支付渠道
     * @return 渠道信息
     */
    PayChannel validPayChannel(Long appId, String code);

    /**
     * 获得指定应用的开启的渠道列表
     *
     * @param appId 应用编号
     * @return 渠道列表
     */
    List<PayChannel> getEnableChannelList(Long appId);

    PayChannel selectByAppIdAndCode(Long appId, String code);

}
