package com.fastbee.iot.data.service.impl;

import com.alibaba.fastjson.JSON;
import com.fastbee.base.service.ISessionStore;
import com.fastbee.base.session.Session;
import com.fastbee.common.constant.FastBeeConstant;
import com.fastbee.common.core.mq.DeviceReport;
import com.fastbee.common.core.mq.DeviceReportBo;
import com.fastbee.common.core.mq.DeviceStatusBo;
import com.fastbee.common.core.mq.message.DeviceData;
import com.fastbee.common.core.mq.message.ReportDataBo;
import com.fastbee.common.core.mq.ota.OtaReplyMessage;
import com.fastbee.common.core.ota.OtaPackageCode;
import com.fastbee.common.core.redis.RedisCache;
import com.fastbee.common.core.redis.RedisKeyBuilder;
import com.fastbee.common.enums.*;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.gateway.mq.TopicsUtils;
import com.fastbee.iot.cache.IOtaTaskCache;
import com.fastbee.iot.data.service.IDataHandler;
import com.fastbee.iot.data.service.IDeviceReportMessageService;
import com.fastbee.iot.data.service.IFirmwareTaskDetailService;
import com.fastbee.iot.domain.FunctionLog;
import com.fastbee.iot.enums.DeviceType;
import com.fastbee.iot.model.DeviceStatusVO;
import com.fastbee.iot.service.IDeviceService;
import com.fastbee.iot.service.IFunctionLogService;
import com.fastbee.iot.service.IProductService;
import com.fastbee.mq.producer.MessageProducer;
import com.fastbee.mqtt.manager.MqttRemoteManager;
import com.fastbee.mqtt.model.PushMessageBo;
import com.fastbee.protocol.base.protocol.IProtocol;
import com.fastbee.protocol.service.IProtocolManagerService;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 处理类 处理设备主动上报和设备回调信息
 *
 * @author bill
 */
@Service
@Slf4j
public class DeviceReportMessageServiceImpl implements IDeviceReportMessageService {

    @Autowired
    private IDeviceService deviceService;
    @Autowired
    private IProductService productService;
    @Autowired
    private IProtocolManagerService protocolManagerService;
    @Autowired
    private IFirmwareTaskDetailService firmwareTaskDetailService;
    @Resource
    private IDataHandler dataHandler;
    @Resource
    private RedisCache redisCache;
    @Resource
    private IFunctionLogService logService;
    @Resource
    private ISessionStore sessionStore;
    @Resource
    private IOtaTaskCache otaTaskCache;
    @Resource
    private TopicsUtils topicsUtils;
    @Resource
    private MqttRemoteManager remoteManager;

    /**
     * 处理设备主动上报数据
     */
    @Override
    public void parseReportMsg(DeviceReportBo bo) {
        String serialNumber = bo.getSerialNumber();
        switch (bo.getServerType()) {
            case MQTT:
                log.debug("=>MQ*收到设备主题[{}],消息:[{}]", bo.getTopicName(), bo.getData());
                //构建消息
                DeviceStatusVO deviceStatusVO = buildReport(bo);
                Long productId = bo.getProductId();
                /*获取协议处理器*/
                IProtocol protocol = selectedProtocol(productId);
                DeviceData data = DeviceData.builder()
                        .serialNumber(serialNumber)
                        .topicName(bo.getTopicName())
                        .productId(deviceStatusVO.getProductId())
                        .data(bo.getData())
                        .buf(Unpooled.wrappedBuffer(bo.getData()))
                        .build();
                /*根据协议解析后的数据*/
                DeviceReport reportMessage = protocol.decode(data, serialNumber);
                //设备回复，更新指令下发记录
                if (reportMessage.getIsReply()) {
                    handlerDeviceReply(reportMessage);
                }
                serialNumber = reportMessage.getSerialNumber() == null ? serialNumber : reportMessage.getSerialNumber();
                productId = reportMessage.getProductId()== null ? productId : reportMessage.getProductId();
                reportMessage.setSerialNumber(serialNumber);
                reportMessage.setProductId(productId);
                reportMessage.setPlatformDate(bo.getPlatformDate());
                reportMessage.setServerType(bo.getServerType());
                //同步设备状态
                this.synchDeviceStatus(serialNumber);
                //处理网关设备上报数据
                processNoSub(reportMessage, bo.getTopicName());
                break;
            case TCP:
                log.debug("*MQ收到TCP推送消息[{}]", JSON.toJSON(bo.getThingsModelSimpleItem()));
                DeviceStatusVO deviceStatusVO1 = deviceService.selectDeviceStatusAndTransportStatus(serialNumber);
                Optional.ofNullable(deviceStatusVO1).orElseThrow(() -> new ServiceException("设备不存在"));
                //同步设备状态
                this.synchDeviceStatus(serialNumber);
                DeviceReport deviceReport = new DeviceReport();
                BeanUtils.copyProperties(bo, deviceReport);
                deviceReport.setProductId(deviceStatusVO1.getProductId());
                deviceReport.setThingsModelSimpleItem(bo.getThingsModelSimpleItem());
                deviceReport.setSlaveId(bo.getSlaveId());
                deviceReport.setSerialNumber(deviceStatusVO1.getSerialNumber());
                //设备回复数据处理
                if (bo.getIsReply()) {
                    handlerDeviceReply(deviceReport);
                }
                processNoSub(deviceReport, null);
                break;
        }
    }



