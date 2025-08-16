package com.fastbee.common.core.mq.message;

import com.fastbee.common.core.ota.OtaPackageCode;
import com.fastbee.common.core.protocol.Message;
import com.fastbee.common.core.protocol.modbus.ModbusCode;
import io.netty.buffer.ByteBuf;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息解析model
 * @author gsb
 * @date 2022/10/10 15:53
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class DeviceData extends Message {

    /*topic*/
    private String topicName;

    /*设备编号*/
    private String serialNumber;

    /*原数据*/
    private byte[] data;

    private ByteBuf buf;

    private Object body;
    /*MQTT OR 其他*/
    private int type;
    /*Modbus*/
    private ModbusCode code;
    /**产品id*/
    private Long productId;

    private OtaPackageCode netModbusCode;

    private int bitCount;
}
