package com.fastbee.common.wechat;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * 微信回调的时候入参XML解析节点封装BO
 * @author fastb
 * @date 2024-03-12 15:24
 * @version 1.0
 */
@Data
public class WxCallBackXmlBO {

    @JSONField(name = "MsgType")
    private String msgType;

    @JSONField(name = "FromUserName")
    private String fromUserName;

    @JSONField(name = "ToUserName")
    private String toUserName;

    @JSONField(name = "CreateTime")
    private String createTime;

    @JSONField(name = "Content")
    private String content;

    @JSONField(name = "MsgId")
    private String msgId;

    @JSONField(name = "Event")
    private String event;

    @JSONField(name = "EventKey")
    private String eventKey;

    @JSONField(name = "Ticket")
    private String ticket;

    @JSONField(name = "UnionId")
    private String unionId;

    @JSONField(name = "Recognition")
    private String recognition;

    @JSONField(name = "PicUrl")
    private String picUrl;

    @JSONField(name = "SuccessOrderId")
    private String successOrderId;

    @JSONField(name = "CardId")
    private String cardId;

    @JSONField(name = "UserCardCode")
    private String userCardCode;

    @JSONField(name = "LocationX")
    private String locationX;

    @JSONField(name = "LocationY")
    private String locationY;


}
