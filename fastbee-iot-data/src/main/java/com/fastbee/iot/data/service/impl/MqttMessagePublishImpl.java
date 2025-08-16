package com.fastbee.iot.data.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.core.device.DeviceAndProtocol;
import com.fastbee.common.core.mq.MQSendMessageBo;
import com.fastbee.common.core.mq.message.DeviceMessage;
import com.fastbee.common.core.mq.message.FunctionCallBackBo;
import com.fastbee.common.core.mq.message.ReportDataBo;
import com.fastbee.common.core.mq.ota.OtaUpgradeBo;
import com.fastbee.common.core.protocol.Message;
import com.fastbee.common.core.redis.RedisCache;
import com.fastbee.common.core.redis.RedisKeyBuilder;
import com.fastbee.common.core.thingsModel.ThingsModelSimpleItem;
import com.fastbee.common.enums.DeviceStatus;
import com.fastbee.common.enums.FunctionReplyStatus;
import com.fastbee.common.enums.ServerType;
import com.fastbee.common.enums.TopicType;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.common.utils.gateway.mq.TopicsUtils;
import com.fastbee.common.utils.modbus.ModbusUtils;
import com.fastbee.iot.cache.IFirmwareCache;
import com.fastbee.iot.cache.ITSLCache;
import com.fastbee.iot.data.service.IDataHandler;
import com.fastbee.iot.data.service.IFirmwareTaskDetailService;
import com.fastbee.iot.data.service.IMqttMessagePublish;
import com.fastbee.iot.domain.Device;
import com.fastbee.iot.domain.FunctionLog;
import com.fastbee.iot.domain.ModbusConfig;
import com.fastbee.iot.enums.DeviceType;
import com.fastbee.iot.model.NtpModel;
import com.fastbee.iot.model.ThingsModels.ThingsModelValueItem;
import com.fastbee.iot.ruleEngine.MsgContext;
import com.fastbee.iot.ruleEngine.RuleProcess;
import com.fastbee.iot.service.IDeviceService;
import com.fastbee.iot.service.IFunctionLogService;
import com.fastbee.iot.service.IOrderControlService;
import com.fastbee.iot.util.SnowflakeIdWorker;
import com.fastbee.mqttclient.PubMqttClient;
import com.fastbee.protocol.base.protocol.IProtocol;
import com.fastbee.protocol.service.IProtocolManagerService;
import com.fastbee.sip.service.IGatewayService;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

import static com.fastbee.common.constant.FastBeeConstant.PROTOCOL.ModbusRtu;

/**
 * 消息推送方法集合
 *
 * @author bill
 */
@Slf4j
@Service
public class MqttMessagePublishImpl implements IMqttMessagePublish {

    @Resource
    private IProtocolManagerService protocolManagerService;
    @Resource
    private PubMqttClient mqttClient;
    @Resource
    private IFirmwareCache firmwareCache;
    @Resource
    private IFirmwareTaskDetailService firmwareTaskDetailService;
    @Resource
    private MessageManager messageManager;
    @Resource
    private TopicsUtils topicsUtils;
    @Resource
    private IDeviceService deviceService;
    @Resource
    private IFunctionLogService functionLogService;
    @Resource
    private IDataHandler dataHandler;
    @Resource
    private RuleProcess ruleProcess;
    @Autowired
    private IGatewayService gatewayService;
    @Resource
    private ITSLCache itslCache;
    @Resource
    private RedisCache redisCache;
    @Resource
    private PubMqttClient pubMqttClient;
    @Resource
    private IOrderControlService orderControlService;
    private final SnowflakeIdWorker snowflakeIdWorker = new SnowflakeIdWorker(3);


