package com.fastbee.notify.core.voice.config;

import com.aliyun.dyvmsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;

/**
 * @author fastb
 * @version 1.0
 * @description: 语音配置类
 * @date 2024-01-11 16:06
 */
public class VoiceConfig {

    /**
     * 使用AK&SK初始化账号Client
     * @param accessKeyId
     * @param accessKeySecret
     * @return Client
     * @throws Exception
     */
    public static Client createClient(String accessKeyId, String accessKeySecret) throws Exception {
        Config config = new Config()
                // 必填，您的 AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 必填，您的 AccessKey Secret
                .setAccessKeySecret(accessKeySecret);
        // Endpoint 请参考 https://api.aliyun.com/product/Dyvmsapi
        config.endpoint = "dyvmsapi.aliyuncs.com";
        return new Client(config);
    }
}
