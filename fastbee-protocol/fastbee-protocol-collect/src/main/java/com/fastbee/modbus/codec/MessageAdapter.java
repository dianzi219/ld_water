package com.fastbee.modbus.codec;

import com.fastbee.common.ProtocolColl;
import com.fastbee.common.constant.FastBeeConstant;
import com.fastbee.common.core.mq.DeviceReport;
import com.fastbee.common.core.mq.message.DeviceData;
import com.fastbee.common.core.protocol.Message;
import com.fastbee.common.enums.ServerType;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.iot.model.ProductCode;
import com.fastbee.iot.service.IDeviceService;
import com.fastbee.protocol.base.protocol.IProtocol;
import com.fastbee.protocol.service.IProtocolManagerService;
import com.fastbee.base.codec.MessageDecoder;
import com.fastbee.base.codec.MessageEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 消息编解码适配器
 *
 * @author bill
 */
@Slf4j
@Component
public class MessageAdapter implements MessageDecoder, MessageEncoder{


    @Autowired
    private IProtocolManagerService managerService;
    @Resource
    private IDeviceService deviceService;

    /**
     * modbus消息解码
     *
     * @param buf     原数据
     * @param  clientId 客户端id
     * @return 解析后bean
     */
    @Override
    public DeviceReport decode(ByteBuf buf, String clientId) {
        IProtocol protocol = null;
        Long productId = null;
        String devNum = new String(ByteBufUtil.getBytes(buf));
        String dump = ByteBufUtil.hexDump(buf);
        log.info("=>上报hex数据:{}", dump);

        //这里兼容一下TCP整包发送的数据(整包==设备编号，数据一起发送)
        if (clientId == null){
            if (StringUtils.isNotEmpty(dump)) {
                assert dump != null;
                if (dump.startsWith("68") && dump.endsWith("16")) {
                    //protocol = managerService.getProtocolByProtocolCode(FastBeeConstant.PROTOCOL.FlowMeter);
                    protocol = managerService.getProtocolByProtocolCode(FastBeeConstant.PROTOCOL.SL247);
                }else if (dump.startsWith("7e")&& dump.endsWith("7e")){
                    protocol = managerService.getProtocolByProtocolCode(FastBeeConstant.PROTOCOL.ModbusRtu);
                }else if (devNum.startsWith("{") && devNum.endsWith("}")){
                    protocol = managerService.getProtocolByProtocolCode(FastBeeConstant.PROTOCOL.SGZ);
                }else if (devNum.startsWith("[") && devNum.endsWith("]")){
                    protocol = managerService.getProtocolByProtocolCode(FastBeeConstant.PROTOCOL.JsonArray);
                }else if (dump.startsWith("fedc")){
                    protocol = managerService.getProtocolByProtocolCode(FastBeeConstant.PROTOCOL.CH);
                }
            }

        }else {
            ProtocolColl coll = selectedProtocol(clientId);
            protocol = coll.getProtocol();
            productId = coll.getProductId();
        }
        DeviceData deviceData = DeviceData.builder()
                .buf(buf)
                .productId(productId)
                .serialNumber(clientId)
                .data(ByteBufUtil.getBytes(buf))
                .build();
        return protocol.decode(deviceData, clientId);
    }


    /**
     * modbus消息编码
     *
     * @param message modbusbean
     * @return 编码指令
     */
    @Override
    public ByteBuf encode(Message message, String clientId) {
        String serialNumber = StringUtils.isNotEmpty(clientId) ? clientId : message.getClientId();
        ProtocolColl protocol = selectedProtocol(serialNumber);
        IProtocol instance = protocol.getProtocol();
        DeviceData data = DeviceData.builder().body(message).build();
        byte[] out = instance.encodeCallBack(data);
        log.info("应答设备,clientId=[{}],指令=[{}]", serialNumber, ByteBufUtil.hexDump(out));
        return Unpooled.wrappedBuffer(out);
    }

    private ProtocolColl selectedProtocol(String serialNumber) {
        ProtocolColl protocolColl = new ProtocolColl();
        try {
            ProductCode protocol = deviceService.getProtocolBySerialNumber(serialNumber);
            if (protocol == null || StringUtils.isEmpty(protocol.getProtocolCode())) {
                protocol.setProtocolCode(FastBeeConstant.PROTOCOL.ModbusRtu);
            }
//            } else if (ServerType.TCP.getCode().equals(protocol.getTransport()) && FastBeeConstant.PROTOCOL.ModbusRtu.equals(protocol.getProtocolCode())) {
//                protocol.setProtocolCode(FastBeeConstant.PROTOCOL.ModbusRtuTcp);
//            }
            IProtocol code = managerService.getProtocolByProtocolCode(protocol.getProtocolCode());
            protocolColl.setProtocol(code);
            protocolColl.setProductId(protocol.getProductId());
            return protocolColl;
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    }
}
