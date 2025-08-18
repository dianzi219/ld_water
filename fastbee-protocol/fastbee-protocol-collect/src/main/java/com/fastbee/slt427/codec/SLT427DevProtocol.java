package com.fastbee.slt427.codec;

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
import com.fastbee.slt427.model.ElecDev;
import com.fastbee.slt427.model.FlowDev;
import com.fastbee.protocol.base.protocol.IProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static com.fastbee.modbus.pak.TcpDtu.心跳包;
import static com.fastbee.modbus.pak.TcpDtu.注册报文;

/**
 * @author gsb
 * @date 2023/5/17 16:56
 */
@Slf4j
@Component
@SysProtocol(name = "SLT427解析协议",protocolCode = FastBeeConstant.PROTOCOL.SL247,description = "SLT427解析协议")
public class SLT427DevProtocol implements IProtocol {

    @Resource
    private SLT427DevDecoder devDecoder;
    @Resource
    private SLT427DevEncoder devEncoder;


    @Override
    public DeviceReport decode(DeviceData data, String clientId) {
        try {
            DeviceReport report = new DeviceReport();
            Object deviceObj = devDecoder.decode(data,null);
            
            // 根据解析出的对象类型进行不同的处理
            if (deviceObj instanceof ElecDev) {
                ElecDev elecDev = (ElecDev) deviceObj;
                this.handlerElecData(elecDev);
                List<ThingsModelSimpleItem> items = BeanMapUtilByReflect.beanToItem(elecDev);
                report.setThingsModelSimpleItem(items);
                report.setClientId(clientId);
                report.setMessageId(elecDev.getStart()+"");
                report.setIsPackage(true);
            } else if (deviceObj instanceof FlowDev) {
                FlowDev flowDev = (FlowDev) deviceObj;
                this.handlerFlowData(flowDev);
                List<ThingsModelSimpleItem> items = BeanMapUtilByReflect.beanToItem(flowDev);
                report.setThingsModelSimpleItem(items);
                report.setClientId(clientId);
                report.setMessageId(flowDev.getStart()+"");
                report.setIsPackage(true);
            }
            
            // 根据功能码设置消息ID，用于区分注册包和心跳包
            if (deviceObj instanceof ElecDev) {
                ElecDev elecDev = (ElecDev) deviceObj;
                String code = elecDev.getCode();
                if ("C0".equals(code)) {
                    // 心跳包
                    report.setMessageId(String.valueOf(心跳包));
                } else if ("C1".equals(code)) {
                    // 注册包
                    report.setMessageId(String.valueOf(注册报文));
                }
            } else if (deviceObj instanceof FlowDev) {
                FlowDev flowDev = (FlowDev) deviceObj;
                String code = flowDev.getCode();
                if ("C0".equals(code)) {
                    // 心跳包
                    report.setMessageId(String.valueOf(心跳包));
                } else if ("C1".equals(code)) {
                    // 注册包
                    report.setMessageId(String.valueOf(注册报文));
                }
            }
            
            return report;
        }catch (Exception e){
            log.error("=>数据解析出错",e);
            throw new ServiceException("数据解析出错"+e);
        }
    }

    @Override
    public FunctionCallBackBo encode(MQSendMessageBo message) {
        FunctionCallBackBo callBack = new FunctionCallBackBo();
        FlowDev flowDev = new FlowDev();
        flowDev.setImei(message.getSerialNumber());
        // flowDev.setControl(0x33);
        flowDev.setControl("33");
        flowDev.setLength(0x08);
        ByteBuf buf = devEncoder.encode(flowDev, null);
        byte[] source = ByteBufUtil.getBytes(buf, 3, buf.writerIndex() - 5);
        byte b = CRC8Utils.calcCrc8_E5(source);
        // 将byte转换为无符号int
        int crcValue = b & 0xFF;
        byte[] crc = new byte[]{(byte) crcValue, 0x16};

        // 创建最终的字节数组，长度为source长度+2字节CRC
        byte[] result = new byte[source.length + 2];
        System.arraycopy(source, 0, result, 0, source.length);
        System.arraycopy(crc, 0, result, source.length, 2);
        System.out.println(ByteBufUtil.hexDump(buf));
        //删除缓存，防止netty内存溢出
        ReferenceCountUtil.release(buf);
        callBack.setSources(ByteBufUtil.hexDump(buf));
        callBack.setMessage(result);
        return callBack;
    }

    private FlowDev handlerFlowData(FlowDev flowDev){
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
//        String instant = flowDev.getInstant();
//        if (StringUtils.isNotEmpty(instant)){
//            String replace = instant.replace("0", "");
//            flowDev.setInstant(replace.equals("")? "0":replace);
//            int val = Integer.parseInt(flowDev.getInstant())/1000;
//            flowDev.setInstant(val+"");
//        }
        return flowDev;
    }
    
    private ElecDev handlerElecData(ElecDev elecDev) {
        //时间处理
        String ts = elecDev.getTs();
        if (StringUtils.isNotEmpty(ts)){
            Date date = DateUtils.dateTime(DateUtils.SS_MM_HH_DD_HH_YY, ts);
            String s = DateUtils.dateTimeYY(date);
            elecDev.setTs(s);
        }
        String vol = elecDev.getVol();
        if (StringUtils.isNotEmpty(vol)){
            String replace = vol.replace("0", "");
            elecDev.setVol(replace.equals("")? "0":replace);
        }
        return elecDev;
    }
}