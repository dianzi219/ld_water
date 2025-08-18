package com.fastbee.slt427.codec;

import com.fastbee.slt427.model.ElecDev;
import com.fastbee.slt427.model.FlowDev;
import com.fastbee.protocol.WModelManager;
import com.fastbee.protocol.base.model.ActiveModel;
import com.fastbee.protocol.util.ArrayMap;
import com.fastbee.protocol.util.ExplainUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author gsb
 * @date 2023/5/17 16:45
 */
@Slf4j
@Component
@NoArgsConstructor
public class SLT427DevEncoder {

    private static final ByteBufAllocator ALLOC = PooledByteBufAllocator.DEFAULT;

    @Resource
    private WModelManager modelManager;
    private ArrayMap<ActiveModel> headerSchemaMap;
    private ArrayMap<ActiveModel> elecSchemaMap;

    public SLT427DevEncoder(String...basePackages){
        this.modelManager = new WModelManager(basePackages);
        this.headerSchemaMap = this.modelManager.getActiveMap(FlowDev.class);
        this.elecSchemaMap = this.modelManager.getActiveMap(ElecDev.class);
    }

    public ByteBuf encode(FlowDev message, ExplainUtils explain){
        this.build();
        ByteBuf buf = ALLOC.buffer();
        ActiveModel activeModel = headerSchemaMap.get(1);
        activeModel.writeTo(buf,message,explain);
        return buf;
    }
    
    public ByteBuf encode(ElecDev message, ExplainUtils explain){
        this.build();
        ByteBuf buf = ALLOC.buffer();
        ActiveModel activeModel = elecSchemaMap.get(1);
        activeModel.writeTo(buf,message,explain);
        return buf;
    }

    private void build() {
        if (this.headerSchemaMap == null) {
            this.headerSchemaMap = this.modelManager.getActiveMap(FlowDev.class);
        }
        if (this.elecSchemaMap == null) {
            this.elecSchemaMap = this.modelManager.getActiveMap(ElecDev.class);
        }
    }
}