package com.fastbee.iot.data.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.config.RuoYiConfig;
import com.fastbee.common.constant.FastBeeConstant;
import com.fastbee.common.core.mq.message.DeviceData;
import com.fastbee.common.core.mq.ota.OtaUpgradeBo;
import com.fastbee.common.core.ota.OtaPackageCode;
import com.fastbee.common.enums.OTAUpgrade;
import com.fastbee.common.enums.TopicType;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.common.utils.file.FileUtils;
import com.fastbee.common.utils.gateway.mq.TopicsUtils;
import com.fastbee.common.utils.ip.IpUtils;
import com.fastbee.iot.cache.IOtaTaskCache;
import com.fastbee.iot.cache.ITSLValueCache;
import com.fastbee.iot.data.service.IFirmwareTaskDetailService;
import com.fastbee.iot.data.service.IOtaTaskUpgradeService;
import com.fastbee.iot.domain.Firmware;
import com.fastbee.mq.producer.MessageProducer;
import com.fastbee.mqtt.manager.MqttRemoteManager;
import com.fastbee.mqtt.model.PushMessageBo;
import com.fastbee.protocol.base.protocol.IProtocol;
import com.fastbee.protocol.service.IProtocolManagerService;
import com.fastbee.protocol.util.IntegerToByteUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * OTA异步升级实现
 */
@Service
@Slf4j
public class OtaTaskUpgradeServiceImpl implements IOtaTaskUpgradeService {

    @Resource
    private ITSLValueCache itslValueCache;
    @Resource
    private IFirmwareTaskDetailService firmwareTaskDetailService;
    @Resource
    private MqttRemoteManager remoteManager;
    @Resource
    private IProtocolManagerService protocolManagerService;
    @Resource
    private IOtaTaskCache otaTaskCache;
    @Resource
    private TopicsUtils topicsUtils;

    @Value("${server.port}")
    private String serverPort;

    /**
     * 固件升级异步方法
     * @param taskId
     * @param serialNumber
     * @param firmware
     */
    @Override
    @Async(FastBeeConstant.TASK.OTA_THREAD_POOL)
    public void upgrade(Long taskId, String serialNumber, Firmware firmware) {
        // 前端WS消息体
        PushMessageBo appMessageBo = new PushMessageBo();
        appMessageBo.setTopic(topicsUtils.buildTopic(String.valueOf(taskId), TopicType.WS_OTA_STATUS));

        // 判断当前设备是否处于升级状态
        if (otaTaskCache.checkOtaCacheExist(serialNumber)) {
            // 状态为升级中，则返回
            firmwareTaskDetailService.updateStatus(taskId, serialNumber, OTAUpgrade.STOP);
            appMessageBo.setMessage(createWsMessage(serialNumber, OTAUpgrade.STOP));
            remoteManager.pushCommon(appMessageBo);
            return;
        }
        otaTaskCache.removeOtaCache(serialNumber);
        if (!MqttRemoteManager.checkDeviceStatus(serialNumber)) {
            // 设备离线返回
            firmwareTaskDetailService.updateStatus(taskId, serialNumber, OTAUpgrade.STOP_OFFLINE);
            appMessageBo.setMessage(createWsMessage(serialNumber, OTAUpgrade.STOP_OFFLINE));
            remoteManager.pushCommon(appMessageBo);
            return;
        }
        appMessageBo.setMessage(createWsMessage(serialNumber, OTAUpgrade.AWAIT));
        remoteManager.pushCommon(appMessageBo);
        // MCU固件升级
        if (firmware.getFirmwareType() == 1) {
            upgradeFetchPackage(taskId, serialNumber, firmware, appMessageBo);
        }
        // WIFI模组固件升级
        else if (firmware.getFirmwareType() == 2) {
            upgradeHttp(taskId, serialNumber, firmware, appMessageBo);
        }
    }

