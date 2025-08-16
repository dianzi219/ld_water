package com.fastbee.modbus.test;

import com.fastbee.common.core.mq.message.DeviceData;
import com.fastbee.common.core.protocol.modbus.ModbusCode;
import com.fastbee.common.utils.CaculateUtils;
import com.fastbee.common.utils.gateway.protocol.ByteUtils;
import com.fastbee.modbus.codec.ModbusDecoder;
import com.fastbee.modbus.codec.ModbusEncoder;
import com.fastbee.modbus.model.ModbusRtu;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;

/**
 * 协议解析本地测试
 * @author bill
 */
@Slf4j
public class ModbusTest {

    private static ModbusDecoder decoder = new ModbusDecoder("com.fastbee.modbus");
    private static ModbusEncoder encoder = new ModbusEncoder("com.fastbee.modbus");

    public static void main(String[] args) throws Exception {
        /*单个03寄存器数据解析*/
        String hex = "0103020016398a";
        ByteBuf buf = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump(hex));
        DeviceData data = DeviceData.builder()
                .buf(buf)
                .code(ModbusCode.Read03)
                .build();
        ModbusRtu message = decoder.decode(data);
        System.out.println("单个解析="+message);
        buf.release();
        /*批量读返回 03数据解析*/
        String hexMore = "01031000000000000000000000000000000000E459";
        ByteBuf bufMore = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump(hexMore));
        DeviceData dataMore = DeviceData.builder()
                .buf(bufMore).build();
        ModbusRtu messageMore = decoder.decode(dataMore);
        System.out.println("批量上传报文解析="+messageMore);
        bufMore.release();

        /*01、02解析*/
        String hex01 = "110105CD6BB20E1B45E6";
        ByteBuf buf01 = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump(hex01));
        DeviceData data01 = DeviceData.builder()
               .buf(buf01)
               .code(ModbusCode.Read01)
               .build();
        ModbusRtu message01 = decoder.decode(data01);
        log.info("01、02解析=> 字节数:[{}],hex值:[{}]",message01.getBitCount(),ByteBufUtil.hexDump(message01.getBitData()));
        buf01.release();


        ModbusRtu read03 = new ModbusRtu();
        read03.setSlaveId(1);
        read03.setAddress(1);
        read03.setCode(3);
        read03.setCount(3);
        ByteBuf out = encoder.encode(read03);
        System.out.println(ByteBufUtil.hexDump(out));

        ModbusRtu write06 = new ModbusRtu();
        write06.setSlaveId(1);
        write06.setAddress(1);
        write06.setCode(6);
        write06.setWriteData(17);
        ByteBuf writeOut = encoder.encode(write06);
        System.out.println(ByteBufUtil.hexDump(writeOut));


        String s = Integer.toHexString(Integer.parseInt("16878"));
        String s1 = Integer.toHexString(16878);
        System.out.println(s);
        String hexToBytes = "41EE8000";
        byte[] bytes = ByteBufUtil.decodeHexDump(hexToBytes);
        String v = CaculateUtils.toFloat(bytes);
        System.out.println(v);

        System.out.println(ByteUtils.intToHex(-32768));
    }
}
