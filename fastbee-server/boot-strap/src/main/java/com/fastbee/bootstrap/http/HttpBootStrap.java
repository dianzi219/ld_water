package com.fastbee.bootstrap.http;

import com.fastbee.common.enums.ServerType;
import com.fastbee.http.server.HttpServer;
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
@ConditionalOnProperty(name = "server.http.enabled", havingValue = "true")//设置是否启动
@ConfigurationProperties(value = "server.http")
@Data
public class HttpBootStrap {
    @Autowired
    private HttpServer httpServer;
    private int port;

    @Bean(initMethod = "start",destroyMethod = "stop")
    public Server httpServerBoot(){
        return NettyConfig.custom()
                .setPort(port)
                .setName(ServerType.HTTP.getDes())
                .setType(ServerType.HTTP)
                .setServer(httpServer)
                .build();
    }
}
