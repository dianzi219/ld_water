package com.fastbee.http.auth;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Component
public class BasicAuth {
    @Value("${server.http.auth.user.name}")
    private String user;
    @Value("${server.http.auth.user.password}")
    private String password;
    private static final String REALM = "FastbeeBasic";

    public boolean auth(ChannelHandlerContext ctx, String authHeader) {
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            String encodedCredentials = authHeader.substring("Basic ".length());
            String credentials = new String(Base64.getDecoder().decode(encodedCredentials), StandardCharsets.UTF_8);
            String[] parts = credentials.split(":", 2);
            if (parts.length == 2 && validateUser(parts[0], parts[1])) {
                // 用户名和密码验证通过，继续处理请求
                return true;
            } else {
                // 发送401 Unauthorized响应
                sendUnauthorizedResponse(ctx);
            }
        } else {
            // 发送401 Unauthorized响应
            sendUnauthorizedResponse(ctx);
        }
        return false;
    }

    private boolean validateUser(String username, String password) {
        // 实现用户验证逻辑
        return username.equals(user) && password.equals(this.password);
    }

    public void sendUnauthorizedResponse(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED,
                Unpooled.copiedBuffer("Unauthorized\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.WWW_AUTHENTICATE, "Basic realm=\"" + REALM + "\"");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
