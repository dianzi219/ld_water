package com.fastbee.http.server;

import com.fastbee.http.handler.IHttpReqHandler;
import com.fastbee.http.handler.IHttpResHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class HttpListener {
    private static final Map<String, IHttpReqHandler> requestProcessorMap = new ConcurrentHashMap<>();
    private static final Map<String, IHttpResHandler> responseProcessorMap = new ConcurrentHashMap<>();

    public void addRequestProcessor(String method, IHttpReqHandler processor) {
        requestProcessorMap.put(method, processor);
    }

    public void addResponseProcessor(String method, IHttpResHandler processor) {
        responseProcessorMap.put(method, processor);
    }

    @Async("taskExecutor")
    public void processRequest(FullHttpRequest req, HttpSession session) {
        String uri = req.uri();
        IHttpReqHandler sipRequestProcessor = requestProcessorMap.get(uri);
        if (sipRequestProcessor == null) {
            log.warn("不支持的uri:{}", uri);
            return;
        }
        requestProcessorMap.get(uri).processMsg(req, session);
    }

    @Async("taskExecutor")
    public void processResponse(FullHttpResponse response) {
        HttpResponseStatus status = response.status();
        // 响应成功
        if ((status.code() >= HttpResponseStatus.OK.code()) && (status.code() < HttpResponseStatus.MULTIPLE_CHOICES.code())) {
            log.info("response：{},", response.content());
            log.info("接收response响应！status：{}", status);

        } else if ((status.code() >= HttpResponseStatus.CONTINUE.code()) && (status.code() < HttpResponseStatus.OK.code())) {
            log.info("接收response响应！status：{}", status);
        } else {
            log.warn("接收到失败的response响应！status：{}", status);
        }
    }
}