    /**
     * 处理设备回调数据
     */
    @Override
    public void parseReplyMsg(DeviceReportBo bo) {
        log.debug("=>MQ*收到设备回调消息,[{}]", bo);
        buildReport(bo);
        //获取解析协议
        IProtocol protocol = selectedProtocol(bo.getProductId());
        DeviceData deviceSource = DeviceData.builder()
                .serialNumber(bo.getSerialNumber())
                .topicName(bo.getTopicName())
                .data(bo.getData())
                .build();
        //协议解析后的数据
        DeviceReport message = protocol.decode(deviceSource, null);
        //处理网关设备回复数据
        processNoSub(message, bo.getTopicName());

    }

    /**
     * 处理设备OTA升级
     *
     * @param bo
     */
    @Override
    public void parseOTAUpdateReply(DeviceReportBo bo) {
        if (bo.getTopicName().endsWith(TopicType.FETCH_UPGRADE_REPLY.getTopicSuffix())) {
            String message = ByteBufUtil.hexDump(bo.getData());
            IProtocol protocol = protocolManagerService.getProtocolByProtocolCode(FastBeeConstant.PROTOCOL.NetOTA);
            DeviceData deviceData = protocol.decode(message);
            if (OtaPackageCode.OTA_0B.equals(deviceData.getNetModbusCode())) {
                otaTaskCache.setOtaCacheValue(bo.getSerialNumber(), "offset", "1", 5);
            } else if (OtaPackageCode.OTA_0A.equals(deviceData.getNetModbusCode())) {
                // 获取分包传输大小
                byte[] data = deviceData.getData();
                if (data == null || data.length != 1) {
                    return;
                }
                int size = 256;
                if ((int)data[0] == 1) {
                    size = 512;
                } else if ((int)data[0] == 2) {
                    size = 1024;
                }
                otaTaskCache.setOtaCacheValue(bo.getSerialNumber(), "packageSize", String.valueOf(size), 5);
            }
        }
        // HTTP固件升级回复
        else if (bo.getTopicName().endsWith(TopicType.HTTP_UPGRADE_REPLY.getTopicSuffix())) {
            String data = new String(bo.getData(), StandardCharsets.UTF_8);
            OtaReplyMessage replyMessage = com.alibaba.fastjson2.JSONObject.parseObject(data, OtaReplyMessage.class);
            OTAUpgrade otaUpgrade = OTAUpgrade.parse(replyMessage.getStatus());

            PushMessageBo appMessageBo = new PushMessageBo();
            appMessageBo.setTopic(topicsUtils.buildTopic(String.valueOf(replyMessage.getTaskId()), TopicType.WS_OTA_STATUS));
            com.alibaba.fastjson2.JSONObject wsMessage = new com.alibaba.fastjson2.JSONObject();
            wsMessage.put("serialNumber", bo.getSerialNumber());
            wsMessage.put("status", otaUpgrade.getStatus());
            wsMessage.put("timestamp", System.currentTimeMillis());
            if (replyMessage.getStatus() == 2) {
                wsMessage.put("progress", replyMessage.getProgress());
            }
            // WS消息发送前端
            appMessageBo.setMessage(wsMessage.toString());
            remoteManager.pushCommon(appMessageBo);
            Map<String, String> hMap = new HashMap<>();
            hMap.put("progress", String.valueOf(replyMessage.getProgress()));
            hMap.put("version", String.valueOf(replyMessage.getVersion()));
            hMap.put("status", String.valueOf(replyMessage.getStatus()));
            otaTaskCache.setOtaCache(bo.getSerialNumber(), hMap, 5);
            firmwareTaskDetailService.updateStatus(replyMessage.getTaskId(), bo.getSerialNumber(), otaUpgrade);
        }
    }

    /**
     * 构建消息
     *
     * @param bo
     */
    @Override
    public DeviceStatusVO buildReport(DeviceReportBo bo) {
        DeviceStatusVO deviceStatusVO = deviceService.selectDeviceStatusAndTransportStatus(bo.getSerialNumber());
        Optional.ofNullable(deviceStatusVO).orElseThrow(() -> new ServiceException("设备不存在"));
        //产品id
        bo.setProductId(deviceStatusVO.getProductId());
        return deviceStatusVO;
    }

    /**
     * 根据产品id获取协议处理器
     */
    @Override
    public IProtocol selectedProtocol(Long productId) {

        //查询产品获取协议编号
        String code = productService.getProtocolByProductId(productId);
        return protocolManagerService.getProtocolByProtocolCode(code);
    }

