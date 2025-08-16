package com.fastbee.iot.data.service;

/**
 * 设备消息推送mq
 * @author bill
 */
public interface IMessagePublishService {


    /**
     * 发布消息到mq
     * @param message 设备消息
     * @param channel 推送channel
     */
    public void publish(Object message,String channel);

}
