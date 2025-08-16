package com.fastbee.http.handler;

import io.netty.handler.codec.http.FullHttpResponse;

import java.text.ParseException;

public interface IHttpResHandler {
    public void processMsg(FullHttpResponse req) throws ParseException;
}
