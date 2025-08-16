package com.fastbee.sgz;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.annotation.SysProtocol;
import com.fastbee.common.constant.FastBeeConstant;
import com.fastbee.common.core.mq.DeviceReport;
import com.fastbee.common.core.mq.MQSendMessageBo;
import com.fastbee.common.core.mq.message.DeviceData;
import com.fastbee.common.core.mq.message.DeviceDownMessage;
import com.fastbee.common.core.mq.message.FunctionCallBackBo;
import com.fastbee.common.core.redis.RedisCache;
import com.fastbee.common.core.redis.RedisKeyBuilder;
import com.fastbee.common.core.thingsModel.ThingsModelSimpleItem;
import com.fastbee.common.core.thingsModel.ThingsModelValuesInput;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.iot.model.ThingsModels.ValueItem;
import com.fastbee.protocol.base.protocol.IProtocol;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author gsb
 * @date 2024/3/29 11:27
 */
@Slf4j
@Component
@SysProtocol(name = "数码灌装解析协议", protocolCode = FastBeeConstant.PROTOCOL.SGZ, description = "数码灌装解析协议")
public class SgzProtocolService implements IProtocol {

    @Resource
    private RedisCache redisCache;

    /**
     * 上报数据格式 <p>
     * {
     * "params": {
     * "DI" : 0,
     * "DO" : 1
     * }
     * }
     */
    @Override
    public DeviceReport decode(DeviceData deviceData, String clientId) {
        SgzMessageType dtype = SgzMessageType.HEART;
        DeviceReport reportMessage = new DeviceReport();
        String data = new String(deviceData.getData(), StandardCharsets.UTF_8);
        JSONObject params = (JSONObject) JSON.parse(data);
        List<ThingsModelSimpleItem> result = new ArrayList<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            ThingsModelSimpleItem item = new ThingsModelSimpleItem();
            String val = entry.getValue() + "";
            item.setTs(DateUtils.getNowDate());
            item.setValue(val);
            item.setId(entry.getKey());
            result.add(item);
            if (entry.getKey().equals("fuc")) {
                dtype = SgzMessageType.convert(val);
            }
            if (dtype.equals(SgzMessageType.CZSTOP)) {
                String[] strings = {"stime", "sno", "scode", "smiao", "sjz", "spz", "etime", "stotal"};
                String[] split = val.split(",");
                for (int i = 0; i < split.length; i++) {
                    ThingsModelSimpleItem simpleItem = new ThingsModelSimpleItem();
                    simpleItem.setTs(DateUtils.getNowDate());
                    simpleItem.setValue(split[i]);
                    simpleItem.setId(strings[i]);
                    result.add(simpleItem);
                }
            }
        }
        reportMessage.setThingsModelSimpleItem(result);
        reportMessage.setIsPackage(true);
        reportMessage.setMessageId(dtype.messageId + "");
        reportMessage.setClientId(clientId);
        reportMessage.setSerialNumber(clientId);
        return reportMessage;
    }

    @Override
    public FunctionCallBackBo encode(MQSendMessageBo message) {
        try {
            FunctionCallBackBo callBack = new FunctionCallBackBo();
            String cacheKey = RedisKeyBuilder.buildTSLVCacheKey(message.getDp().getProductId(), message.getSerialNumber());
            String data = redisCache.getCacheMapValue(cacheKey, "dtype");
            ValueItem valueItem = JSON.parseObject(data, ValueItem.class);
            JSONObject params = message.getParams();
            Object scode = params.get("scode");
            params.put("device", message.getSerialNumber());
            params.put("fuc", SgzMessageType.CZOK);
            params.put("dtype", valueItem.getValue());
            params.put("sdata", scode);
            String out = JSON.toJSONString(params);
            callBack.setMessage(out.getBytes());
            callBack.setSources(out);
            return callBack;
            // }
        } catch (Exception e) {
            throw new ServiceException("指令编号异常" + e.getMessage());
        }
    }

    @Override
    public byte[] encodeCallBack(Object message) {

        return new byte[0];
    }
}
