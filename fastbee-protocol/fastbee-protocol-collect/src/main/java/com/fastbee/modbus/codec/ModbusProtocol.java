package com.fastbee.modbus.codec;

import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.annotation.SysProtocol;
import com.fastbee.common.constant.FastBeeConstant;
import com.fastbee.common.core.mq.DeviceReport;
import com.fastbee.common.core.mq.GwDeviceBo;
import com.fastbee.common.core.mq.MQSendMessageBo;
import com.fastbee.common.core.mq.SubDeviceBo;
import com.fastbee.common.core.mq.message.DeviceData;
import com.fastbee.common.core.mq.message.FunctionCallBackBo;
import com.fastbee.common.core.protocol.modbus.ModbusCode;
import com.fastbee.common.core.redis.RedisCache;
import com.fastbee.common.core.redis.RedisKeyBuilder;
import com.fastbee.common.core.thingsModel.ThingsModelSimpleItem;
import com.fastbee.common.enums.FunctionReplyStatus;
import com.fastbee.common.enums.ModbusDataType;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.CaculateUtils;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.common.utils.gateway.CRC16Utils;
import com.fastbee.common.utils.modbus.BitUtils;
import com.fastbee.common.utils.modbus.ModbusUtils;
import com.fastbee.common.utils.modbus.Mparams;
import com.fastbee.iot.cache.IModbusConfigCache;
import com.fastbee.iot.domain.ModbusConfig;
import com.fastbee.iot.model.ThingsModelItem.Datatype;
import com.fastbee.iot.model.ThingsModels.ThingsModelValueItem;
import com.fastbee.iot.model.gateWay.SubDeviceListVO;
import com.fastbee.iot.service.IDeviceService;
import com.fastbee.iot.service.ISubGatewayService;
import com.fastbee.iot.service.IThingsModelService;
import com.fastbee.modbus.model.ModbusRtu;
import com.fastbee.protocol.base.protocol.IProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author bill
 */
@Slf4j
@Component
@SysProtocol(name = "ModbusRtu协议", protocolCode = FastBeeConstant.PROTOCOL.ModbusRtu, description = "系统内置ModbusRtu解析协议")
public class ModbusProtocol implements IProtocol {


    @Resource
    private ModbusDecoder messageDecoder;
    @Resource
    private ModbusEncoder messageEncoder;
    @Resource
    private RedisCache redisCache;
    @Resource
    private IModbusConfigCache modbusConfigCache;
    @Resource
    private ISubGatewayService subGatewayService;

