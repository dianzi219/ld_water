package com.fastbee.pay.framework.client.impl;

import cn.hutool.core.lang.Assert;
import com.fastbee.pay.framework.client.PayClient;
import com.fastbee.pay.framework.client.PayClientConfig;
import com.fastbee.pay.framework.client.PayClientFactory;
import com.fastbee.pay.framework.client.impl.alipay.AlipayAppPayClient;
import com.fastbee.pay.framework.client.impl.alipay.AlipayBarPayClient;
import com.fastbee.pay.framework.client.impl.alipay.AlipayPayClientConfig;
import com.fastbee.pay.framework.client.impl.alipay.AlipayPcPayClient;
import com.fastbee.pay.framework.client.impl.alipay.AlipayQrPayClient;
import com.fastbee.pay.framework.client.impl.alipay.AlipayWapPayClient;
import com.fastbee.pay.framework.client.impl.mock.MockPayClient;
import com.fastbee.pay.framework.client.impl.mock.MockPayClientConfig;
import com.fastbee.pay.framework.client.impl.weixin.WxAppPayClient;
import com.fastbee.pay.framework.client.impl.weixin.WxBarPayClient;
import com.fastbee.pay.framework.client.impl.weixin.WxLitePayClient;
import com.fastbee.pay.framework.client.impl.weixin.WxNativePayClient;
import com.fastbee.pay.framework.client.impl.weixin.WxPayClientConfig;
import com.fastbee.pay.framework.client.impl.weixin.WxPubPayClient;
import com.fastbee.pay.framework.enums.channel.PayChannelEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 支付客户端的工厂实现类
 *
 * @author fastbee
 */
@Slf4j
public class PayClientFactoryImpl implements PayClientFactory {

    /**
     * 支付客户端 Map
     * key：渠道编号
     */
    private final ConcurrentMap<Long, AbstractPayClient<?>> clients = new ConcurrentHashMap<>();

    @Override
    public PayClient getPayClient(Long channelId) {
        AbstractPayClient<?> client = clients.get(channelId);
        if (client == null) {
            log.error("[getPayClient][渠道编号({}) 找不到客户端]", channelId);
        }
        return client;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Config extends PayClientConfig> void createOrUpdatePayClient(Long channelId, String channelCode,
                                                                         Config config) {
        AbstractPayClient<Config> client = (AbstractPayClient<Config>) clients.get(channelId);
        if (client == null) {
            client = this.createPayClient(channelId, channelCode, config);
            client.init();
            clients.put(client.getId(), client);
        } else {
            client.refresh(config);
        }
    }

    @SuppressWarnings("unchecked")
    private <Config extends PayClientConfig> AbstractPayClient<Config> createPayClient(
            Long channelId, String channelCode, Config config) {
        PayChannelEnum channelEnum = PayChannelEnum.getByCode(channelCode);
        Assert.notNull(channelEnum, String.format("支付渠道(%s) 为空", channelEnum));
        // 创建客户端
        switch (channelEnum) {
            // 微信支付
            case WX_PUB: return (AbstractPayClient<Config>) new WxPubPayClient(channelId, (WxPayClientConfig) config);
            case WX_LITE: return (AbstractPayClient<Config>) new WxLitePayClient(channelId, (WxPayClientConfig) config);
            case WX_APP: return (AbstractPayClient<Config>) new WxAppPayClient(channelId, (WxPayClientConfig) config);
            case WX_BAR: return (AbstractPayClient<Config>) new WxBarPayClient(channelId, (WxPayClientConfig) config);
            case WX_NATIVE: return (AbstractPayClient<Config>) new WxNativePayClient(channelId, (WxPayClientConfig) config);
            // 支付宝支付
            case ALIPAY_WAP: return (AbstractPayClient<Config>) new AlipayWapPayClient(channelId, (AlipayPayClientConfig) config);
            case ALIPAY_QR: return (AbstractPayClient<Config>) new AlipayQrPayClient(channelId, (AlipayPayClientConfig) config);
            case ALIPAY_APP: return (AbstractPayClient<Config>) new AlipayAppPayClient(channelId, (AlipayPayClientConfig) config);
            case ALIPAY_PC: return (AbstractPayClient<Config>) new AlipayPcPayClient(channelId, (AlipayPayClientConfig) config);
            case ALIPAY_BAR: return (AbstractPayClient<Config>) new AlipayBarPayClient(channelId, (AlipayPayClientConfig) config);
            // 其它支付
            case MOCK: return (AbstractPayClient<Config>) new MockPayClient(channelId, (MockPayClientConfig) config);
        }
        // 创建失败，错误日志 + 抛出异常
        log.error("[createPayClient][配置({}) 找不到合适的客户端实现]", config);
        throw new IllegalArgumentException(String.format("配置(%s) 找不到合适的客户端实现", config));
    }

}
