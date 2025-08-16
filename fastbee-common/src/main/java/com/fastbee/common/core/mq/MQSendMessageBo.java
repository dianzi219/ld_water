package com.fastbee.common.core.mq;

import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.core.device.DeviceAndProtocol;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务(指令)下发对象
 *
 * @author bill
 */
@Data
@NoArgsConstructor
public class MQSendMessageBo {

    /**
     * 设备编号
     */
    private String serialNumber;
    /**
     * 下发属性标识符
     */
    private String identifier;
    /**
     * 下发参数
     */
    private JSONObject params;

    /**
     * 下发的值
     */
    private String value;
    /**
     * messageId生成放到调用接口的时候生成
     */
    private String messageId;
    /**
     * 协议产品相关信息
     */
    private DeviceAndProtocol dp;

    /**
     * 物模型
     */
    private String thingsModel;
    /**
     * 主题
     */
    private String topicName;


    public Boolean isShadow;

    public Long userId;

    public Long delay;

    private String parentSerialNumber;

}
