package com.fastbee.pay.core.service.channel;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import com.fastbee.common.enums.CommonStatusEnum;
import com.fastbee.common.utils.json.JsonUtils;
import com.fastbee.framework.mybatis.LambdaQueryWrapperX;
import com.fastbee.pay.core.controller.admin.channel.vo.PayChannelCreateReqVO;
import com.fastbee.pay.core.controller.admin.channel.vo.PayChannelUpdateReqVO;
import com.fastbee.pay.core.convert.channel.PayChannelConvert;
import com.fastbee.pay.core.domain.dataobject.channel.PayChannel;
import com.fastbee.pay.core.domain.mapper.channel.PayChannelMapper;
import com.fastbee.pay.framework.client.PayClientConfig;
import com.fastbee.pay.framework.client.PayClientFactory;
import com.fastbee.pay.framework.enums.channel.PayChannelEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.Validator;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.fastbee.common.exception.ServiceExceptionUtil.exception;
import static com.fastbee.pay.api.enums.ErrorCodeConstants.CHANNEL_EXIST_SAME_CHANNEL_ERROR;
import static com.fastbee.pay.api.enums.ErrorCodeConstants.CHANNEL_IS_DISABLE;
import static com.fastbee.pay.api.enums.ErrorCodeConstants.CHANNEL_NOT_FOUND;


/**
 * 支付渠道 Service 实现类
 *
 * @author aquan
 */
@Service
@Slf4j
@Validated
public class PayChannelServiceImpl implements PayChannelService {

    @Getter // 为了方便测试，这里提供 getter 方法
    @Setter
    private volatile List<PayChannel> channelCache;

    @Resource
    private PayClientFactory payClientFactory;

    @Resource
    private PayChannelMapper payChannelMapper;

    @Resource
    private Validator validator;

    /**
     * 初始化 {@link #payClientFactory} 缓存
     */
    @PostConstruct
    public void initLocalCache() {
        // 注意：忽略自动多租户，因为要全局初始化缓存
//        TenantUtils.executeIgnore(() -> {
//            // 第一步：查询数据
//            List<PayChannel> channels = Collections.emptyList();
//            try {
//                channels = channelMapper.selectList();
//            } catch (Throwable ex) {
//                if (!ex.getMessage().contains("doesn't exist")) {
//                    throw ex;
//                }
//                log.error("[支付模块 yudao-module-pay - 表结构未导入][参考 https://doc.iocoder.cn/pay/build/ 开启]");
//            }
//            log.info("[initLocalCache][缓存支付渠道，数量为:{}]", channels.size());
//
//            // 第二步：构建缓存：创建或更新支付 Client
//            channels.forEach(payChannel -> payClientFactory.createOrUpdatePayClient(payChannel.getId(),
//                    payChannel.getCode(), payChannel.getConfig()));
//            this.channelCache = channels;
//        });
    }

    /**
     * 通过定时任务轮询，刷新缓存
     *
     * 目的：多节点部署时，通过轮询”通知“所有节点，进行刷新
     */
    @Scheduled(initialDelay = 60, fixedRate = 60, timeUnit = TimeUnit.SECONDS)
    public void refreshLocalCache() {
        // 注意：忽略自动多租户，因为要全局初始化缓存
//        TenantUtils.executeIgnore(() -> {
//            // 情况一：如果缓存里没有数据，则直接刷新缓存
//            if (CollUtil.isEmpty(channelCache)) {
//                initLocalCache();
//                return;
//            }
//
//            // 情况二，如果缓存里数据，则通过 updateTime 判断是否有数据变更，有变更则刷新缓存
//            LocalDateTime maxTime = CollectionUtils.getMaxValue(channelCache, PayChannel::getUpdateTime);
//            if (channelMapper.selectCountByUpdateTimeGt(maxTime) > 0) {
//                initLocalCache();
//            }
//        });
    }