    /**
     * 服务(指令)下发
     */
    @Override
    public void funcSend(MQSendMessageBo bo) throws InterruptedException {
        DeviceAndProtocol deviceAndProtocol = deviceService.selectProtocolBySerialNumber(bo.getSerialNumber());
        Optional.ofNullable(deviceAndProtocol).orElseThrow(() -> new ServiceException("服务下发的设备:[" + bo.getSerialNumber() + "]不存在"));
        int deviceType = deviceAndProtocol.getDeviceType();
        String sourceNo = bo.getSerialNumber();
        //处理数据下发
        bo.setDp(deviceAndProtocol);
        Long productId = bo.getDp().getProductId();
        String serialNumber = bo.getSerialNumber();
        String transport = bo.getDp().getTransport();
        //获取物模型
        ThingsModelValueItem thingModels = itslCache.getSingleThingModels(productId, bo.getIdentifier());
        ModbusConfig modbusConfig = thingModels.getConfig();
        if (!Objects.isNull(modbusConfig)) {
            thingModels.getConfig().setModbusCode(ModbusUtils.getModbusCode(modbusConfig.getType()));
            if (deviceType == DeviceType.SUB_GATEWAY.getCode()) {
                //这里获取绑定网关时，设置的子设备地址
                thingModels.getConfig().setSlave(deviceAndProtocol.getSlaveId() == null ? deviceAndProtocol.getProSlaveId() : deviceAndProtocol.getSlaveId());
                //设置网关产品id，设备编号
                bo.setSerialNumber(deviceAndProtocol.getGwSerialNumber());
                bo.getDp().setProductId(deviceAndProtocol.getGwProductId());
                productId = deviceAndProtocol.getGwProductId();
                serialNumber = deviceAndProtocol.getGwSerialNumber();
            }
        }
        bo.setThingsModel(JSON.toJSONString(thingModels));
        Integer type = thingModels.getType();
        //处理影子模式
        this.hadndlerShadow(bo, type);
        //下发指令日志
        FunctionLog funcLog = this.handleLog(bo, thingModels.getName());
        ServerType serverType = ServerType.explain(transport);
        //组建下发服务指令
        FunctionCallBackBo backBo = buildMessage(bo);
        switch (serverType) {
            case MQTT:
                //  规则引擎脚本处理,完成后返回结果
                MsgContext context = ruleProcess.processRuleScript(serialNumber, 2, backBo.getTopicName(), new String(backBo.getMessage()));
                if (!Objects.isNull(context) && StringUtils.isNotEmpty(context.getPayload())
                        && StringUtils.isNotEmpty(context.getTopic())) {
                    backBo.setTopicName(context.getTopic());
                    backBo.setMessage(context.getPayload().getBytes());
                }

                publishWithLog(backBo.getTopicName(), backBo.getMessage(), funcLog,bo.getDelay());
                log.debug("=>服务下发,topic=[{}],指令=[{}]", backBo.getTopicName(), new String(backBo.getMessage()));
                break;
            case TCP:
                Message data = new Message();
                data.setPayload(Unpooled.wrappedBuffer(backBo.getMessage()));
                data.setClientId(backBo.getSerialNumber());
                messageManager.requestR(serialNumber, data, Message.class);
                funcLog.setResultMsg(FunctionReplyStatus.NORELY.getMessage());
                funcLog.setResultCode(FunctionReplyStatus.NORELY.getCode());
                functionLogService.insertFunctionLog(funcLog);
                break;
            case UDP:
                break;
            case COAP:
                break;
            case GB28181:
                MqttMessagePublishImpl.log.debug("=>功能指令下发,functinos=[{}]", bo);
                gatewayService.sendFunction(serialNumber, bo.getIdentifier(), bo.getParams().getString(bo.getIdentifier()));
                break;

        }
        //发送至前端数据调试
        String topic = topicsUtils.buildTopic(bo.getDp().getProductId(), bo.getSerialNumber(), TopicType.MESSAGE_POST);
        DeviceMessage deviceMessage = new DeviceMessage();
        deviceMessage.setMessage(backBo.getSources());
        deviceMessage.setTime(new Date());
        deviceMessage.setTopicName(TopicType.FUNCTION_GET.getTopicSuffix());
        byte[] bytes = JSONObject.toJSONString(deviceMessage).getBytes();
        publishWithLog(topic, bytes, null,null);

        if (deviceAndProtocol.getProtocolCode().equals(ModbusRtu)) {
            //这里做一个消息id标记消息下发顺序，如果设备指令带流水号，则不需要使用
            String cacheKey = RedisKeyBuilder.buildDownMessageIdCacheKey(sourceNo);
            redisCache.zSetAdd(cacheKey, backBo.getSources() + ":" + bo.getMessageId(), DateUtils.getTimestampSeconds());
        }
        //处理指令下发权限问题
        deviceService.updateByOrder(bo.getUserId(), deviceAndProtocol.getDeviceId());
    }


