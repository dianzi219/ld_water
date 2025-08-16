package com.fastbee.notify.core.email.config;

import com.fastbee.common.core.notify.config.EmailConfigParams;
import org.dromara.email.api.MailClient;
import org.dromara.email.comm.config.MailSmtpConfig;
import org.dromara.email.core.factory.MailFactory;

/**
 * @author fastb
 * @version 1.0
 * @description: 邮箱获取发送配置类
 * @date 2023-12-20 10:23
 */
public class EmailNotifyConfig {

    public static MailClient create(String mailClientKey, EmailConfigParams emailNotifyConfig) {
        MailSmtpConfig config = MailSmtpConfig.builder()
                .smtpServer(emailNotifyConfig.getSmtpServer())
                .port(emailNotifyConfig.getPort())
                .fromAddress(emailNotifyConfig.getUsername())
                .username(emailNotifyConfig.getUsername())
                .password(emailNotifyConfig.getPassword())
                .isSSL(emailNotifyConfig.getSslEnable().toString())
                .isAuth(emailNotifyConfig.getAuthEnable().toString())
                .retryInterval(emailNotifyConfig.getRetryInterval())
                .maxRetries(emailNotifyConfig.getMaxRetries())
                .build();
        MailFactory.put(mailClientKey, config);
        return MailFactory.createMailClient(mailClientKey);
    }

}
