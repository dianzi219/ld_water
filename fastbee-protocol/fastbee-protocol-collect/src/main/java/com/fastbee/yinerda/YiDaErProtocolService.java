package com.fastbee.yinerda;

import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.annotation.SysProtocol;
import com.fastbee.common.constant.FastBeeConstant;
import com.fastbee.common.core.mq.DeviceReport;
import com.fastbee.common.core.mq.MQSendMessageBo;
import com.fastbee.common.core.mq.message.DeviceData;
import com.fastbee.common.core.mq.message.FunctionCallBackBo;
import com.fastbee.common.enums.FunctionReplyStatus;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.protocol.base.protocol.IProtocol;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * @author gsb
 * @date 2022/10/10 16:55
 */
@Slf4j
@Component
@SysProtocol(name = "YinErDa解析协议",protocolCode = FastBeeConstant.PROTOCOL.YinErDa,description = "YinErDa解析协议")
public class YiDaErProtocolService implements IProtocol {


    /**
     * YiDaEr,这里处理设备回复，不进行数据存储
     * 上报数据格式：
     *   on:done
     *   off:done
     */
    @Override
    public DeviceReport decode(DeviceData deviceData, String clientId) {
        try {
            DeviceReport reportMessage = new DeviceReport();
            String data = new String(deviceData.getData(),StandardCharsets.UTF_8);
            reportMessage.setReplyMessage(data);
            reportMessage.setStatus(FunctionReplyStatus.SUCCESS);
            reportMessage.setIsReply(true);
            reportMessage.setProtocolCode(FastBeeConstant.PROTOCOL.YinErDa);
            return reportMessage;
        }catch (Exception e){
            throw new ServiceException("数据解析异常"+e.getMessage());
        }
    }

    @Override
    public FunctionCallBackBo encode(MQSendMessageBo message) {
        try {
            FunctionCallBackBo callBack = new FunctionCallBackBo();
            String value = message.getValue();
            value = "0".equals(value) ? "off" : "on";
            callBack.setMessage(value.getBytes());
            callBack.setSources(value);
            return callBack;
        }catch (Exception e){
            log.error("=>指令编码异常,device={},data={}",message.getSerialNumber(),
                    message.getParams());
            return null;
        }
    }
}
