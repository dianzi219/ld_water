package com.fastbee.zqwl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.annotation.SysProtocol;
import com.fastbee.common.constant.FastBeeConstant;
import com.fastbee.common.core.mq.DeviceReport;
import com.fastbee.common.core.mq.MQSendMessageBo;
import com.fastbee.common.core.mq.message.DeviceData;
import com.fastbee.common.core.mq.message.FunctionCallBackBo;
import com.fastbee.common.core.redis.RedisCache;
import com.fastbee.common.core.redis.RedisKeyBuilder;
import com.fastbee.common.core.thingsModel.ThingsModelSimpleItem;
import com.fastbee.common.core.thingsModel.ThingsModelValuesInput;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.gateway.mq.TopicsUtils;
import com.fastbee.iot.model.ThingsModels.ThingsModelValueItem;
import com.fastbee.iot.service.IDeviceService;
import com.fastbee.protocol.base.protocol.IProtocol;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author gsb
 * @date 2023/8/14 16:04
 */
@Slf4j
@Component
@SysProtocol(name = "8路继电器+Modbus转Json-智嵌物联",protocolCode = FastBeeConstant.PROTOCOL.ModbusToJsonZQWL,description = "8路继电器+Modbus转Json-智嵌物联")
public class ModbusToJsonZQWLProtocolService implements IProtocol {


    @Resource
    private RedisCache redisCache;
    @Resource
    private TopicsUtils topicsUtils;
    @Resource
    private IDeviceService deviceService;

    /**
     * 上报数据格式:              <p>
     *  device1：   从机1标识     <p>
     *  name：      物模型标识符   <p>
     *  value：     上报值        <p>
     * {
     * 	"device1": [
     *                {
     * 			"name": "J2",
     * 			"value": 8.331631
     *        },
     *        {
     * 			"name": "J1",
     * 			"value": -130.123718
     *        }
     * 	],
     * 	"device2": [
     *        {
     * 			"name": "J4",
     * 			"value": -16.350224
     *        },
     *        {
     * 			"name": "J3",
     * 			"value": 94.769806
     *        }
     * 	]
     * }
     *
     * 下发报文格式<p>
     * device   从机编号  <p>
     * name     标识符    <p>
     * value    值        <p>
     * serNo    流水号    <p>
     * {
     * 	"device": 1,
     * 	"name": "template",
     * 	"value": 111,
     * 	"serNo": "213245489543789"
     * }
     * </p>
     *
     * 下发指令回复格式<p>
     * serNo   平台的流水号，用于对应回复消息   <p>
     * ack     下发指令状态 0是失败 1是成功    <p>
     *   {
     *  "serNo": "213245489543789",
     *  "ack": 1
     * }
     * </p>
     *
     */
    @Override
    public DeviceReport decode(DeviceData deviceData, String clientId) {
        try {
            DeviceReport reportMessage = new DeviceReport();
            String data = new String(deviceData.getData(),StandardCharsets.UTF_8);
            List<ThingsModelSimpleItem> result = new ArrayList<>();
            Map<String,Object> values = JSON.parseObject(data, Map.class);
            if (values.containsKey("addr") || (values.containsKey("cmd") && values.get("cmd").equals("ret"))){
                for (Map.Entry<String, Object> entry : values.entrySet()) {
                    if (entry.getKey().equals("x")){
                        JSONArray value = (JSONArray) entry.getValue();
                        for (int i = 0; i < value.size(); i++) {
                            ThingsModelSimpleItem simpleItem = new ThingsModelSimpleItem();
                            simpleItem.setTs(DateUtils.getNowDate());
                            simpleItem.setId(entry.getKey()+(i+1));
                            simpleItem.setValue(value.get(i)+"");
                            result.add(simpleItem);
                        }
                    }
                    if (entry.getKey().equals("y")){
                        JSONArray value = (JSONArray) entry.getValue();
                        for (int i = 0; i < value.size(); i++) {
                            ThingsModelSimpleItem simpleItem = new ThingsModelSimpleItem();
                            simpleItem.setTs(DateUtils.getNowDate());
                            simpleItem.setId(entry.getKey()+ (i+1));
                            simpleItem.setValue(value.get(i)+"");
                            result.add(simpleItem);
                        }
                    }
                }
            }else {
                for (Map.Entry<String, Object> entry : values.entrySet()) {
                    String key = entry.getKey();
                    if (key.contains("-")) {
                        String slaveKey = key.split("-")[0];
                        Integer slaveId = Integer.parseInt(slaveKey);
                        ThingsModelSimpleItem item = new ThingsModelSimpleItem();
                        item.setTs(DateUtils.getNowDate());
                        item.setValue(entry.getValue() + "");
                        item.setId(key);
                        //item.setSlaveId(slaveId);
                        result.add(item);
                    }
                }
            }
            reportMessage.setThingsModelSimpleItem(result);
            reportMessage.setClientId(clientId);
            reportMessage.setSerialNumber(clientId);
            return reportMessage;
        }catch (Exception e){
            throw new ServiceException("数据解析异常"+e.getMessage());
        }
    }

    @Override
    public FunctionCallBackBo encode(MQSendMessageBo message) {
        try {
            FunctionCallBackBo callBack = new FunctionCallBackBo();
            Long productId = message.getDp().getProductId();
            String messageValue = message.getValue();
            JSONObject params = new JSONObject();
            params.put("addr",1);
            params.put("cmd","set");
            String identifier = message.getIdentifier();
            int idIndex = Integer.parseInt(identifier.substring(1, 2));
            int value = Integer.parseInt(messageValue);
            int[] ints = new int[8];
            List<ThingsModelValueItem> cacheValueList = getCacheDeviceStatus(productId, message.getSerialNumber());
            if (identifier.contains("x")){
                for (ThingsModelValueItem valueItem : cacheValueList) {
                    if (valueItem.getId().contains("x")){
                        int itemIndex = Integer.parseInt(valueItem.getId().substring(1, 2));
                        ints[itemIndex-1] = Integer.parseInt(valueItem.getValue());
                    }
                }
                ints[idIndex-1] = value;
                params.put("x",ints);
            }else if (identifier.contains("y")){
                for (ThingsModelValueItem valueItem : cacheValueList) {
                    if (valueItem.getId().contains("y")){
                        int itemIndex = Integer.parseInt(valueItem.getId().substring(1, 2));
                        ints[itemIndex-1] = Integer.parseInt(valueItem.getValue());
                    }
                }
                ints[idIndex-1] = value;
                params.put("y",ints);
            }
            String msg = JSONObject.toJSONString(params);
            callBack.setSources(msg);
            callBack.setMessage(msg.getBytes());
            return callBack;
        }catch (Exception e){
            log.error("=>指令编码异常,device={}",message.getSerialNumber());
            throw new ServiceException(e.getMessage());
        }
    }

    private List<ThingsModelValueItem> getCacheDeviceStatus(Long productId, String deviceNumber) {
        String key = RedisKeyBuilder.buildTSLVCacheKey(productId, deviceNumber);
        Map<String, String> map = redisCache.hashEntity(key);
        List<ThingsModelValueItem> valueList = new ArrayList<>();
        if (map != null && map.size() >0){
            // 获取redis缓存的物模型值
            valueList = map.values().stream().map(s -> JSONObject.parseObject(s, ThingsModelValueItem.class))
                    .collect(Collectors.toList());
        }
        return valueList;
    }

}
