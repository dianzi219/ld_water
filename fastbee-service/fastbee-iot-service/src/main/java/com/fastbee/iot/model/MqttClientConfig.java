package com.fastbee.iot.model;

import lombok.Builder;
import lombok.Data;

/** mqtt配置信息*/

@Data
@Builder
public class MqttClientConfig {

    private String key;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 连接地址
     */
    private String hostUrl;
    /**
     * 客户Id
     */
    private String clientId;
    /**
     * 默认连接话题
     */
    private String defaultTopic;
    /**
     * 超时时间
     */
    private int timeout;
    /**
     * 保持连接数
     */
    private int keepalive;

    /**是否清除session*/
    private boolean clearSession;
    /**是否共享订阅*/
    private boolean isShared;
    /**分组共享订阅*/
    private boolean isSharedGroup;

}

