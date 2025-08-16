package com.fastbee.jsonchenyi;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
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
import com.fastbee.common.utils.DateUtils;
import com.fastbee.protocol.base.protocol.IProtocol;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author gsb
 * @date 2022/10/10 16:55
 */
@Slf4j
@Component
@SysProtocol(name = "JSON-Data解析协议", protocolCode = FastBeeConstant.PROTOCOL.JsonObject_ChenYi, description = "系统内置JSONObject解析协议")
public class JsonChenYiProtocolService implements IProtocol {

    /**
     * 解析json格式数据
     * 上报数据格式：
     */
    @Override
    public DeviceReport decode(DeviceData deviceData, String clientId) {
        try {
            DeviceReport reportMessage = new DeviceReport();
            // bytep[] 转String
            String data = new String(deviceData.getData(), StandardCharsets.UTF_8);
            Map<String, Object> values = JSON.parseObject(data, Map.class);
            JSONArray array = (JSONArray) values.get("data");
            List<ThingsModelSimpleItem> result = new ArrayList<>();
            JSONObject jsonObject = (JSONObject) array.get(0);
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                ThingsModelSimpleItem item = new ThingsModelSimpleItem();
                item.setTs(DateUtils.getNowDate());
                item.setValue(entry.getValue() + "");
                item.setId(entry.getKey());
                result.add(item);
            }
            reportMessage.setThingsModelSimpleItem(result);
            reportMessage.setClientId(clientId);
            reportMessage.setSerialNumber(clientId);
            reportMessage.setSources(data);
            return reportMessage;
        } catch (Exception e) {
            throw new ServiceException("数据解析异常" + e.getMessage());
        }
    }

    @Override
    public FunctionCallBackBo encode(MQSendMessageBo message) {
        try {
            FunctionCallBackBo callBack = new FunctionCallBackBo();
            String msg = message.getParams().toString();
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
