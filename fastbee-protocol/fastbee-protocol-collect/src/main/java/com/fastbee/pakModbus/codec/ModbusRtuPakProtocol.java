package com.fastbee.pakModbus.codec;

import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.annotation.SysProtocol;
import com.fastbee.common.constant.FastBeeConstant;
import com.fastbee.common.core.mq.DeviceReport;
import com.fastbee.common.core.mq.MQSendMessageBo;
import com.fastbee.common.core.mq.message.DeviceData;
import com.fastbee.common.core.mq.message.DeviceDownMessage;
import com.fastbee.common.core.mq.message.FunctionCallBackBo;
import com.fastbee.common.core.protocol.modbus.ModbusCode;
import com.fastbee.common.core.thingsModel.ThingsModelSimpleItem;
import com.fastbee.common.core.thingsModel.ThingsModelValuesInput;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.gateway.CRC16Utils;
import com.fastbee.common.utils.modbus.BitUtils;
import com.fastbee.iot.cache.IModbusConfigCache;
import com.fastbee.iot.domain.ModbusConfig;
import com.fastbee.iot.model.ThingsModels.ThingsModelValueItem;
import com.fastbee.pakModbus.model.PakModbusRtu;
import com.fastbee.protocol.base.protocol.IProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 包装过的modbus-rtu协议
 *
 * @author gsb
 * @date 2022/11/15 11:16
 */
@Slf4j
@Component
@SysProtocol(name = "ModbusRtu扩展自定义协议", protocolCode = FastBeeConstant.PROTOCOL.ModbusRtuPak, description = "ModbusRtu扩展自定义协议")
public class ModbusRtuPakProtocol implements IProtocol {

    /**
     * 1.约定报文格式如下
     * 1.设备主动上报数据组成：
     * * 上报的指令数据
     * * FFAA 0001 010302030578B7
     * * FFAA   0001         010302030578B7
     * * 包头   起始寄存器   数据包
     * <p>
     * * 数据包
     * * 01        03      02              0305  78B7
     * * 设备地址  命令号  返回数据字节数  数据  CRC校验
     * <p>
     * 2.服务下发数据组成
     * * 下发的指令数据
     * * FFDD a69035158678888448 01 06 0101 0015 1839
     * * FFDD       a69035158678888448   0106010100151839
     * * 固定报文头   9字节消息ID    数据包
     * </p>
     * * 数据包
     * * 01         06       0101         0015    1839
     * * 设备地址   命令号   寄存器地址   数据位  CRC校验
     * <p>
     * 3.设备应答服务下发数据组成
     * * 下发的指令数据
     * * FFDD a69035158678888448 01 06 0101 0015 1839
     * * FFDD         a69035158678888448   0106010100151839
     * * 固定报文头   9字节消息ID    数据包
     * <p>
     * * 数据包
     * * 01         06       0101         0015    1839
     * * 设备地址   命令号   寄存器地址   数据位  CRC校验
     */

    private final static String FFDD = "ffdd";

    @Resource
    private ModbusRtuPakDecoder rtuPakDecoder;
    @Resource
    private ModbusRtuPakEncoder rtuPakEncoder;
    @Resource
    private IModbusConfigCache modbusConfigCache;

    @Override
    public DeviceReport decode(DeviceData deviceData, String clientId) {
        DeviceReport report = new DeviceReport();
        ByteBuf buf = deviceData.getBuf();
        String hexDump = ByteBufUtil.hexDump(buf);
        if (hexDump.startsWith(FFDD)){
            report.setIsReply(true);
            report.setMessageId(hexDump.substring(4,22));
            report.setClientId(clientId);
            report.setProtocolCode(FastBeeConstant.PROTOCOL.ModbusRtuPak);
            return report;
        }
        PakModbusRtu message = rtuPakDecoder.decode(buf);
        List<ThingsModelSimpleItem> values = new ArrayList<>();
        short[] data = message.getData();
        for (int i = 0; i < data.length; i++) {
            int address = message.getAddress() + i;
            handleModbusDataType(deviceData.getProductId(),address+"",data[i]+"",values);
        }
        report.setClientId(clientId);
        report.setThingsModelSimpleItem(values);
        report.setSources(hexDump);
        return report;
    }


