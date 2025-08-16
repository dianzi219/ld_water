package com.fastbee.ld.test;

import com.fastbee.common.core.mq.message.DeviceData;
import com.fastbee.ld.model.FlowDev;
import com.fastbee.ld.codec.FlowDevEncoder;
import com.fastbee.ld.codec.FlowDevDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

/**
 * @author bill
 */
public class FlowDevCodeTest {

    private static FlowDevDecoder decoder = new FlowDevDecoder("com.fastbee");
    private static FlowDevEncoder encoder = new FlowDevEncoder("com.fastbee");


    public static void main(String[] args) {
        // String flowData = "681B68B33701120008C100000000000000000000022050004341231811215716";
        String flowData = "681C68B30000000001C00000000030062201000000002000005522100825005B16";
        // String flowData = "681668b80000000012c06803150000002000000013010825003216";
        // String flowData = "681c68b32201840100c00300000000010000000061000000000014110723001f16";
        // String flowData = "681C68B32201840100C00300000000010000000061000000000014110723001F16";

        int len = flowData.length();
        ByteBuf in = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump(flowData));
        DeviceData data = DeviceData.builder().buf(in).build();
        FlowDev flowDev = decoder.decode(data, null);
        System.out.println(flowDev);

    }
}
