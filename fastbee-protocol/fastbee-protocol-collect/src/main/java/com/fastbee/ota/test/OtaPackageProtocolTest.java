package com.fastbee.ota.test;


import com.fastbee.ota.codec.OtaPackageDecoder;
import com.fastbee.ota.codec.OtaPackageEncoder;
import com.fastbee.ota.model.OtaCheckUtils;
import com.fastbee.ota.model.OtaPackage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 协议解析本地测试
 * @author bill
 */
@Slf4j
public class OtaPackageProtocolTest {

    private static OtaPackageDecoder decoder = new OtaPackageDecoder("com.fastbee.ota");
    private static OtaPackageEncoder encoder = new OtaPackageEncoder("com.fastbee.ota");

    public static void main(String[] args) throws Exception {

        // 解码
        String hex = "55aa007400033132330C";
        ByteBuf buf = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump(hex));
        OtaPackage message = decoder.decode(buf);
        log.debug("{}解码={}", hex, message);
        buf.release();

        // 编码
        OtaPackage ota = new OtaPackage();
        ota.setVersion(0);
        ota.setCode(1);
        ota.setBitCount(4);
        ota.setData(new byte[]{0,0,1,0});
        ByteBuf byteBuf = encoder.encode(ota);
        byte[] bytes = new byte[byteBuf.writerIndex()];
        byteBuf.readBytes(bytes);
        ReferenceCountUtil.release(byteBuf);
        byte[] result = OtaCheckUtils.addSumCheck(bytes);
        log.debug("{}编码={}", ota, ByteBufUtil.hexDump(result));
    }
}