    @Override
    public Long createChannel(PayChannelCreateReqVO reqVO) {
        // 断言是否有重复的
        PayChannel dbChannel = getChannelByAppIdAndCode(reqVO.getAppId(), reqVO.getCode());
        if (dbChannel != null) {
            throw exception(CHANNEL_EXIST_SAME_CHANNEL_ERROR);
        }

        // 新增渠道
        PayChannel channel = PayChannelConvert.INSTANCE.convert(reqVO)
                .setConfig(parseConfig(reqVO.getCode(), reqVO.getConfig()));
        payChannelMapper.insert(channel);

        // 刷新缓存
        initLocalCache();
        return channel.getId();
    }

    @Override
    public void updateChannel(PayChannelUpdateReqVO updateReqVO) {
        // 校验存在
        PayChannel dbChannel = validateChannelExists(updateReqVO.getId());

        // 更新
        PayChannel channel = PayChannelConvert.INSTANCE.convert(updateReqVO)
                .setConfig(parseConfig(dbChannel.getCode(), updateReqVO.getConfig()));
        payChannelMapper.updateById(channel);

        // 刷新缓存
        initLocalCache();
    }

    /**
     * 解析并校验配置
     *
     * @param code      渠道编码
     * @param configStr 配置
     * @return 支付配置
     */
    private PayClientConfig parseConfig(String code, String configStr) {
        // 解析配置
        Class<? extends PayClientConfig> payClass = PayChannelEnum.getByCode(code).getConfigClass();
        if (ObjectUtil.isNull(payClass)) {
            throw exception(CHANNEL_NOT_FOUND);
        }
        PayClientConfig config = JsonUtils.parseObject2(configStr, payClass);
        Assert.notNull(config);

        // 验证参数
        config.validate(validator);
        return config;
    }

    @Override
    public void deleteChannel(Long id) {
        // 校验存在
        validateChannelExists(id);

        // 删除
        payChannelMapper.deleteById(id);

        // 刷新缓存
        initLocalCache();
    }

    private PayChannel validateChannelExists(Long id) {
        PayChannel channel = payChannelMapper.selectById(id);
        if (channel == null) {
            throw exception(CHANNEL_NOT_FOUND);
        }
        return channel;
    }

    @Override
    public PayChannel getChannel(Long id) {
        return payChannelMapper.selectById(id);
    }

    @Override
    public List<PayChannel> getChannelListByAppIds(Collection<Long> appIds) {
        return payChannelMapper.selectList(PayChannel::getAppId, appIds);
    }

    @Override
    public PayChannel getChannelByAppIdAndCode(Long appId, String code) {
        return this.selectByAppIdAndCode(appId, code);
    }

    @Override
    public PayChannel validPayChannel(Long id) {
        PayChannel channel = payChannelMapper.selectById(id);
        validPayChannel(channel);
        return channel;
    }

    @Override
    public PayChannel validPayChannel(Long appId, String code) {
        PayChannel channel = this.selectByAppIdAndCode(appId, code);
        validPayChannel(channel);
        return channel;
    }

    private void validPayChannel(PayChannel channel) {
        if (channel == null) {
            throw exception(CHANNEL_NOT_FOUND);
        }
        if (CommonStatusEnum.DISABLE.getStatus().equals(channel.getStatus())) {
            throw exception(CHANNEL_IS_DISABLE);
        }
    }

    @Override
    public List<PayChannel> getEnableChannelList(Long appId) {
        return payChannelMapper.selectList(new LambdaQueryWrapperX<PayChannel>()
                .eq(PayChannel::getAppId, appId)
                .eq(PayChannel::getStatus, CommonStatusEnum.ENABLE.getStatus()));
    }

    @Override
    public PayChannel selectByAppIdAndCode(Long appId, String code) {
        return payChannelMapper.selectOne(PayChannel::getAppId, appId, PayChannel::getCode, code);
    }

}
