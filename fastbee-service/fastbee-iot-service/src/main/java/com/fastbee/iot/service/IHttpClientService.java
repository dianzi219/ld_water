package com.fastbee.iot.service;

import java.util.List;

import com.fastbee.http.model.HttpClientConfig;
import com.fastbee.iot.domain.HttpClient;

/**
 * http桥接配置Service接口
 *
 * @author gx_magx_ma
 * @date 2024-06-03
 */
public interface IHttpClientService
{

    /**
     * 构建httpclient配置
     * @param httpClient
     * @return
     */
    public HttpClientConfig buildhttpclientconfig(HttpClient httpClient);
}
