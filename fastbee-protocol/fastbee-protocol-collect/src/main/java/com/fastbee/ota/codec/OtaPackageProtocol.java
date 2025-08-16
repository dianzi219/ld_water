package com.fastbee.ota.codec;

import com.fastbee.common.annotation.SysProtocol;
import com.fastbee.common.constant.FastBeeConstant;
import com.fastbee.common.core.mq.DeviceReport;
import com.fastbee.common.core.mq.MQSendMessageBo;
import com.fastbee.common.core.mq.message.DeviceData;
import com.fastbee.common.core.mq.message.FunctionCallBackBo;
import com.fastbee.common.core.ota.OtaPackageCode;
import com.fastbee.ota.model.OtaCheckUtils;
import com.fastbee.ota.model.OtaPackage;

import com.fastbee.protocol.base.protocol.IProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 包装过的modbus-rtu协议
 *
 * @author gsb
 * @date 2022/11/15 11:16
 */
@Slf4j
@Component
@SysProtocol(name = "OTA升级协议", protocolCode = FastBeeConstant.PROTOCOL.NetOTA, description = "OTA升级协议")
public class OtaPackageProtocol implements IProtocol {

    @Resource
    private OtaPackageDecoder netModbusDecoder;
    @Resource
    private OtaPackageEncoder netModbusEncoder;

    @Override
    public DeviceData decode(String message) {
        ByteBuf buf = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump(message));
        OtaPackage netModbusRtu = netModbusDecoder.decode(buf);
        buf.release();
        DeviceData deviceData = DeviceData.builder().build();
        deviceData.setNetModbusCode(OtaPackageCode.getInstance(netModbusRtu.getCode()));
        deviceData.setData(netModbusRtu.getData());
        return deviceData;
    }

    @Override
    public byte[] encode(DeviceData data) {
        OtaPackage otaPackage = new OtaPackage();
        otaPackage.setCode(data.getNetModbusCode().getCode());
        otaPackage.setBitCount(data.getBitCount());
        otaPackage.setData(data.getData());
        ByteBuf out = netModbusEncoder.encode(otaPackage);
        byte[] result = new byte[out.writerIndex()];
        out.readBytes(result);
        ReferenceCountUtil.release(out);
        return OtaCheckUtils.addSumCheck(result);
    }

    @Override
    public DeviceReport decode(DeviceData deviceData, String clientId) {
        return null;
    }

    @Override
    public FunctionCallBackBo encode(MQSendMessageBo bo) {
        return null;
    }

}
