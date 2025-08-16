package com.fastbee.flowdev.test;

import com.fastbee.common.core.mq.message.DeviceData;
import com.fastbee.flowdev.codec.FlowDevDecoder2;
import com.fastbee.flowdev.codec.FlowDevEncoder2;
import com.fastbee.flowdev.model.FlowDev2;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

/**
 * @author bill
 */
public class FlowDevCodeTest {

    private static FlowDevDecoder2 decoder = new FlowDevDecoder2("com.fastbee");
    private static FlowDevEncoder2 encoder = new FlowDevEncoder2("com.fastbee");


    public static void main(String[] args) {
        //681C68B32201840100C00300000000010000000061000000000014110723001F16
        //681B68B33701120008C100000000000000000000022050004341231811215716
        String flowData = "681C68B32201840100C00300000000010000000061000000000014110723001F16";
        ByteBuf in = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump(flowData));
        DeviceData data = DeviceData.builder()
                .buf(in).build();
        FlowDev2 flowDev = decoder.decode(data, null);
        System.out.println(flowDev);

    }
}
