package com.fastbee.coap.handler;

import com.fastbee.coap.model.CoapMessage;
import com.fastbee.coap.model.CoapRequest;
import com.fastbee.coap.server.ResourceRegistry;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@ChannelHandler.Sharable
@Slf4j
@Setter
@Getter
public class ReqDispatcher extends SimpleChannelInboundHandler<CoapMessage> {

    private ResourceRegistry resourceRegistry;
    private ChannelHandlerContext context;

    public ReqDispatcher(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, CoapMessage coapMessage) throws Exception {
        log.debug("ReqDispatcher message: {}", coapMessage);
        if (!(coapMessage instanceof CoapRequest)) {
            log.info("remoteAddress:{}",coapMessage.getSender());
            return ;
        }
        final CoapRequest coapRequest = (CoapRequest) coapMessage;
        channelHandlerContext.writeAndFlush(resourceRegistry.respond(coapRequest));
    }

}
