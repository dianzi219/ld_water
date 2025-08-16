package com.fastbee.iot.service.impl;

import java.util.Map;
import com.dtflys.forest.http.ForestRequestType;
import com.fastbee.http.model.HttpClientConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import com.fastbee.iot.domain.HttpClient;
import com.fastbee.iot.service.IHttpClientService;

/**
 * http桥接配置Service业务层处理
 *
 * @author gx_magx_ma
 * @date 2024-06-03
 */
@Service
public class HttpClientServiceImpl implements IHttpClientService {

    public HttpClientConfig buildhttpclientconfig(HttpClient httpClient) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            HttpClientConfig.HttpClientConfigBuilder builder  = HttpClientConfig.builder();
            if (httpClient.getRequestHeaders() != null) {
                Map<String, Object> headers = mapper.readValue(httpClient.getRequestHeaders(), Map.class);
                builder.headers(headers);
            }
            if (httpClient.getRequestQuerys() != null) {
                Map<String, Object> querys = mapper.readValue(httpClient.getRequestQuerys(), Map.class);
                builder.querys(querys);
            }
            return builder
                    .url(httpClient.getHostUrl())
                    .method(ForestRequestType.findType(httpClient.getMethod()))
                    .backend("okhttp3")
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
