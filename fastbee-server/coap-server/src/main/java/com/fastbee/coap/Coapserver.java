package com.fastbee.coap;

import com.fastbee.coap.handler.TimeResourceHandler;
import com.fastbee.coap.server.CoapServerChannelInitializer;
import com.fastbee.coap.server.ResourceRegistry;
import com.fastbee.server.Server;
import com.fastbee.server.config.NettyConfig;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class Coapserver extends Server {
    @Override
    protected AbstractBootstrap initialize() {
        bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory(config.name, Thread.MAX_PRIORITY));
        if (config.businessCore > 0) {
            businessService = new ThreadPoolExecutor(config.businessCore, config.businessCore, 1L,
                    TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new DefaultThreadFactory(config.name, true, Thread.NORM_PRIORITY));
        }
        //注册数据路由
        ResourceRegistry resourceRegistry = new ResourceRegistry();
        resourceRegistry.register(new TimeResourceHandler("/utc-time"));
        return new Bootstrap()
                .group(bossGroup)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new CoapServerChannelInitializer(resourceRegistry));
    }
}
