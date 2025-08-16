package com.fastbee.http.server;

import com.fastbee.http.auth.BasicAuth;
import com.fastbee.http.auth.DigestAuth;
import com.fastbee.http.manager.NettyHttpSession;
import com.fastbee.http.manager.HttpSessionManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.util.Set;

@Slf4j
@Component
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    @Autowired
    private HttpSessionManager sessionManager;

    @Autowired
    private HttpListener httpListener;

    @Autowired
    private BasicAuth basicAuth;

    @Autowired
    private DigestAuth digestAuth;

    @Value("${server.http.auth.type}")
    private String authtype;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        String sessionId = null;
        HttpSession session;
        String cookieHeader = req.headers().get(HttpHeaderNames.COOKIE);
        // 使用ServerCookieDecoder解码Cookie字符串
        Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieHeader);
        // 遍历Cookies
        for (Cookie cookie : cookies) {
            if ("JSESSIONID".equals(cookie.name())) {
                sessionId = cookie.value();
            }
        }
        // 未认证
        if (sessionId == null) {
            String authHeader = req.headers().get(HttpHeaderNames.AUTHORIZATION);
            boolean check = false;
            if (authHeader != null && authHeader.startsWith("Basic ")) {
                if (basicAuth.auth(ctx, authHeader)) {
                    check = true;
                }
            } else if (authHeader != null && authHeader.startsWith("Digest ")) {
                if (digestAuth.auth(ctx, req)) {
                    check = true;
                }
            }
            if (check) {
                sessionId = sessionManager.createSession();
                session = sessionManager.getSession(sessionId);
                ((NettyHttpSession) session).setAttribute("user", "John Doe");
                // 创建一个Cookie
                Cookie sessionCookie = new DefaultCookie("JSESSIONID", sessionId);
                // 设置一些属性，比如路径和最大年龄
                sessionCookie.setPath("/");
                sessionCookie.setMaxAge(30 * 60); // 30分钟
                // 编码Cookie并添加到响应头中
                FullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                res.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(sessionCookie));
                ctx.writeAndFlush(res);
            } else {
                if ("Basic".equals(authtype)) {
                    basicAuth.sendUnauthorizedResponse(ctx);
                } else {
                    digestAuth.sendUnauthorizedResponse(ctx);
                }
            }
        } else {
            // 已经认证
            session = sessionManager.getSession(sessionId);
            // http 路由处理函数
            httpListener.processRequest(req, session);
        }
    }
}
