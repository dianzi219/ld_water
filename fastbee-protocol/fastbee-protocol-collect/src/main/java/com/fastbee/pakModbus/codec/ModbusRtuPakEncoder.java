package com.fastbee.pakModbus.codec;

import com.fastbee.common.core.protocol.modbus.ModbusCode;
import com.fastbee.pakModbus.model.PakModbusRtu;
import com.fastbee.protocol.WModelManager;
import com.fastbee.protocol.base.model.ActiveModel;
import com.fastbee.protocol.util.ArrayMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author gsb
 * @date 2022/11/15 11:34
 */
@Component
@Slf4j
public class ModbusRtuPakEncoder {

    private static final ByteBufAllocator ALLOC = PooledByteBufAllocator.DEFAULT;

    @Autowired
    private WModelManager modelManager;
    private ArrayMap<ActiveModel> headerSchemaMap;

    public ModbusRtuPakEncoder(String...basePackages) {
        this.modelManager = new WModelManager(basePackages);
        this.headerSchemaMap = this.modelManager.getActiveMap(PakModbusRtu.class);
    }


    /**
     * 组装下发指令
     * @param pakModbusRtu
     * @return
     */
    public ByteBuf encode(PakModbusRtu pakModbusRtu){
        this.build();
        ByteBuf buf =  ALLOC.buffer();
        // 下发读
        int version = 1;
        if (pakModbusRtu.getCode() == ModbusCode.Write05.getCode() || pakModbusRtu.getCode() == ModbusCode.Write06.getCode()) {
            // 下发写单个
            version = 2;
        } else if (pakModbusRtu.getCode() == ModbusCode.Write10.getCode()) {
            // 下发写多个
            version = 3;
        }else if (pakModbusRtu.getCode() == ModbusCode.Write0F.getCode()){
            version = 5;
        }
        ActiveModel activeModel = headerSchemaMap.get(version);
        activeModel.writeTo(buf, pakModbusRtu, null);
        return buf;
    }

    private void build(){
        if (this.headerSchemaMap == null) {
            this.headerSchemaMap = this.modelManager.getActiveMap(PakModbusRtu.class);
        }
    }
}
