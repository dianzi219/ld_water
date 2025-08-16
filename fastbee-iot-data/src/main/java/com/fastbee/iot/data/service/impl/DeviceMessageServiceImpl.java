package com.fastbee.iot.data.service.impl;

import com.alibaba.fastjson2.JSON;
import com.fastbee.common.constant.FastBeeConstant;
import com.fastbee.common.core.device.DeviceAndProtocol;
import com.fastbee.common.core.mq.DeviceReportBo;
import com.fastbee.common.core.mq.MQSendMessageBo;
import com.fastbee.common.core.mq.message.DeviceMessage;
import com.fastbee.common.core.mq.message.FunctionCallBackBo;
import com.fastbee.common.core.mq.message.ModbusPollMsg;
import com.fastbee.common.core.protocol.modbus.ModbusCode;
import com.fastbee.common.core.redis.RedisCache;
import com.fastbee.common.core.thingsModel.ThingsModelSimpleItem;
import com.fastbee.common.enums.DeviceStatus;
import com.fastbee.common.enums.ServerType;
import com.fastbee.common.enums.TopicType;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.BitUtils;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.common.utils.gateway.CRC16Utils;
import com.fastbee.common.utils.gateway.mq.TopicsUtils;
import com.fastbee.common.utils.modbus.ModbusUtils;
import com.fastbee.iot.cache.ITSLCache;
import com.fastbee.iot.data.service.IDeviceMessageService;
import com.fastbee.iot.domain.ModbusConfig;
import com.fastbee.iot.domain.ModbusJob;
import com.fastbee.iot.enums.DeviceType;
import com.fastbee.iot.model.DeviceStatusVO;
import com.fastbee.iot.model.ThingsModels.ThingsModelValueItem;
import com.fastbee.iot.model.VariableReadVO;
import com.fastbee.iot.model.modbus.ModbusPollJob;
import com.fastbee.iot.service.IDeviceService;
import com.fastbee.iot.service.IModbusJobService;
import com.fastbee.modbus.codec.ModbusEncoder;
import com.fastbee.modbus.codec.ModbusProtocol;
import com.fastbee.modbus.model.ModbusRtu;
import com.fastbee.modbustcp.codec.ModbusTcpEncoder;
import com.fastbee.modbustcp.codec.ModbusTcpProtocol;
import com.fastbee.modbustcp.model.ModbusTcp;
import com.fastbee.mq.producer.MessageProducer;
import com.fastbee.mqttclient.PubMqttClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DeviceMessageServiceImpl implements IDeviceMessageService {

    @Resource
    private PubMqttClient mqttClient;
    @Resource
    private ModbusEncoder modbusMessageEncoder;
    @Resource
    private IDeviceService deviceService;
    @Resource
    private TopicsUtils topicsUtils;
    @Resource
    private ITSLCache itslCache;
    @Resource
    private ModbusProtocol modbusProtocol;
    @Resource
    private IModbusJobService modbusJobService;
    @Resource
    private ModbusTcpProtocol modbusTcpProtocol;
    @Resource
    private ModbusTcpEncoder modbusTcpEncoder;
    @Resource
    private RedisCache redisCache;
    @Resource
    private MessageManager messageManager;

    @Override
    public void messagePost(DeviceMessage deviceMessage) {
        String topicName = deviceMessage.getTopicName();
        String serialNumber = deviceMessage.getSerialNumber();
        DeviceAndProtocol deviceAndProtocol = deviceService.selectProtocolBySerialNumber(serialNumber);
        String transport = deviceAndProtocol.getTransport();
        TopicType type = TopicType.getType(topicName);
        topicName = topicsUtils.buildTopic(deviceAndProtocol.getProductId(), serialNumber,type);
        switch (type){
            case FUNCTION_GET:
                if (transport.equals(ServerType.MQTT.getCode())){
                    mqttClient.publish(0, false, topicName, deviceMessage.getMessage().toString());
                }else if (transport.equals(ServerType.TCP.getCode())){
                    //处理TCP下发
                    ModbusPollMsg modbusPollMsg = new ModbusPollMsg();
                    modbusPollMsg.setSerialNumber(serialNumber);
                    modbusPollMsg.setProductId(deviceAndProtocol.getProductId());
                    List<String> commandList = new ArrayList<>();
                    commandList.add(deviceMessage.getMessage().toString());
                    modbusPollMsg.setCommandList(commandList);
                    DeviceStatusVO deviceStatusVO = deviceService.selectDeviceStatusAndTransportStatus(modbusPollMsg.getSerialNumber());
                    modbusPollMsg.setTransport(deviceStatusVO.getTransport());
                    if (deviceStatusVO.getStatus() != DeviceStatus.ONLINE.getType()){
                        log.info("设备：[{}],不在线",modbusPollMsg.getSerialNumber());
                        return;
                    }
                    ModbusPollJob modbusPollJob = new ModbusPollJob();
                    modbusPollJob.setPollMsg(modbusPollMsg);
                    modbusPollJob.setType(1);
                    MessageProducer.sendPropFetch(modbusPollJob);
                }
                break;
            case PROPERTY_POST:
                //下发的不经过mqtt或TCP直接转发到数据处理模块
                DeviceReportBo reportBo = DeviceReportBo.builder()
                        .serverType(ServerType.explain(transport))
                        .data(BitUtils.hexStringToByteArray(deviceMessage.getMessage().toString()))
                        .platformDate(DateUtils.getNowDate())
                        .serialNumber(serialNumber)
                        .topicName(topicName).build();
                MessageProducer.sendPublishMsg(reportBo);
                break;
        }
    }

    @Override
    public String messageEncode(ModbusRtu modbusRtu) {
        ByteBuf out;
        String modbusTcpId = "";
        if (FastBeeConstant.PROTOCOL.ModbusTcp.equals(modbusRtu.getProtocolCode())) {
            ModbusTcp modbusTcp = modbusTcpEncoder.change(modbusRtu);
            modbusTcpId = redisCache.getCacheModbusTcpId(modbusTcp.getSerialNumber());
            modbusTcp.setTransactionIdentifier(Integer.parseInt(modbusTcpId));
            out = modbusTcpEncoder.encode(modbusTcp);
        } else {
            //兼容15、16功能码
            if (modbusRtu.getCode() == ModbusCode.Write10.getCode()){
                //计算:字节数=2*N；N为寄存器个数
                modbusRtu.setBitCount(2 * modbusRtu.getCount());
            }else if (modbusRtu.getCode() == ModbusCode.Write0F.getCode()){
                //计算:字节数=N/8 余数为0是 N需要再+1。N是线圈个数
                int i = modbusRtu.getCount() / 8;
                if (modbusRtu.getCount() % 8!= 0){
                    i++;
                }
                modbusRtu.setBitCount(i);
                //计算线圈值，前端返回二进制的字符串，需要将高低位先翻转，在转化为16进制
                String reverse = StringUtils.reverse(modbusRtu.getBitString());
                modbusRtu.setBitData(BitUtils.string2bytes(reverse));
            }
            out = modbusMessageEncoder.encode(modbusRtu);
        }
        byte[] result = new byte[out.writerIndex()];
        out.readBytes(result);
        ReferenceCountUtil.release(out);
        if (FastBeeConstant.PROTOCOL.ModbusTcp.equals(modbusRtu.getProtocolCode()) && StringUtils.isNotEmpty(modbusTcpId)) {
            String s = ByteBufUtil.hexDump(result);
            redisCache.cacheModbusTcpData(modbusRtu.getSerialNumber(), modbusTcpId, s);
            return s;
        }
        return ByteBufUtil.hexDump(CRC16Utils.AddCRC(result));
    }

    @Override
    public List<ThingsModelSimpleItem> messageDecode(DeviceMessage deviceMessage) {
        // ProductCode productCode = productService.getProtocolBySerialNumber(deviceMessage.getSerialNumber());
        // ByteBuf buf = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump(deviceMessage.getMessage()));
        // DeviceData deviceData = DeviceData.builder()
        //         .buf(buf)
        //         .productId(productCode.getProductId())
        //         .serialNumber(productCode.getSerialNumber())
        //         .data(ByteBufUtil.getBytes(buf))
        //         .build();
        // return modbusRtuPakProtocol.decodeMessage(deviceData, deviceMessage.getSerialNumber());
        return null;
    }

    /**
     * 变量读取
     * @param readVO
     */
    @Override
    public void readVariableValue(VariableReadVO readVO){
        String serialNumber = readVO.getSerialNumber();
        assert !Objects.isNull(serialNumber) : "设备编号为空";
        DeviceAndProtocol deviceAndProtocol = deviceService.selectProtocolBySerialNumber(serialNumber);
        if (!deviceAndProtocol.getProtocolCode().equals(FastBeeConstant.PROTOCOL.ModbusRtu)) throw new ServiceException("非modbus协议请先配置主动采集协议");
        Long productId = deviceAndProtocol.getProductId();
        //如果是子设备，则获取网关子设备的产品id和设备编号
        Integer deviceType = deviceAndProtocol.getDeviceType();
        if (deviceType == DeviceType.SUB_GATEWAY.getCode()){
            serialNumber = deviceAndProtocol.getGwSerialNumber();
            productId = deviceAndProtocol.getGwProductId();
        }
        Integer type = readVO.getType();
        type = Objects.isNull(type) ? 1 : type;
        if (type  == 1){
            //单个变量获取
            String identifier = readVO.getIdentifier();
            ThingsModelValueItem thingModels = itslCache.getSingleThingModels(deviceAndProtocol.getProductId(), identifier);
            ModbusConfig config = thingModels.getConfig();
            if (Objects.isNull(config)) throw new ServiceException("未配置modbus点位");
            thingModels.getConfig().setModbusCode(ModbusUtils.getReadModbusCode(config.getType(),config.getIsReadonly()));
            MQSendMessageBo messageBo = new MQSendMessageBo();
            messageBo.setThingsModel(JSON.toJSONString(thingModels));
            messageBo.setSerialNumber(readVO.getSerialNumber());
            messageBo.setParentSerialNumber(readVO.getParentSerialNumber());
            FunctionCallBackBo encode;
            if (ServerType.TCP.getCode().equals(deviceAndProtocol.getTransport())) {
                encode = modbusTcpProtocol.encode(messageBo);
            } else {
                encode = modbusProtocol.encode(messageBo);
            }
            List<String> commandList = new ArrayList<>();
            commandList.add(encode.getSources());
            ModbusPollMsg modbusPollMsg = new ModbusPollMsg();
            modbusPollMsg.setSerialNumber(serialNumber);
            modbusPollMsg.setProductId(productId);
            modbusPollMsg.setCommandList(commandList);
            DeviceStatusVO deviceStatusVO = deviceService.selectDeviceStatusAndTransportStatus(modbusPollMsg.getSerialNumber());
            modbusPollMsg.setTransport(deviceStatusVO.getTransport());
            if (deviceStatusVO.getStatus() != DeviceStatus.ONLINE.getType()){
                log.info("设备：[{}],不在线",modbusPollMsg.getSerialNumber());
                return;
            }
            ModbusPollJob job = new ModbusPollJob();
            job.setPollMsg(modbusPollMsg);
            job.setType(1);
            MessageProducer.sendPropFetch(job);
        }else {
            //读取当前设备的所有变量,这里读取所有的，判断传递的设备是网关设备、网关子设备、直连设备
            DeviceType code = DeviceType.transfer(deviceType);
            List<ModbusJob> modbusJobList = modbusJobService.selectDevicesJobByDeviceType(code, readVO.getSerialNumber());
            List<String> commandList = modbusJobList.stream().map(ModbusJob::getCommand).collect(Collectors.toList());
            ModbusPollMsg msg = new ModbusPollMsg();
            msg.setCommandList(commandList);
            msg.setTransport(deviceAndProtocol.getTransport());
            msg.setProductId(productId);
            msg.setSerialNumber(serialNumber);
            ModbusPollJob job = new ModbusPollJob();
            job.setPollMsg(msg);
            job.setType(1);
            MessageProducer.sendPropFetch(job);
        }
    }

}
