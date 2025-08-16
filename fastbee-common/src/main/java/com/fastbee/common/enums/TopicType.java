package com.fastbee.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * topic类型
 * @author gsb
 */
@Getter
@AllArgsConstructor
public enum TopicType {

    /**
     * @param     type  0:标记是订阅主题  1:标记是发布属性
     * @param     order 排序
     * @param     topicSuffix topic后缀
     * @param     msg  描述信息
     */

    /*** 通用设备上报主题（平台订阅） ***/
    PROPERTY_POST(0,1,"/property/post", "订阅属性"),
    EVENT_POST(0,2,"/event/post", "订阅事件"),
    FUNCTION_POST(0,3,"/function/post", "订阅功能"),
    INFO_POST(0,4,"/info/post","订阅设备信息"),
    NTP_POST(0,5,"/ntp/post","订阅时钟同步"),
    SERVICE_INVOKE_REPLY(0,8,"/service/reply", "订阅功能调用返回结果"),
    MESSAGE_POST(0,26,"/message/post","订阅设备上报消息"),

    /*** 通用设备订阅主题（平台下发）***/
    FUNCTION_GET(1,17,"/function/get", "发布功能"),
    PROPERTY_GET(1,12,"/property/get" ,"发布设备属性读取"),
    PROPERTY_SET(1,15,"/property/set" ,"设置设备属性读取"),
    STATUS_POST(1,11,"/status/post","发布状态"),
    NTP_GET(1,15,"/ntp/get","发布时钟同步"),
    INFO_GET(1,18,"/info/get","发布设备信息"),


    /*** 视频监控设备转协议发布 ***/
    DEV_INFO_POST(3,19,"/info/post","设备端发布设备信息"),
    DEV_EVENT_POST(3,20,"/event/post","设备端发布事件"),
    DEV_PROPERTY_POST(3,22,"/property/post", "设备端发布属性"),


    /*** webSocket转发前端使用  ***/
    WS_SERVICE_INVOKE(2,16,"/ws/service", "WS服务调用"),

    /**
     * OTA升级
     */
    WS_OTA_STATUS(2,18,"/ws/ota/status","OTA升级状态"),
    HTTP_FIRMWARE_SET(1,14, "/http/upgrade/set","http推送发布OTA升级"),
    FETCH_FIRMWARE_SET(1,19, "/fetch/upgrade/set","分包拉取方式发布OTA升级"),
    FETCH_UPGRADE_REPLY(0,9,"/fetch/upgrade/reply", "分包拉取方式OTA升级回调"),
    HTTP_UPGRADE_REPLY(0,9,"/http/upgrade/reply", "http推送方式OTA升级回调")
    ;

    Integer type;
    Integer order;
    String topicSuffix;
    String msg;

    public static TopicType getType(String topicSuffix) {
        for (TopicType value : TopicType.values()) {
            if (value.topicSuffix.equals(topicSuffix)) {
                return value;
            }
        }
        return TopicType.PROPERTY_POST;
    }

}
