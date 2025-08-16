package com.fastbee.sgz;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author gsb
 * @date 2024/3/29 11:31
 */
@Getter
@AllArgsConstructor
public enum SgzMessageType {

    HEART(150,"heart","心跳"),
    LINKING(151," LINKING"," LINKING已连接"),
    CZOK(152," CZOK","开始充装"),
    UNLOCK(153,"UNLOCK","已解锁"),
    CZSTOP(154,"CZSTOP","数据上报"),
    CZROK(155,"CZROK","充装结束回复设备");

    int messageId;
    String type;
    String desc;

    public static SgzMessageType convert(String type){
        for (SgzMessageType value : SgzMessageType.values()) {
            if (value.type.equals(type)){
                return value;
            }
        }
        return HEART;
    }

}