    /**
     * 分包拉取方式固件升级
     * @param taskId
     * @param serialNumber
     * @param firmware
     */
    private void upgradeFetchPackage(Long taskId, String serialNumber, Firmware firmware, PushMessageBo appMessageBo) {
        // 固件包准备
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            FileUtils.writeBytes(RuoYiConfig.getProfile() + firmware.getFilePath().replace("/profile", ""), outputStream);
        } catch (IOException e) {
            upgradeReturn(taskId, serialNumber, appMessageBo, OTAUpgrade.FAILED_PULL);
            throw new RuntimeException(e);
        }
        byte[] fileBytes = outputStream.toByteArray();
        // 若获取包失败，返回
        if (fileBytes.length == 0) {
            upgradeReturn(taskId, serialNumber, appMessageBo, OTAUpgrade.FAILED_PULL);
            return;
        }
        // 获取分包大小
        DeviceData deviceData = DeviceData.builder()
                .serialNumber(serialNumber)
                .netModbusCode(OtaPackageCode.OTA_0A)
                .bitCount(4)
                .data(IntegerToByteUtil.intToBytes2(fileBytes.length))
                .build();
        IProtocol protocol = protocolManagerService.getProtocolByProtocolCode(FastBeeConstant.PROTOCOL.NetOTA);
        int size = 0;
        try {
            size = getPackageUpgradeSize(serialNumber, protocol.encode(deviceData));
        } catch (InterruptedException e) {
            upgradeReturn(taskId, serialNumber, appMessageBo, OTAUpgrade.FAILED_PACKAGE_SIZE);
            throw new RuntimeException(e);
        }
        if (size == 0) {
            upgradeReturn(taskId, serialNumber, appMessageBo, OTAUpgrade.FAILED_PACKAGE_SIZE);
            return;
        }
        // 发送固件包
        appMessageBo.setMessage(createWsMessage(serialNumber, OTAUpgrade.SEND));
        remoteManager.pushCommon(appMessageBo);
        firmwareTaskDetailService.updateStatus(taskId, serialNumber, OTAUpgrade.SEND);
        deviceData.setNetModbusCode(OtaPackageCode.OTA_0B);
        // 获取总发送包数，向上取整
        int count = fileBytes.length / size + (fileBytes.length % size != 0 ? 1 : 0);
        byte[] data = new byte[size + 4];
        deviceData.setBitCount(size + 4);
        deviceData.setData(data);
        for (int i = 0; i < count; i++) {
            // 最后一包，数据长度可能不一致
            if ((i == count - 1) && (fileBytes.length % size != 0)) {
                int bitCount = fileBytes.length % size;
                byte[] dataEnd = new byte[bitCount + 4];
                deviceData.setBitCount(bitCount + 4);
                deviceData.setData(dataEnd);
                System.arraycopy(IntegerToByteUtil.intToBytes(i), 0, dataEnd, 0, 4);
                System.arraycopy(fileBytes, i * size, dataEnd, 4, bitCount);
            } else {
                System.arraycopy(IntegerToByteUtil.intToBytes(i), 0, data, 0, 4);
                System.arraycopy(fileBytes, i * size, data, 4, size);
            }
            boolean sendStatus = false;
            try {
                sendStatus = sendPackageUpgradeMsg(serialNumber, protocol.encode(deviceData));
            } catch (InterruptedException e) {
                upgradeReturn(taskId, serialNumber, appMessageBo, OTAUpgrade.FAILED_PUSH);
                throw new RuntimeException(e);
            }
            if (!sendStatus) {
                upgradeReturn(taskId, serialNumber, appMessageBo, OTAUpgrade.FAILED_PUSH);
                return;
            }
            // ws更新升级进度
            int progress = ((i + 1) * 100) / count;
            if (progress > ((i * 100) / count)) {
                otaTaskCache.setOtaCacheValue(serialNumber, "progress", String.valueOf(progress), 5);
                appMessageBo.setMessage(createWsMessage(serialNumber, OTAUpgrade.SEND, String.valueOf(progress)));
                remoteManager.pushCommon(appMessageBo);
            }
        }
        appMessageBo.setMessage(createWsMessage(serialNumber, OTAUpgrade.REPLY));
        firmwareTaskDetailService.updateStatus(taskId, serialNumber, OTAUpgrade.REPLY);
        // 升级结果
        boolean upgradeStatus = false;
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 180000) {
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                upgradeReturn(taskId, serialNumber, appMessageBo, OTAUpgrade.FAILED);
                throw new RuntimeException(e);
            }
            // 从缓存读取数据   ！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！注意这里默认设备上传的版本物模型是version
            String version = itslValueCache.getCacheIdentifierValue(firmware.getProductId(), serialNumber, "version");
            if (firmware.getVersion().compareTo(new BigDecimal(version)) == 0) {
                upgradeStatus = true;
                break;
            }
        }
        if (upgradeStatus) {
            // 升级成功
            upgradeReturn(taskId, serialNumber, appMessageBo, OTAUpgrade.SUCCESS);
        } else {
            // 升级失败
            upgradeReturn(taskId, serialNumber, appMessageBo, OTAUpgrade.FAILED);
        }
    }

    /**
     * HTTP方式固件升级
     * @param taskId
     * @param serialNumber
     * @param firmware
     */
    private void upgradeHttp(Long taskId, String serialNumber, Firmware firmware, PushMessageBo appMessageBo) {
        // 发送固件包
        OtaUpgradeBo bo = OtaUpgradeBo.builder()
                .taskId(taskId)
                .serialNumber(serialNumber)
                .topicName(TopicType.HTTP_FIRMWARE_SET.getTopicSuffix())
                .url(firmware.getFilePath())
                .version(firmware.getVersion())
                .status(OTAUpgrade.SEND.getStatus())
                .build();
        MessageProducer.sendOtaUpgradeMsg(bo);
        // 数据库与WS更新状态
        appMessageBo.setMessage(createWsMessage(serialNumber, OTAUpgrade.SEND));
        remoteManager.pushCommon(appMessageBo);
        firmwareTaskDetailService.updateStatus(taskId, serialNumber, OTAUpgrade.SEND);
    }

    /**
     * 升级失败或结束返回
     * @param taskId
     * @param serialNumber
     * @param appMessageBo
     * @param otaUpgrade
     */
    private void upgradeReturn(Long taskId, String serialNumber, PushMessageBo appMessageBo, OTAUpgrade otaUpgrade) {
        appMessageBo.setMessage(createWsMessage(serialNumber, otaUpgrade));
        remoteManager.pushCommon(appMessageBo);
        firmwareTaskDetailService.updateStatus(taskId, serialNumber, otaUpgrade);
        otaTaskCache.removeOtaCache(serialNumber);
    }

    /**
     * 生成WS消息体
     * @param serialNumber
     * @param otaUpgrade
     * @param progress
     * @return
     */
    private String createWsMessage(String serialNumber, OTAUpgrade otaUpgrade, String progress) {
        JSONObject wsMessage = new JSONObject();
        wsMessage.put("serialNumber", serialNumber);
        wsMessage.put("status", otaUpgrade.getStatus());
        wsMessage.put("timestamp", System.currentTimeMillis());
        if (!StringUtils.isEmpty(progress)) {
            wsMessage.put("progress", progress);
        }
        return wsMessage.toString();
    }

    private String createWsMessage(String serialNumber, OTAUpgrade otaUpgrade) {
        return createWsMessage(serialNumber, otaUpgrade, "");
    }

    /**
     * 获取拆包升级分包大小
     * @param serialNumber
     * @param msg
     * @return
     */
    private int getPackageUpgradeSize(String serialNumber, byte[] msg) throws InterruptedException {
        OtaUpgradeBo bo = OtaUpgradeBo.builder()
                .serialNumber(serialNumber)
                .topicName(TopicType.FETCH_FIRMWARE_SET.getTopicSuffix())
                .msg(msg)
                .build();
        int count = 0;
        while (count < 3) {
            // 发送消息
            MessageProducer.sendOtaUpgradeMsg(bo);
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 5000) {
                Thread.sleep(300);
                // 从缓存读取数据
                String packageSize = otaTaskCache.getOtaCacheValue(serialNumber, "packageSize");
                if (StringUtils.isNotEmpty(packageSize)) {
                    return Integer.parseInt(packageSize);
                }
            }
            count ++;
        }
        return 0;
    }

    /**
     * 发送分包升级消息到队列中
     * @param msg
     */
    private boolean sendPackageUpgradeMsg(String serialNumber, byte[] msg) throws InterruptedException {
        OtaUpgradeBo bo = OtaUpgradeBo.builder()
                .serialNumber(serialNumber)
                .topicName(TopicType.FETCH_FIRMWARE_SET.getTopicSuffix())
                .msg(msg)
                .build();
        int count = 0;
        while (count < 3) {
            // 发送消息
            MessageProducer.sendOtaUpgradeMsg(bo);
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 5000) {
                Thread.sleep(300);
                // 从缓存读取数据
                String offset = otaTaskCache.getOtaCacheValue(serialNumber, "offset");
                if ("1".equals(offset)) {
                    otaTaskCache.setOtaCacheValue(serialNumber, "offset", "0", 5);
                    return true;
                }
            }
            count ++;
        }
        return false;
    }
}
