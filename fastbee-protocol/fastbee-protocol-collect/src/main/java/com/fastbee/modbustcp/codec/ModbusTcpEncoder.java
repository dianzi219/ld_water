package com.fastbee.modbustcp.codec;

import com.fastbee.common.core.protocol.modbus.ModbusCode;
import com.fastbee.common.utils.BitUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.modbus.model.ModbusRtu;
import com.fastbee.modbustcp.model.ModbusTcp;
import com.fastbee.protocol.WModelManager;
import com.fastbee.protocol.base.model.ActiveModel;
import com.fastbee.protocol.util.ArrayMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.fastbee.common.core.protocol.modbus.ModbusCode.getInstance;

/**
 * modbus-tcp协议编码器
 *
 * @author bill
 */
@Slf4j
@Component
@NoArgsConstructor
public class ModbusTcpEncoder {

    private static final ByteBufAllocator ALLOC = PooledByteBufAllocator.DEFAULT;

    @Autowired
    private WModelManager modelManager;

    private ArrayMap<ActiveModel> headerSchemaMap;

    public ModbusTcpEncoder(String... basePackages) {
        this.modelManager = new WModelManager(basePackages);
        this.headerSchemaMap = this.modelManager.getActiveMap(ModbusTcp.class);
    }

    public ModbusTcp change(ModbusRtu modbusRtu) {
        ModbusTcp modbusTcp = new ModbusTcp();
        modbusTcp.setSlaveId(modbusRtu.getSlaveId());
        modbusTcp.setCode(modbusRtu.getCode());
        modbusTcp.setAddress(modbusRtu.getAddress());
        modbusTcp.setCount(modbusRtu.getCount());
        modbusTcp.setSerialNumber(modbusRtu.getSerialNumber());
        modbusTcp.setWriteData(modbusRtu.getWriteData());
        modbusTcp.setTenWriteData(modbusRtu.getTenWriteData());
        ModbusCode modbusCode = getInstance(modbusRtu.getCode());
        switch (modbusCode) {
            case Read01:
            case Read02:
            case Read03:
            case Read04:
                // MBAP 长度默认6个字节
                modbusTcp.setBitLength(6);
                break;
            case Write05:
                // MBAP 长度默认6个字节
                modbusTcp.setBitLength(6);
                modbusTcp.setWriteData(1 == modbusTcp.getWriteData() ? 0xFF00 : 0x0000);
                break;
            case Write06:
                // MBAP 长度默认6个字节
                modbusTcp.setBitLength(6);
                break;
            case Write0F:
                // MBAP 长度默认8个字节
                modbusTcp.setBitLength(8);
                modbusTcp.setBitCount(1 + (modbusTcp.getCount() - 1) / 8);
                String reverse = StringUtils.reverse(modbusRtu.getBitString());
                modbusTcp.setBitData(BitUtils.string2bytes(reverse));
                break;
            case Write10:
                // MBAP 长度默认8个字节
                modbusTcp.setBitLength(9);
                modbusTcp.setBitCount(modbusTcp.getCount() * 2);
                break;
        }
        return modbusTcp;
    }

    public ByteBuf encode(ModbusTcp message) {
        this.build();
        /*下发读指令*/
        int version = 1;
        /*下发写指令*/
        if (message.getCode() == ModbusCode.Write06.getCode()
                || message.getCode() == ModbusCode.Write05.getCode()) {
            version = 2;
        }else if (message.getCode() == ModbusCode.Write10.getCode()){
            version = 16;
        }else if (message.getCode() == ModbusCode.Write0F.getCode()){
            version = 15;
        }
        ByteBuf buf = ALLOC.buffer(6);
        ActiveModel activeModel = headerSchemaMap.get(version);
        activeModel.writeTo(buf, message, null);
        return buf;
    }

    private void build() {
        if (this.headerSchemaMap == null) {
            this.headerSchemaMap = this.modelManager.getActiveMap(ModbusTcp.class);
        }
    }
}
