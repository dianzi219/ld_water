package com.fastbee.common.core.notify.config;

import lombok.Data;

/**
 * 语音配置
 * @author fastb
 * @date 2023-12-09 17:32
 */
@Data
public class VoiceConfigParams {

    /**
     * 您的AccessKey ID
     */
    private String accessKeyId;

    /**
     * 您的AccessKey Secret
     */
    private String accessKeySecret;

}
