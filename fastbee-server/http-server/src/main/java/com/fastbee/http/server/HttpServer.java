package com.fastbee.http.server;

import com.fastbee.server.Server;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class HttpServer extends Server {

    @Autowired
    private HttpServerHandler httpServerHandler;

    @Override
    protected AbstractBootstrap initialize() {
        bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory(config.name, Thread.MAX_PRIORITY));
        workerGroup = new NioEventLoopGroup(config.workerCore, new DefaultThreadFactory(config.name, Thread.MAX_PRIORITY));
        if (config.businessCore > 0) {
            businessService = new ThreadPoolExecutor(config.businessCore, config.businessCore, 1L,
                    TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new DefaultThreadFactory(config.name + "-B", true, Thread.NORM_PRIORITY));
        }
        return new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(NioChannelOption.SO_REUSEADDR, true)
                .option(NioChannelOption.SO_BACKLOG, 1024)
                .childOption(NioChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new HttpServerCodec());
                        p.addLast(new HttpObjectAggregator(65536));
                        p.addLast(httpServerHandler);
                    }
                });
    }
}
