package com.fastbee.modbustcp.codec;

import com.fastbee.common.core.mq.message.DeviceData;
import com.fastbee.common.core.protocol.modbus.ModbusCode;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.modbus.pak.TcpDtu;
import com.fastbee.modbustcp.model.ModbusTcp;
import com.fastbee.protocol.WModelManager;
import com.fastbee.protocol.base.model.ActiveModel;
import com.fastbee.protocol.util.ArrayMap;
import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * modbus-tcp协议解码器
 *
 * @author bill
 */
@Slf4j
@Component
@NoArgsConstructor
public class ModbusTcpDecoder {

    @Resource
    private WModelManager modelManager;
    private ArrayMap<ActiveModel> headerSchemaMap;

    public ModbusTcpDecoder(String... basePackages) {
        this.modelManager = new WModelManager(basePackages);
        this.headerSchemaMap = this.modelManager.getActiveMap(ModbusTcp.class);
    }

    public ModbusTcp decode(DeviceData deviceData) {
        try {
            this.build();
            ByteBuf in = deviceData.getBuf();
            int start = in.getUnsignedByte(1);
            int messageId = in.getUnsignedByte(7);
            int currentMessageId = messageId;
            if (!(start == TcpDtu.起始位 && (messageId == TcpDtu.注册报文 || messageId == TcpDtu.心跳包))) {
                //03解析
                currentMessageId = 0;
                if (deviceData.getCode() == ModbusCode.Write06 || messageId == ModbusCode.Write06.getCode() ||
                    deviceData.getCode() == ModbusCode.Write05 || messageId == ModbusCode.Write05.getCode()) {
                    //设备回复06解析
                    currentMessageId = 2;
                } else if (deviceData.getCode() == ModbusCode.Read01 || messageId == ModbusCode.Read01.getCode() ||
                        deviceData.getCode() == ModbusCode.Read02 || messageId == ModbusCode.Read02.getCode()) {
                    // 01、02解析
                    currentMessageId = 4;
                }
//               in = verify(in);
            }
            messageId = currentMessageId;
            ActiveModel<ModbusTcp> activeModel = headerSchemaMap.get(messageId);
            ModbusTcp message = new ModbusTcp();
            message.setPayload(in);
            activeModel.mergeFrom(in, message, null);
            log.info("=>解析:[{}]", message);
            return message;
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    }

    private void build() {
        if (this.headerSchemaMap == null) {
            this.headerSchemaMap = this.modelManager.getActiveMap(ModbusTcp.class);
        }
    }


}
