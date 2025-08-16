package com.fastbee.flowdev.codec;

import com.fastbee.common.annotation.SysProtocol;
import com.fastbee.common.constant.FastBeeConstant;
import com.fastbee.common.core.mq.DeviceReport;
import com.fastbee.common.core.mq.MQSendMessageBo;
import com.fastbee.common.core.mq.message.DeviceData;
import com.fastbee.common.core.mq.message.FunctionCallBackBo;
import com.fastbee.common.core.thingsModel.ThingsModelSimpleItem;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.BeanMapUtilByReflect;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.common.utils.gateway.CRC8Utils;
import com.fastbee.flowdev.model.FlowDev2;
import com.fastbee.protocol.base.protocol.IProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author gsb
 * @date 2023/5/17 16:56
 */
@Slf4j
@Component
@SysProtocol(name = "流量计解析协议",protocolCode = FastBeeConstant.PROTOCOL.FlowMeter,description = "流量计解析协议")
public class FlowDevProtocol2 implements IProtocol {

    @Resource
    private FlowDevDecoder2 devDecoder;
    @Resource
    private FlowDevEncoder2 devEncoder;


    @Override
    public DeviceReport decode(DeviceData data, String clientId) {
        try {
            log.error("=>#######################################################################"+data.toString());
            System.out.println(data.toString());
            DeviceReport report = new DeviceReport();
            FlowDev2 flowDev = devDecoder.decode(data,null);
            this.handlerData(flowDev);
            List<ThingsModelSimpleItem> items = BeanMapUtilByReflect.beanToItem(flowDev);
            report.setThingsModelSimpleItem(items);
            report.setClientId(clientId);
            report.setMessageId(flowDev.getStart()+"");
            report.setIsPackage(true);
            report.setSources(byteArrayToHexString(data.getData()));
            System.out.println("222222222222222222222222222222222222222222222222222222222222222222"+report.toString());
            return report;
        }catch (Exception e){
            log.error("=>数据解析出错",e);
            throw new ServiceException("数据解析出错"+e);
        }
    }

    @Override
    public FunctionCallBackBo encode(MQSendMessageBo message) {
        FunctionCallBackBo callBack = new FunctionCallBackBo();
        FlowDev2 flowDev = new FlowDev2();
        flowDev.setImei(message.getSerialNumber());
        flowDev.setDire(0x33);
        flowDev.setLength(0x08);
        ByteBuf buf = devEncoder.encode(flowDev, null);
        byte[] source = ByteBufUtil.getBytes(buf, 3, buf.writerIndex() - 5);
        byte[] result = new byte[ByteBufUtil.getBytes(buf).length];
        byte b = CRC8Utils.calcCrc8_E5(source);
        byte[] crc = new  byte[]{b,0x16};
        System.arraycopy(source,0,result,0,source.length);
        System.arraycopy(crc,0,result,result.length -2,2);
        System.out.println(ByteBufUtil.hexDump(buf));
        //删除缓存，防止netty内存溢出
        ReferenceCountUtil.release(buf);
        callBack.setSources(ByteBufUtil.hexDump(buf));
        callBack.setMessage(result);
        return callBack;
    }

    private FlowDev2 handlerData(FlowDev2 flowDev){
        //时间处理
        String ts = flowDev.getTs();
        if (StringUtils.isNotEmpty(ts)){
            Date date = DateUtils.dateTime(DateUtils.SS_MM_HH_DD_HH_YY, ts);
            String s = DateUtils.dateTimeYY(date);
            flowDev.setTs(s);
        }
        String sum = flowDev.getSum();
        if (StringUtils.isNotEmpty(sum)){
            String replace = sum.replace("0", "");
            flowDev.setSum(replace.equals("")? "0":replace);
        }
        String instant = flowDev.getInstant();
        if (StringUtils.isNotEmpty(instant)){
            String replace = instant.replace("0", "");
            flowDev.setInstant(replace.equals("")? "0":replace);
            int val = Integer.parseInt(flowDev.getInstant())/1000;
            flowDev.setInstant(val+"");
        }
        return flowDev;
    }
    private String byteArrayToHexString(byte[] byteArray) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : byteArray) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