    /**
     * 处理网关设备
     *
     * @param message
     * @param topicName
     */
    /**
     * 处理网关设备
     *
     * @param message
     * @param topicName
     */
    private void processNoSub(DeviceReport message, String topicName) {

        if (message.getServerType().equals(ServerType.MQTT)) {
            //处理topic以prop结尾上报的数据 (属性)
            if (message.getServerType().equals(ServerType.MQTT)) {
                if (!topicName.endsWith(TopicType.PROPERTY_POST.getTopicSuffix())) {
                    return;
                }
            }
        }
        ReportDataBo report = new ReportDataBo();
        report.setSerialNumber(message.getSerialNumber());
        report.setProductId(message.getProductId());
        report.setDataList(message.getThingsModelSimpleItem());
        report.setType(1);
        report.setUserId(message.getUserId());
        report.setUserName(message.getUserName());
        report.setDeviceName(message.getDeviceName());
        report.setSources(message.getSources());
        report.setGwDeviceBo(message.getGwDeviceBo());
        //属性上报执行规则引擎
        report.setRuleEngine(true);
        log.info("--------------------------------------9----------------------------------");
        System.out.println(report.toString());
        dataHandler.reportData(report);
    }


    /**
     * 处理设备回调信息，此处按照topic区分 prop上报和设备回调reply，
     * 如果模组可订阅的topic有限，不能区分prop上报和reply，自行根据上报数据来区分
     * @param message
     */
    public void handlerDeviceReply(DeviceReport message) {
        String messageId = "";
        String sources = message.getSources();
        String serialNumber = message.getSerialNumber();
        String cacheKey = RedisKeyBuilder.buildDownMessageIdCacheKey(serialNumber);
        Set<String> functionList = redisCache.zRange(cacheKey, 0, -1);
        //从redis中获取messageId 流水号，获取下发记录
        for (String fun : functionList) {
            String[] split = fun.split(":");
            if (split[0].equals(sources)){
                messageId = split[1];
            }
            redisCache.zRem(cacheKey,fun);
        }
        FunctionLog functionLog = new FunctionLog();
        switch (message.getProtocolCode()){
            case FastBeeConstant.PROTOCOL.ModbusRtu:
            case FastBeeConstant.PROTOCOL.ModbusToJsonHP:
            case FastBeeConstant.PROTOCOL.ModbusRtuPak:
                //更新值
                functionLog.setResultCode(FunctionReplyStatus.SUCCESS.getCode());
                functionLog.setResultMsg(FunctionReplyStatus.SUCCESS.getMessage());
                functionLog.setReplyTime(DateUtils.getNowDate());
                functionLog.setMessageId(message.getMessageId() == null ? messageId : message.getMessageId());
                logService.updateByMessageId(functionLog);
                break;
        }
    }

    /**
     * 解析OTA升级回复消息,更新升级状态
     */
    private void otaUpgrade(DeviceReport message, String topicName) {


    }

    /**
     * 同步设备状态
     * @param
     */
    private void synchDeviceStatus(String serialNumber){
        //如果有数据上报，但是数据库设备状态为离线，则进行同步
        DeviceStatusVO deviceStatusVO = deviceService.selectDeviceStatusAndTransportStatus(serialNumber);
        if (deviceStatusVO.getStatus() == DeviceStatus.OFFLINE.getType()
                || deviceStatusVO.getStatus() == DeviceStatus.UNACTIVATED.getType()){
            DeviceStatusBo statusBo = new DeviceStatusBo();
            statusBo.setStatus(DeviceStatus.ONLINE);
            statusBo.setSerialNumber(deviceStatusVO.getSerialNumber());
            statusBo.setTimestamp(DateUtils.getNowDate());
            MessageProducer.sendStatusMsg(statusBo);

            //如果是子设备，维护子设备的状态到session
            if (deviceStatusVO.getDeviceType() == DeviceType.SUB_GATEWAY.getCode()) {
                Session session = new Session();
                session.setServerType(ServerType.MQTT);
                session.setClientId(deviceStatusVO.getSerialNumber());
                session.setLastAccessTime(DateUtils.getTimestamp());
                session.setConnected(true);
                sessionStore.storeSession(deviceStatusVO.getSerialNumber(), session);
            }
        }else if (deviceStatusVO.getStatus() == DeviceStatus.ONLINE.getType() ){
            if (deviceStatusVO.getDeviceType() == DeviceType.SUB_GATEWAY.getCode()){
                //如果是在线,则更新session在线时间
                Session session = sessionStore.getSession(deviceStatusVO.getSerialNumber());
                if (session!= null) {
                    session.setLastAccessTime(DateUtils.getTimestamp());
                    sessionStore.storeSession(deviceStatusVO.getSerialNumber(), session);
                }
            }

        }


    }

}
