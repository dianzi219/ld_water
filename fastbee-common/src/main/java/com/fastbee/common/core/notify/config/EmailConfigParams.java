package com.fastbee.common.core.notify.config;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 邮箱配置参数
 * @author gsb
 * @date 2023/12/11 17:17
 */
@Data
public class EmailConfigParams {

    @ApiModelProperty("服务器地址")
    private String smtpServer;

    @ApiModelProperty("端口号")
    private String port;

    @ApiModelProperty("账号（发件人地址）")
    private String username;

    @ApiModelProperty("密码")
    private String password;

    /**
     * 是否开启ssl 默认开启 QQ之类的邮箱默认都需要ssl
     */
    @ApiModelProperty("是否启动ssl")
    private Boolean sslEnable;

    /**
     * 是否开启验证 默认开启
     */
    @ApiModelProperty("启动ttl")
    private Boolean authEnable;

    /**
     * 重试间隔（单位：秒），默认为5秒
     */
    private Integer retryInterval = 5;

    /**
     * 重试次数，默认为1次
     */
    private Integer maxRetries = 1;

}