    /**
     * 处理不同数据
     * @param productId
     * @param values
     */
    private void handleModbusDataType(Long productId,String address,String value,List<ThingsModelSimpleItem> values){

        int nValue = Integer.parseInt(value);
        List<ModbusConfig> modbusConfig = modbusConfigCache.getSingleModbusConfig(productId, address);
        if (CollectionUtils.isEmpty(modbusConfig)){
            log.warn("寄存器地址：{},不存在",address); return;
        }
        Map<Integer, List<ModbusConfig>> listMap = modbusConfig.stream().collect(Collectors.groupingBy(ModbusConfig::getType));
        //处理IO类型
        List<ModbusConfig> IOConfigList = listMap.get(1);
        if (!CollectionUtils.isEmpty(IOConfigList)){
            if (IOConfigList.size() > 1){
                //按位运行情况，需要将16进制转换为2进制，按位取值
                for (ModbusConfig config : IOConfigList) {
                    int result = BitUtils.deter(nValue, config.getBitOrder());
                    ThingsModelSimpleItem simpleItem = new ThingsModelSimpleItem();
                    simpleItem.setId(config.getIdentifier());
                    //simpleItem.setSlaveId(config.getSlave());
                    simpleItem.setValue(result+"");
                    simpleItem.setTs(DateUtils.getNowDate());
                    values.add(simpleItem);
                }
            }else {
                //普通IO取值，应该只有一个数据，将identity与address替换
                ThingsModelSimpleItem simpleItem = new ThingsModelSimpleItem();
                simpleItem.setId(IOConfigList.get(0).getIdentifier());
                //simpleItem.setSlaveId(IOConfigList.get(0).getSlave());
                simpleItem.setValue(value);
                simpleItem.setTs(DateUtils.getNowDate());
                values.add(simpleItem);
            }
        }
        List<ModbusConfig> dataConfigList = listMap.get(2);
        if (!CollectionUtils.isEmpty(dataConfigList)){
            //普通取值，应该只有一个数据，将identity与address替换
            ThingsModelSimpleItem simpleItem = new ThingsModelSimpleItem();
            simpleItem.setId(dataConfigList.get(0).getIdentifier());
            //simpleItem.setSlaveId(dataConfigList.get(0).getSlave());
            simpleItem.setValue(value);
            simpleItem.setTs(DateUtils.getNowDate());
            values.add(simpleItem);
        }
    }


    @Override
    public FunctionCallBackBo encode(MQSendMessageBo bo) {
        FunctionCallBackBo callBack = new FunctionCallBackBo();
        String thingsModel = bo.getThingsModel();
        ThingsModelValueItem item = JSONObject.parseObject(thingsModel, ThingsModelValueItem.class);
        ModbusConfig modbusConfig = item.getConfig();
        String value = bo.getValue();
        PakModbusRtu modbusRtu = new PakModbusRtu();
        modbusRtu.setMessageId(bo.getMessageId());
        modbusRtu.setSlaveId(modbusConfig.getSlave());
        modbusRtu.setCode(modbusConfig.getModbusCode().getCode());
        modbusRtu.setDownAdd(modbusConfig.getAddress());
        modbusRtu.setWriteData(Integer.parseInt(value));
        ByteBuf out = rtuPakEncoder.encode(modbusRtu);
        byte[] result = new byte[out.writerIndex()];
        out.readBytes(result);
        ReferenceCountUtil.release(out);
        callBack.setMessage(result);
        callBack.setSources(ByteBufUtil.hexDump(result));
        return callBack;
    }

}
