package com.fastbee.http.handler;

import io.netty.handler.codec.http.FullHttpRequest;

import javax.servlet.http.HttpSession;

public interface IHttpReqHandler {
    public void processMsg(FullHttpRequest req, HttpSession session);
}
