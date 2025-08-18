package com.fastbee.slt427.codec;

import com.fastbee.common.core.mq.message.DeviceData;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.gateway.CRC8Utils;
import com.fastbee.slt427.model.ElecDev;
import com.fastbee.slt427.model.FlowDev;
import com.fastbee.protocol.WModelManager;
import com.fastbee.protocol.base.model.ActiveModel;
import com.fastbee.protocol.util.ArrayMap;
import com.fastbee.protocol.util.ExplainUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author gsb
 * @date 2023/5/17 16:37
 */
@Slf4j
@Component
@NoArgsConstructor
public class SLT427DevDecoder {

    @Resource
    private WModelManager modelManager;
    private ArrayMap<ActiveModel> headerSchemaMap;
    private ArrayMap<ActiveModel> elecSchemaMap;

    public SLT427DevDecoder(String...basePackages){
        this.modelManager = new WModelManager(basePackages);
        this.headerSchemaMap = this.modelManager.getActiveMap(FlowDev.class);
        this.elecSchemaMap = this.modelManager.getActiveMap(ElecDev.class);
    }

    public Object decode(DeviceData deviceData, ExplainUtils explain){
        this.build();
        ByteBuf in = deviceData.getBuf();
        // verify(in); // TODO 待完善 校验错误
        
        // 根据第4字节（索引为3）判断设备类型
        byte controlByte = in.getByte(3);
        boolean isElecDev = (controlByte & 0xFF) == 0xB3; // 如果第4字节是B3，则为电量设备
        
        if (isElecDev) {
            // 使用ElecDev解析
            ActiveModel<ElecDev> activeModel = elecSchemaMap.get(0);
            ElecDev elecDev = new ElecDev();
            activeModel.mergeFrom(in, elecDev, explain);
            log.info("=>电量设备数据解析:[{}]", elecDev);
            return elecDev;
        } else {
            // 使用FlowDev解析
            ActiveModel<FlowDev> activeModel = headerSchemaMap.get(0);
            FlowDev flowDev = new FlowDev();
            activeModel.mergeFrom(in, flowDev, explain);
            log.info("=>流量计数据解析:[{}]", flowDev);
            return flowDev;
        }
    }

    /*CRC校验*/
    private void verify(ByteBuf in){
        ByteBuf copy = in.duplicate();
        byte[] source = new byte[in.writerIndex()];
        copy.readBytes(source);
        //取倒数第二位校验CRC8
        byte checkBytes = source[source.length -2];
        byte[] sourceCheck = ArrayUtils.subarray(source,3,source.length -2);
        byte crc = CRC8Utils.calcCrc8_E5(sourceCheck);
        if (crc != checkBytes){
            log.warn("=>CRC校验异常,报文={}",ByteBufUtil.hexDump(source));
            throw new ServiceException("CRC校验异常");
        }
    }

    private void build(){
        if (this.headerSchemaMap == null) {
            this.headerSchemaMap = this.modelManager.getActiveMap(FlowDev.class);
        }
        if (this.elecSchemaMap == null) {
            this.elecSchemaMap = this.modelManager.getActiveMap(ElecDev.class);
        }
    }
}