    @Override
    public DeviceReport decode(DeviceData deviceData, String clientId) {
        try {
            DeviceReport report = new DeviceReport();
            report.setProductId(deviceData.getProductId());
            report.setSerialNumber(deviceData.getSerialNumber());
            //原始报文字符串
            String hexDump = ByteBufUtil.hexDump(deviceData.getBuf());
            ModbusRtu message = messageDecoder.decode(deviceData);
            int slaveId = message.getSlaveId();
            //获取功能码
            int code = message.getCode();
            if (message.getmId() != 0) {
                report.setClientId(message.getMac());
                report.setMessageId(String.valueOf(message.getmId()));
                report.setSources(hexDump);
                return report;
            }
            GwDeviceBo gwDeviceBo = new GwDeviceBo();
            gwDeviceBo.setProductId(deviceData.getProductId());
            gwDeviceBo.setSerialNumber(deviceData.getSerialNumber());
            report.setGwDeviceBo(gwDeviceBo);
            matchSubDev(report, deviceData.getSerialNumber(), slaveId);
            String serialNumber = report.getSerialNumber();
            Long productId = report.getProductId();
            ModbusCode modbusCode = ModbusCode.getInstance(code);
            List<ThingsModelSimpleItem> values = new ArrayList<>();
            switch (modbusCode) {
                case Read01:
                case Read02:
                    //起始地址
                    String address0102 = getCacheModbusAddress(deviceData.getSerialNumber(),slaveId,code);
                    //读线圈或读离散型寄存器处理
                    handleRead0102(productId, message, address0102, values);
                    break;
                case Read03:
                case Read04:
                    //起始地址
                    String address0304 = getCacheModbusAddress(deviceData.getSerialNumber(),slaveId,code);
                    //读输入、保持寄存器
                    handleRead0304(productId, message, address0304, values, hexDump);
                    break;
                case Write06:
                case Write05:
                    //如果返回06编码，说明是设备回复，更新对应寄存器的值，并发送通知前端
                    report.setClientId(serialNumber);
                    report.setSerialNumber(serialNumber);
                    report.setProductId(productId);
                    report.setIsReply(true);
                    report.setSources(hexDump);
                    report.setProtocolCode(FastBeeConstant.PROTOCOL.ModbusRtu);
                    report.setStatus(FunctionReplyStatus.SUCCESS);
                    this.handleMsgReply(message,report,modbusCode);
                    return report;
            }

            report.setSerialNumber(serialNumber);
            report.setClientId(serialNumber);
            report.setProductId(productId);
            report.setThingsModelSimpleItem(values);
            report.setSources(hexDump);
            return report;
        } catch (Exception e) {
            log.error("=>解码异常[{}]", e, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 读线圈或读离散型寄存器处理
     *
     * @param productId      产品id
     * @param message        数据集合
     * @param address        起始地址
     * @param simpleItemList 返回的物模型值
     */
    private void handleRead0102(Long productId, ModbusRtu message, String address, List<ThingsModelSimpleItem> simpleItemList) {
        //字节数
        int bitCount = message.getBitCount();
        //字节数据集合
        byte[] byteDatas = message.getBitData();
        String hexDump = ByteBufUtil.hexDump(byteDatas);
        //处理多个的情况
        Map<Integer, List<ModbusConfig>> modbusConfigMap = modbusConfigCache.getModbusConfigCacheByProductId(productId);
        if (CollectionUtils.isEmpty(modbusConfigMap)) {
            log.warn("寄存器地址：{},不存在", address);
            return;
        }
        List<ModbusConfig> modbusConfigList = new ArrayList<>();
        for (Map.Entry<Integer, List<ModbusConfig>> entry : modbusConfigMap.entrySet()) {
            modbusConfigList.addAll(entry.getValue());
        }
        int isReadOnly = message.getCode() == 1 ? 1 : 0;
        Map<Integer, List<ModbusConfig>> listMap = modbusConfigList.stream().filter(config ->
        { return config.getType() == 1 && config.getIsReadonly() == isReadOnly;}).collect(Collectors.groupingBy(ModbusConfig::getAddress));
        //分割值
        List<String> values = StringUtils.splitEvenly(hexDump, 2);
        int ioAd = Integer.parseInt(address);
        //匹配寄存器,一个字节有8个位需要处理
        for (int i = 0; i < bitCount * 8; i++) {
            List<ModbusConfig> configList = listMap.get(ioAd);
            if (!CollectionUtils.isEmpty(configList) && configList.size() == 1) {
                ModbusConfig modbusConfig = configList.get(0);
                String hex = values.get(i / 8);
                int result = BitUtils.deterHex(hex, i % 8);
                ThingsModelSimpleItem simpleItem = new ThingsModelSimpleItem();
                simpleItem.setId(modbusConfig.getIdentifier());
                simpleItem.setValue(result + "");
                simpleItem.setTs(DateUtils.getNowDate());
                simpleItemList.add(simpleItem);
            }
            ioAd += 1;
        }
    }

    /**
     * 处理读保持寄存器/输入寄存器
     *
     * @param productId      产品id
     * @param message        数据
     * @param address        起始地址
     * @param simpleItemList 返回的物模型值
     * @param sourceStr      原始报文字符串
     */
    private void handleRead0304(Long productId, ModbusRtu message, String address, List<ThingsModelSimpleItem> simpleItemList, String sourceStr) {
        short[] data = message.getData();
        int length = data.length;
        if (length == 1) {
            //单个寄存器上报情况处理
            List<ModbusConfig> modbusConfig = modbusConfigCache.getSingleModbusConfig(productId, address);
            if (CollectionUtils.isEmpty(modbusConfig)) {
                log.warn("寄存器地址：{},不存在", address);
                return;
            }
            Map<Integer, List<ModbusConfig>> listMap = modbusConfig.stream().collect(Collectors.groupingBy(ModbusConfig::getType));
            //处理IO类型
            List<ModbusConfig> IOConfigList = listMap.get(1);
            if (!CollectionUtils.isEmpty(IOConfigList)) {
                if (IOConfigList.size() > 1) {
                    //03按位运行情况，读寄存器需要将16进制转换为2进制，按位取值
                    // 如1-4个继电器开关情况，寄存器值是0x0007 从右到左，1-4 对应 on-on-on-off
                    for (ModbusConfig config : IOConfigList) {
                        int result = BitUtils.deter(data[0], config.getBitOrder());
                        ThingsModelSimpleItem simpleItem = new ThingsModelSimpleItem();
                        simpleItem.setId(config.getIdentifier());
                        simpleItem.setValue(result + "");
                        simpleItem.setTs(DateUtils.getNowDate());
                        simpleItemList.add(simpleItem);
                    }
                }
            }
            //单个寄存器值
            List<ModbusConfig> dataConfigList = listMap.get(2);
            if (!CollectionUtils.isEmpty(dataConfigList)) {
                ModbusConfig config = dataConfigList.get(0);
                //普通取值，应该只有一个数据，将identity与address替换
                ThingsModelSimpleItem simpleItem = new ThingsModelSimpleItem();
                simpleItem.setId(config.getIdentifier());
                simpleItem.setValue(CaculateUtils.handleToUnSign16(data[0] + "", config.getDataType()));
                simpleItem.setTs(DateUtils.getNowDate());
                simpleItemList.add(simpleItem);
            }
        } else {
            //原数据字符串
            String dataHex = sourceStr.substring(6, sourceStr.length() - 4);
            //处理多个情况
            Map<Integer, List<ModbusConfig>> modbusConfigMap = modbusConfigCache.getModbusConfigCacheByProductId(productId);
            if (CollectionUtils.isEmpty(modbusConfigMap)) {
                log.warn("寄存器数据不存在");
                return;
            }
            List<ModbusConfig> modbusConfigList = new ArrayList<>();
            for (Map.Entry<Integer, List<ModbusConfig>> entry : modbusConfigMap.entrySet()) {
                modbusConfigList.addAll(entry.getValue());
            }
            int isReadOnly = message.getCode() == 3 ? 1 : 0;
            Map<Integer, ModbusConfig> configMap = modbusConfigList.stream().filter(config ->
            {return config.getType() == 2 && config.getIsReadonly() == isReadOnly;}).collect(Collectors.toMap(ModbusConfig::getAddress, Function.identity()));
            int registerAd = Integer.parseInt(address);
            for (int i = 0; i < length; i++) {
                ModbusConfig modbusConfig = configMap.get(registerAd);
                if (Objects.isNull(modbusConfig)) {
                    //处理可能是 03按位运行情况,判断是否有寄存器，而且寄存器对应物模型多个
                    List<ModbusConfig> dataToIoList = modbusConfigMap.get(registerAd);
                    if (!CollectionUtils.isEmpty(dataToIoList) && dataToIoList.size() > 1) {
                        for (ModbusConfig config : dataToIoList) {
                            int result = BitUtils.deter(data[i], config.getBitOrder());
                            ThingsModelSimpleItem simpleItem = new ThingsModelSimpleItem();
                            simpleItem.setId(config.getIdentifier());
                            simpleItem.setValue(result + "");
                            simpleItem.setTs(DateUtils.getNowDate());
                            simpleItemList.add(simpleItem);
                        }
                    } else {
                        log.warn("寄存器地址：{},不存在", registerAd);
                    }
                    registerAd += 1;
                    continue;
                }
                //个数
                Integer quantity = modbusConfig.getQuantity();
                ThingsModelSimpleItem simpleItem = new ThingsModelSimpleItem();
                simpleItem.setId(modbusConfig.getIdentifier());
                simpleItem.setTs(DateUtils.getNowDate());
                if (quantity == 1) {
                    //将identity与address替换
                    simpleItem.setValue(CaculateUtils.handleToUnSign16(data[i] + "", modbusConfig.getDataType()));
                    simpleItemList.add(simpleItem);
                    dataHex = dataHex.substring(4);
                } else if (quantity == 2) {
                    //这里做数据处理
                    String handleHex = dataHex.substring(0, 8);
                    String value = CaculateUtils.parseValue(modbusConfig.getDataType(), handleHex);
                    simpleItem.setValue(value);
                    simpleItemList.add(simpleItem);
                    //处理dataHex
                    dataHex = dataHex.substring(8);
                    //处理quantity ==2的情况，跳过一次循环
                    i += 1;
                }
                //寄存器地址+1
                registerAd += 1;
            }
        }
    }

    /**
     * 处理下发指令设备回复消息，按照modbus协议约定会返回一样的指令
     * @param message 解析后数据
     */
    private void handleMsgReply(ModbusRtu message,DeviceReport report,ModbusCode code){
        //单个寄存器上报情况处理
        String address = message.getAddress() + "";
        List<ModbusConfig> modbusConfig = modbusConfigCache.getSingleModbusConfig(report.getProductId(), address);
        if (CollectionUtils.isEmpty(modbusConfig)) {
            log.warn("寄存器地址：{},不存在", address);
            return;
        }
        Map<Integer, List<ModbusConfig>> listMap = modbusConfig.stream().collect(Collectors.groupingBy(ModbusConfig::getType));
        // 1是IO类型,2是寄存器类型
        int type = code.equals(ModbusCode.Write05) ? 1 : 2;
        List<ModbusConfig> modbusConfigList = listMap.get(type);
        ModbusConfig config = modbusConfigList.get(0);
        List<ThingsModelSimpleItem> values = new ArrayList<>();
        ThingsModelSimpleItem simpleItem = new ThingsModelSimpleItem();
        simpleItem.setId(config.getIdentifier());
        simpleItem.setTs(DateUtils.getNowDate());
        int data = message.getWriteData();
        if (type == 1){
            //对于IO类型的数据做一个数据转换 0x0000 是关 0xff00 是开 对应值 1
            if (data == 0xff00){
                data = 1;
            }
        }
        simpleItem.setValue(data +"");
        values.add(simpleItem);
        report.setThingsModelSimpleItem(values);
    }

    /**
     * 匹配子设备编号
     */
    private void matchSubDev(DeviceReport report, String serialNumber, Integer slaveId) {
        List<SubDeviceBo> subDeviceList = subGatewayService.getSubDeviceListByGw(serialNumber);
        if (!CollectionUtils.isEmpty(subDeviceList)) {
            for (SubDeviceBo vo : subDeviceList) {
                if (vo.getSlaveId().equals(slaveId)) {
                    report.setSubDeviceBo(vo);
                    report.setSerialNumber(vo.getSubDeviceNo());
                    report.setProductId(vo.getSubProductId());
                    break;
                }
            }
        }
    }

    @Override
    public FunctionCallBackBo encode(MQSendMessageBo message) {
        FunctionCallBackBo callBack = new FunctionCallBackBo();
        ModbusRtu rtu = new ModbusRtu();
        String thingsModel = message.getThingsModel();
        ThingsModelValueItem item = JSONObject.parseObject(thingsModel, ThingsModelValueItem.class);
        ModbusConfig config = item.getConfig();
        switch (config.getModbusCode()) {
            case Read01:
            case Read02:
            case Read03:
            case Read04:
                this.read03(config, rtu);
                break;
            case Write05:
                write05(config,message.getValue(), rtu);
                break;
            case Write06:
                write06(config, message.getValue(), rtu);
                break;
        }
        ByteBuf out = messageEncoder.encode(rtu);
        byte[] data = new byte[out.writerIndex()];
        out.readBytes(data);
        ReferenceCountUtil.release(out);
        byte[] result = CRC16Utils.AddCRC(data);
        callBack.setMessage(result);
        callBack.setSources(ByteBufUtil.hexDump(result));
        return callBack;
    }

    /**
     * read03指令
     */
    private void read03(ModbusConfig modbusConfig, ModbusRtu rtu) {
        rtu.setSlaveId(modbusConfig.getSlave());
        rtu.setCount(modbusConfig.getQuantity());
        rtu.setAddress(modbusConfig.getAddress());
        rtu.setCode(modbusConfig.getModbusCode().getCode());
    }

    /**
     * writ05/06指令配置
     */
    private void write05(ModbusConfig modbusConfig, String value, ModbusRtu rtu) {
        int data;
        if (value.contains("0x")) {
            data = Integer.parseInt(value.substring(2), 16);
        } else {
            data = Integer.parseInt(value);
        }
        rtu.setWriteData(data == 1 ? 0xFF00 : 0x0000);
        rtu.setAddress(modbusConfig.getAddress());
        rtu.setCode(modbusConfig.getModbusCode().getCode());
        rtu.setSlaveId(modbusConfig.getSlave());
    }

    /**
     * writ05/06指令配置
     */
    private void write06(ModbusConfig modbusConfig, String value, ModbusRtu rtu) {
        rtu.setWriteData(Integer.parseInt(value));
        rtu.setAddress(modbusConfig.getAddress());
        rtu.setCode(modbusConfig.getModbusCode().getCode());
        rtu.setSlaveId(modbusConfig.getSlave());
    }

    /**
     * 获取modbus地址
     */
    private String getCacheModbusAddress(String serialNumber,int salveId,int code) {
        String key = RedisKeyBuilder.buildModbusRuntimeCacheKey(serialNumber);
        Set<String> commandSet = redisCache.zRange(key, 0, -1);
        if (commandSet.size() == 0) {
            throw new ServiceException("No cache modbus address found");
        }
        for (String command : commandSet) {
            Mparams params = ModbusUtils.getModbusParams(command);
            redisCache.zRem(key, command);
            if (params.getSlaveId() == salveId && params.getCode() == code){
                return params.getAddress()+"";
            }
        }
        throw new ServiceException("No cache modbus address found");
    }

}
