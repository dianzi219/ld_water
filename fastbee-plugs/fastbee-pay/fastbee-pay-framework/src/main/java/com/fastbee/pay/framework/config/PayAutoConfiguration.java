package com.fastbee.pay.framework.config;

import com.fastbee.pay.framework.client.PayClientFactory;
import com.fastbee.pay.framework.client.impl.PayClientFactoryImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 支付配置类
 *
 * @author fastbee
 */
@AutoConfiguration
public class PayAutoConfiguration {

    @Bean
    public PayClientFactory payClientFactory() {
        return new PayClientFactoryImpl();
    }

}