    /**
     * 处理影子模式
     */
    private void hadndlerShadow(MQSendMessageBo bo, int type) {
        //处理设备影子模式
        if (Boolean.TRUE.equals(bo.getIsShadow())) {
            List<ThingsModelSimpleItem> dataList = new ArrayList<>();
            bo.getParams().forEach((key, value) -> {
                ThingsModelSimpleItem item = new ThingsModelSimpleItem();
                item.setId(key);
                item.setValue(value + "");
                dataList.add(item);
            });
            ReportDataBo dataBo = new ReportDataBo();
            dataBo.setDataList(dataList)
                    .setProductId(bo.getDp().getProductId())
                    .setSerialNumber(bo.getSerialNumber())
                    .setRuleEngine(false)
                    .setShadow(true)
                    .setType(type);
            dataHandler.reportData(dataBo);
            return;
        }
    }

    /**
     * 处理下发指令日志
     *
     * @return
     */
    private FunctionLog handleLog(MQSendMessageBo message, String modelName) {
        /* 下发服务数据存储对象*/
        JSONObject params = message.getParams();
        String val = params.get(message.getIdentifier()) + "";
        message.setValue(val);
        FunctionLog funcLog = new FunctionLog();
        funcLog.setCreateTime(DateUtils.getNowDate());
        funcLog.setFunValue(val);
        funcLog.setMessageId(message.getMessageId());
        funcLog.setSerialNumber(message.getSerialNumber());
        funcLog.setIdentify(message.getIdentifier());
        funcLog.setShowValue(val);
        funcLog.setFunType(1);
        funcLog.setModelName(modelName);
        return funcLog;
    }

    /**
     * OTA升级下发
     *
     * @param bo
     */
    @Override
    public void upGradeOTA(OtaUpgradeBo bo) {
        if (TopicType.FETCH_FIRMWARE_SET.getTopicSuffix().equals(bo.getTopicName())) {
            String topicName = topicsUtils.buildTopic(bo.getSerialNumber(), TopicType.FETCH_FIRMWARE_SET);
            FunctionLog log = new FunctionLog();
            log.setCreateTime(DateUtils.getNowDate());
            log.setSerialNumber(bo.getSerialNumber());
            log.setFunType(3);
            log.setIdentify("OTA");
            log.setShowValue(ByteBufUtil.hexDump(bo.getMsg()));
            mqttClient.publish(0,false,topicName, ByteBufUtil.hexDump(bo.getMsg()));
        }
        else if (TopicType.HTTP_FIRMWARE_SET.getTopicSuffix().equals(bo.getTopicName())) {
            String topicName = topicsUtils.buildTopic(bo.getSerialNumber(), TopicType.HTTP_FIRMWARE_SET);
            JSONObject message = new JSONObject();
            message.put("taskId", bo.getTaskId());
            message.put("url", bo.getUrl());
            message.put("version", bo.getVersion());
            message.put("status", bo.getStatus());
            mqttClient.publish(0, false, topicName, message.toJSONString());
        }

    }

    @Override
    public FunctionCallBackBo buildMessage(MQSendMessageBo bo) {
        String protocolCode = bo.getDp().getProtocolCode();
        String transport = bo.getDp().getTransport();
//        if (ServerType.TCP.getCode().equals(transport) && ModbusRtu.equals(protocolCode)) {
//            protocolCode = ModbusRtuTcp;
//        }
        Long productId = bo.getDp().getProductId();
        String serialNumber = bo.getSerialNumber();
        /*组建Topic*/
        String topic = topicsUtils.buildTopic(productId, serialNumber, TopicType.FUNCTION_GET);
        bo.setTopicName(topic);
        /*获取编码协议*/
        IProtocol protocolInstance = protocolManagerService.getProtocolByProtocolCode(protocolCode);
        //根据协议编码后数据
        FunctionCallBackBo callBackBo = protocolInstance.encode(bo);
        callBackBo.setSerialNumber(serialNumber);
        callBackBo.setTopicName(topic);
        return callBackBo;
    }

