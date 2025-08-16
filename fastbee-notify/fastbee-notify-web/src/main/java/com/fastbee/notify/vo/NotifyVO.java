package com.fastbee.notify.vo;

import com.fastbee.common.enums.NotifyChannelProviderEnum;
import com.fastbee.notify.domain.NotifyChannel;
import com.fastbee.notify.domain.NotifyTemplate;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;

/**
 * @author fastb
 * @version 1.0
 * @description: 通知发送参数
 * @date 2024-01-02 11:10
 */
@Data
@Accessors(chain = true)
public class NotifyVO {

    private NotifyChannel notifyChannel;

    private NotifyTemplate notifyTemplate;

    /**
     * 多个账号用英文逗号隔开 例如：21,51
     */
    private String sendAccount;

    private LinkedHashMap<String,String> map;

    private NotifyChannelProviderEnum notifyChannelProviderEnum;
}
