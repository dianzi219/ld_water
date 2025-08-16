package com.fastbee.pakModbus.test;

import com.fastbee.common.core.protocol.modbus.ModbusCode;
import com.fastbee.common.utils.gateway.CRC16Utils;
import com.fastbee.iot.util.SnowflakeIdWorker;
import com.fastbee.pakModbus.codec.ModbusRtuPakDecoder;
import com.fastbee.pakModbus.codec.ModbusRtuPakEncoder;
import com.fastbee.pakModbus.model.PakModbusRtu;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
//import org.checkerframework.checker.units.qual.A;

/**
 * @author bill
 */
public class PakModbusTest {

    private static ModbusRtuPakDecoder decoder = new ModbusRtuPakDecoder("com.fastbee.pakModbus");
    private static ModbusRtuPakEncoder encoder = new ModbusRtuPakEncoder("com.fastbee.pakModbus");
    private static SnowflakeIdWorker snowflakeIdWorker = new SnowflakeIdWorker(6);

    public static void main(String[] args) {

        /**
         * 单个解析
         */
        String hex = "ffaa000001030400d800dafb930d";
        ByteBuf buf = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump(hex));
        PakModbusRtu message = decoder.decode(buf);
        System.out.println("单个解析=" + message);
        buf.release();
        /**
         * 批量上报解析
         */
        String hexMore = "FFAA00010103a0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000040000000000000000cff039c00200000ffffffff0000006e0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000fa0000000000000000ffc900100019001e0010001900190057000747790D";
        ByteBuf bufMore = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump(hexMore));
        PakModbusRtu messageMore = decoder.decode(bufMore);
        System.out.println("批量上传报文解析=" + messageMore);
        bufMore.release();

        PakModbusRtu modbusRtu = new PakModbusRtu();
        modbusRtu.setMessageId(snowflakeIdWorker.nextId()+"");
        modbusRtu.setSlaveId(1);
        modbusRtu.setCode(ModbusCode.Write06.getCode());
        modbusRtu.setDownAdd(1);
        modbusRtu.setWriteData(100);
        ByteBuf out = encoder.encode(modbusRtu);
        byte[] result = new byte[out.writerIndex()];
        out.readBytes(result);
        ReferenceCountUtil.release(out);
        byte[] crc = CRC16Utils.CRC(result);
        System.out.println("->下发指令:"+ ByteBufUtil.hexDump(crc));

        // ModbusRtu read03 = new ModbusRtu();
        // read03.setSlaveId(1);
        // read03.setAddress(1);
        // read03.setCode(3);
        // read03.setCount(3);
        // ByteBuf out = encoder.encode(read03);
        // System.out.println(ByteBufUtil.hexDump(out));
        //
        // ModbusRtu write06 = new ModbusRtu();
        // write06.setSlaveId(1);
        // write06.setAddress(1);
        // write06.setCode(6);
        // write06.setWriteData(17);
        // ByteBuf writeOut = encoder.encode(write06);
        // System.out.println(ByteBufUtil.hexDump(writeOut));
    }
}
