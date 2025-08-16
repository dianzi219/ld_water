package com.fastbee.ch;

import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.annotation.SysProtocol;
import com.fastbee.common.constant.FastBeeConstant;
import com.fastbee.common.core.mq.DeviceReport;
import com.fastbee.common.core.mq.MQSendMessageBo;
import com.fastbee.common.core.mq.message.DeviceData;
import com.fastbee.common.core.mq.message.FunctionCallBackBo;
import com.fastbee.common.core.thingsModel.ThingsModelSimpleItem;
import com.fastbee.common.core.thingsModel.ThingsModelValuesInput;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.protocol.base.protocol.IProtocol;
import com.fastbee.protocol.util.ByteToHexUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author gsb
 * @date 2024/5/11 16:50
 */
@Slf4j
@Component
@SysProtocol(name = "水质监测协议", protocolCode = FastBeeConstant.PROTOCOL.CH, description = "水质监测协议")
public class ChProtocolService implements IProtocol {


    @Override
    public DeviceReport decode(DeviceData data, String clientId) {
        DeviceReport reportMessage = new DeviceReport();
        ByteBuf buf = data.getBuf();
        String hexDump = ByteBufUtil.hexDump(buf);
        JSONObject params = dealMsg(hexDump);
        List<ThingsModelSimpleItem> result = new ArrayList<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            ThingsModelSimpleItem item = new ThingsModelSimpleItem();
            String val = entry.getValue() + "";
            item.setTs(DateUtils.getNowDate());
            item.setValue(val);
            item.setId(entry.getKey());
            result.add(item);
        }
        reportMessage.setThingsModelSimpleItem(result);
        reportMessage.setIsPackage(true);
        reportMessage.setClientId(clientId);
        reportMessage.setMessageId("900");
        reportMessage.setSerialNumber(clientId);
        return reportMessage;
    }

    @Override
    public FunctionCallBackBo encode(MQSendMessageBo message) {
        return new FunctionCallBackBo();
    }


    /**
     * 数据解析（适用于精讯云2.0传输协议）
     * @param msg 数据
     * @return
     */
    public static JSONObject dealMsg(String msg) {
        // 数据示例 FEDC10081609852469E90409003C000000000000165F00002516000000B40000003B0000005500000000000000C1000001C700018A00000000000000000700000008000000000000007581C1
        JSONObject data = new JSONObject();
        // 获取帧头 两字节
        String frameHeader = ByteToHexUtil.substringByte(msg, 0, 4);
        data.put("frameHeader",frameHeader);
        // 获取上传状态命令Order
        String order = ByteToHexUtil.substringByte(msg, 4, 4);
        data.put("order",order);
        // 设备id
        String imei = ByteToHexUtil.substringByte(msg, 8, 12);
        data.put("imei",imei);
        // DIN
        String din = ByteToHexUtil.substringByte(msg, 20, 4);
        data.put("din",din);
        // 数据长度
        String dataLength = ByteToHexUtil.substringByte(msg, 24, 4);
        data.put("dataLength",dataLength);
        // 有效数据
        String validData = ByteToHexUtil.substringByte(msg, 28, msg.getBytes().length - 32);
        JSONObject analyze = validData1008Analyze(validData);
        data.putAll(analyze);
        // 校验和
        String checksum = ByteToHexUtil.substringByte(msg, msg.getBytes().length - 4, 4);
        data.put("checksum",checksum);
        return data;
    }


    /**
     * 有效数据解析（1008 GPS设备上传经纬度包）
     * （适用于精讯云2.0传输协议）
     */
    public static JSONObject validData1008Analyze(String dataStr){
        /**
         * 数据顺序:
         * 一氧化碳 两位小数
         * 二氧化碳 两位小数
         * 臭氧   两位小数
         * 二氧化硫  两位小数
         * PM2.5 一位小数
         * PM10  一位小数
         * TVOC  两位小数
         * 温度   一位小数
         * 湿度   一位小数
         * 大气压  三位小数
         * 风速    一位小数
         * 风向    0位小数
         * 信号强度  两位小数
         * 错误码  整数
         * 版本号 一位小数
         */
        JSONObject data = new JSONObject();
        // 一氧化碳
        BigInteger co = new BigInteger(ByteToHexUtil.substringByte(dataStr, 0 * 8, 8),16);
        data.put("co",BigDecimal.valueOf(co.intValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        // 二氧化碳
        BigInteger co2 = new BigInteger(ByteToHexUtil.substringByte(dataStr, 1 * 8, 8),16);
        data.put("co2",BigDecimal.valueOf(co2.intValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        // 臭氧
        BigInteger o3 = new BigInteger(ByteToHexUtil.substringByte(dataStr, 2 * 8, 8),16);
        data.put("o3",BigDecimal.valueOf(o3.intValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        // 二氧化硫
        BigInteger so2 = new BigInteger(ByteToHexUtil.substringByte(dataStr, 3 * 8, 8),16);
        data.put("so2",BigDecimal.valueOf(so2.intValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        // PM2.5
        BigInteger pm25 = new BigInteger(ByteToHexUtil.substringByte(dataStr, 4 * 8, 8),16);
        data.put("pm25",BigDecimal.valueOf(pm25.intValue()).divide(BigDecimal.valueOf(10), 1, RoundingMode.HALF_UP));
        // PM10
        BigInteger pm10 = new BigInteger(ByteToHexUtil.substringByte(dataStr, 5 * 8, 8),16);
        data.put("pm10",BigDecimal.valueOf(pm10.intValue()).divide(BigDecimal.valueOf(10), 1, RoundingMode.HALF_UP));
        // TVOC
        BigInteger tvoc = new BigInteger(ByteToHexUtil.substringByte(dataStr, 6 * 8, 8),16);
        data.put("tvoc",BigDecimal.valueOf(tvoc.intValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        // 温度
        BigInteger temperature = new BigInteger(ByteToHexUtil.substringByte(dataStr, 7 * 8, 8),16);
        data.put("temperature",BigDecimal.valueOf(temperature.intValue()).divide(BigDecimal.valueOf(10), 1, RoundingMode.HALF_UP));
        // 湿度
        BigInteger humidness = new BigInteger(ByteToHexUtil.substringByte(dataStr, 8 * 8, 8),16);
        data.put("humidness", BigDecimal.valueOf(humidness.intValue()).divide(BigDecimal.valueOf(10), 1, RoundingMode.HALF_UP));
        // 大气压
        BigInteger atmosphericPressure = new BigInteger(ByteToHexUtil.substringByte(dataStr, 9 * 8, 8),16);
        data.put("atmosphericPressure",BigDecimal.valueOf(atmosphericPressure.intValue()).divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP));
        // 风速
        BigInteger windSpeed = new BigInteger(ByteToHexUtil.substringByte(dataStr, 10 * 8, 8),16);
        data.put("windSpeed",BigDecimal.valueOf(windSpeed.intValue()).divide(BigDecimal.valueOf(10), 1, RoundingMode.HALF_UP));
        // 风向
        BigInteger windDirection = new BigInteger(ByteToHexUtil.substringByte(dataStr, 11 * 8, 8),16);
        data.put("windDirection",windDirection.intValue());
        // 信号强度
        BigInteger signalStrength = new BigInteger(ByteToHexUtil.substringByte(dataStr, 12 * 8, 8),16);
        data.put("signalStrength",BigDecimal.valueOf(signalStrength.intValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        // 错误码
        BigInteger errCode = new BigInteger(ByteToHexUtil.substringByte(dataStr, 13 * 8, 8),16);
        data.put("errCode",errCode.intValue());
        // 版本号
        BigInteger version = new BigInteger(ByteToHexUtil.substringByte(dataStr, 14 * 8, 8),16);
        data.put("version",BigDecimal.valueOf(version.intValue()).divide(BigDecimal.valueOf(10), 1, RoundingMode.HALF_DOWN));
        return data;
    }

    public static void main(String[] args) throws Exception {
        String msg = "FEDC10081609852469E90409003C000000000000165F00002516000000B40000003B0000005500000000000000C1000001C700018A00000000000000000700000008000000000000007581C1";
        JSONObject data = dealMsg(msg);
        System.out.println(data);
    }


}
