package com.fastbee.bootstrap.coap;

import com.fastbee.coap.Coapserver;
import com.fastbee.common.enums.ServerType;
import com.fastbee.server.Server;
import com.fastbee.server.config.NettyConfig;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Order(13)
@Configuration
@ConditionalOnProperty(name = "server.coap.enabled", havingValue = "true")//设置是否启动
@ConfigurationProperties(value = "server.coap")
@Data
public class CoapBootStrap {
    @Autowired
    private Coapserver coapserver;
    private int port;
    @Bean(initMethod = "start",destroyMethod = "stop")
    public Server coapServerBoot(){
        return NettyConfig.custom()
                .setPort(port)
                .setName(ServerType.COAP.getDes())
                .setType(ServerType.COAP)
                .setServer(coapserver)
                .build();
    }
}
