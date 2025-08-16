package com.fastbee.ota.codec;

import com.fastbee.ota.model.OtaPackage;
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

@Slf4j
@Component
@NoArgsConstructor
public class OtaPackageEncoder {

    private static final ByteBufAllocator ALLOC = PooledByteBufAllocator.DEFAULT;

    @Autowired
    private WModelManager modelManager;
    private ArrayMap<ActiveModel> headerSchemaMap;

    public OtaPackageEncoder(String... basePackages) {
        this.modelManager = new WModelManager(basePackages);
        this.headerSchemaMap = this.modelManager.getActiveMap(OtaPackage.class);
    }

    public ByteBuf encode(OtaPackage otaPackage) {
        this.build();
        ByteBuf buf = ALLOC.buffer();
        ActiveModel activeModel = headerSchemaMap.get(0);
        activeModel.writeTo(buf, otaPackage, null);
        return buf;
    }

    private void build() {
        if (this.headerSchemaMap == null) {
            this.headerSchemaMap = this.modelManager.getActiveMap(OtaPackage.class);
        }
    }
}