    /**
     * 1.发布设备状态
     */
    @Override
    public void publishStatus(Long productId, String deviceNum, int deviceStatus, int isShadow, int rssi) {
        String message = "{\"status\":" + deviceStatus + ",\"isShadow\":" + isShadow + ",\"rssi\":" + rssi + "}";
        String topic = topicsUtils.buildTopic(productId, deviceNum, TopicType.STATUS_POST);
        mqttClient.publish(1, false, topic, message);
    }


    /**
     * 2.发布设备信息
     */
    @Override
    public void publishInfo(Long productId, String deviceNum) {
        String topic = topicsUtils.buildTopic(productId, deviceNum, TopicType.INFO_GET);
        mqttClient.publish(1, false, topic, "");
    }

    /**
     * 3.发布时钟同步信息
     *
     * @param bo 数据模型
     */
    public void publishNtp(ReportDataBo bo) {
        NtpModel ntpModel = JSON.parseObject(bo.getMessage(), NtpModel.class);
        ntpModel.setServerRecvTime(System.currentTimeMillis());
        ntpModel.setServerSendTime(System.currentTimeMillis());
        String topic = topicsUtils.buildTopic(bo.getProductId(), bo.getSerialNumber(), TopicType.NTP_GET);
        mqttClient.publish(1, false, topic, JSON.toJSONString(ntpModel));
    }

    /**
     * 4.发布属性
     * delay 延时，秒为单位
     */
    @Override
    public void publishProperty(Long productId, String deviceNum, List<ThingsModelSimpleItem> thingsList, int delay) {
        String topic = topicsUtils.buildTopic(productId, deviceNum, TopicType.FUNCTION_GET);
        if (thingsList == null) {
            mqttClient.publish(1, true, topic, "");
        } else {
            mqttClient.publish(1, true, topic, JSON.toJSONString(thingsList));
        }
    }

    /**
     * 5.发布功能
     * delay 延时，秒为单位
     */
    @Override
    public void publishFunction(Long productId, String deviceNum, List<ThingsModelSimpleItem> thingsList, int delay) {
        String pre = "";
        if (delay > 0) {
            pre = "$delayed/" + String.valueOf(delay) + "/";
        }
        String topic = topicsUtils.buildTopic(productId, deviceNum, TopicType.FUNCTION_GET);
        if (thingsList == null) {
            mqttClient.publish(1, true, topic, "");
        } else {
            mqttClient.publish(1, true, topic, JSON.toJSONString(thingsList));
        }

    }


    public void publishWithLog(String topic, byte[] pushMessage, FunctionLog log ,Long delay) {
        try {
            if (!Objects.isNull(delay) && delay >0){
                Thread.sleep(delay);
            }
            mqttClient.publish(pushMessage, topic, false, 0);
            if (null != log) {
                //存储服务下发成功
                log.setResultMsg(FunctionReplyStatus.NORELY.getMessage());
                log.setResultCode(FunctionReplyStatus.NORELY.getCode());
                functionLogService.insertFunctionLog(log);
            }
        } catch (Exception e) {
            if (null != log) {
                //服务下发失败存储
                log.setResultMsg(FunctionReplyStatus.FAIl.getMessage() + "原因: " + e.getMessage());
                log.setResultCode(FunctionReplyStatus.FAIl.getCode());
                functionLogService.insertFunctionLog(log);
            }
        }
    }

    /**
     * 推送设备状态
     *
     * @param device 设备
     * @param status       状态
     */
    public void pushDeviceStatus(Device device, DeviceStatus status) {
        String message = "{\"status\":" + status.getType() + ",\"isShadow\":" + device.getIsShadow() + ",\"rssi\":" + device.getRssi() + "}";
        String topic = topicsUtils.buildTopic(device.getProductId(), device.getSerialNumber(), TopicType.STATUS_POST);
        pubMqttClient.publish(0, false, topic, message);
    }

}
