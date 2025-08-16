package com.fastbee.iot.model;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * EMQX API 配置类
 *
 * @author gx_ma
 * @date 2024/06/27
 */
@Component
@ConfigurationProperties(prefix = "emqx")
@Data
public class EmqxApiConfig {
    /*emqx服务器地址*/
    private String host;

    /*emqx服务器端口*/
    private String port;

    /*emqx服务器apiKey*/
    private String ApiKey;

    /*emqx服务器secretKey*/
    private String ApiSecret;
}
