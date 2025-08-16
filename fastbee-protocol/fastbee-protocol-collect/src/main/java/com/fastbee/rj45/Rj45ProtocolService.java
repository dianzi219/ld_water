package com.fastbee.rj45;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.annotation.SysProtocol;
import com.fastbee.common.constant.FastBeeConstant;
import com.fastbee.common.core.mq.DeviceReport;
import com.fastbee.common.core.mq.MQSendMessageBo;
import com.fastbee.common.core.mq.message.DeviceData;
import com.fastbee.common.core.mq.message.FunctionCallBackBo;
import com.fastbee.common.core.thingsModel.ThingsModelSimpleItem;
import com.fastbee.common.core.thingsModel.ThingsModelValuesInput;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.BeanMapUtilByReflect;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.protocol.base.protocol.IProtocol;
import com.fastbee.rj45.model.RfId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author gsb
 * @date 2022/10/10 16:55
 */
@Slf4j
@Component
@SysProtocol(name = "RJ45解析协议",protocolCode = FastBeeConstant.PROTOCOL.RJ45,description = "系统内置RJ45解析协议")
public class Rj45ProtocolService implements IProtocol {

    /**
     * 解析RJ45格式数据
     */
    @Override
    public DeviceReport decode(DeviceData deviceData, String clientId) {
        try {
            DeviceReport reportMessage = new DeviceReport();
            String data = new String(deviceData.getData(),StandardCharsets.UTF_8);
            String[] split = data.split(",");
            String s = split[2];
            RfId rfId = new RfId();
            rfId.setLabelType(s.substring(0,2));
            rfId.setModelNo(s.substring(2,4));
            rfId.setEpc(s.substring(4));
            List<ThingsModelSimpleItem> values = BeanMapUtilByReflect.beanToItem(rfId);
            reportMessage.setThingsModelSimpleItem(values);
            reportMessage.setSerialNumber(clientId);
            reportMessage.setClientId(clientId);
            return reportMessage;
        }catch (Exception e){
            throw new ServiceException("数据解析异常"+e.getMessage());
        }
    }

    @Override
    public FunctionCallBackBo encode(MQSendMessageBo message) {
        try {
            FunctionCallBackBo callBack = new FunctionCallBackBo();
            String msg = JSONObject.toJSONString(message.getParams());
            callBack.setSources(msg);
            callBack.setMessage(msg.getBytes());
            return callBack;
        }catch (Exception e){
            log.error("=>指令编码异常,device={},data={}",message.getSerialNumber(),
                    message.getParams());
            return null;
        }
    }
}
