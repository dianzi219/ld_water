package com.fastbee.http.auth;

import com.fastbee.http.utils.DigestAuthUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Slf4j
@Component
public class DigestAuth {

    private static final String REALM = "FastbeeDigest";
    private static final String NONCE = UUID.randomUUID().toString();
    @Value("${server.http.auth.user.name)")
    private String user;
    @Value("${server.http.auth.user.password)")
    private String password;

    public boolean auth(ChannelHandlerContext ctx, FullHttpRequest request) throws NoSuchAlgorithmException {
        if (new DigestAuthUtil().doAuthenticatePlainTextPassword(request,
                password)) {
            return true;
        } else {
            // 发送401 Unauthorized响应
            sendUnauthorizedResponse(ctx);
            return false;
        }
    }

    public void sendUnauthorizedResponse(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED,
                Unpooled.copiedBuffer("Unauthorized\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.WWW_AUTHENTICATE,
                "Digest realm=\"" + REALM + "\", nonce=\"" + NONCE + "\"");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

}
