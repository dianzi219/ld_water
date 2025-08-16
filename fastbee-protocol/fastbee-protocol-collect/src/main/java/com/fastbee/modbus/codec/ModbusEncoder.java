package com.fastbee.modbus.codec;

import com.fastbee.modbus.model.ModbusRtu;
import com.fastbee.protocol.WModelManager;
import com.fastbee.protocol.base.model.ActiveModel;
import com.fastbee.common.core.protocol.modbus.ModbusCode;
import com.fastbee.protocol.util.ArrayMap;
import com.fastbee.protocol.util.ExplainUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * modbus-rtu协议编码器
 *
 * @author bill
 */
@Slf4j
@Component
@NoArgsConstructor
public class ModbusEncoder {

    private static final ByteBufAllocator ALLOC = PooledByteBufAllocator.DEFAULT;

    @Autowired
    private WModelManager modelManager;

    private ArrayMap<ActiveModel> headerSchemaMap;

    public ModbusEncoder(String... basePackages) {
        this.modelManager = new WModelManager(basePackages);
        this.headerSchemaMap = this.modelManager.getActiveMap(ModbusRtu.class);
    }

    public ByteBuf encode(ModbusRtu message) {
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
            this.headerSchemaMap = this.modelManager.getActiveMap(ModbusRtu.class);
        }
    }
}
