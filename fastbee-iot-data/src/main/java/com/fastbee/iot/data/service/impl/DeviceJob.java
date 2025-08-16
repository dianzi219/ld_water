package com.fastbee.iot.data.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.core.mq.DeviceStatusBo;
import com.fastbee.common.core.notify.AlertPushParams;
import com.fastbee.common.enums.DeviceStatus;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.iot.domain.*;
import com.fastbee.iot.model.AlertSceneSendVO;
import com.fastbee.iot.service.*;
import com.fastbee.mq.producer.MessageProducer;
import com.fastbee.notify.core.service.NotifySendService;
import com.fastbee.sip.domain.SipDevice;
import com.fastbee.sip.domain.SipDeviceChannel;
import com.fastbee.sip.enums.DeviceChannelStatus;
import com.fastbee.sip.mapper.SipDeviceChannelMapper;
import com.fastbee.sip.mapper.SipDeviceMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author gsb
 * @date 2023/2/20 17:13
 */
@Component
@Slf4j
public class DeviceJob {

    @Resource
    private IDeviceService deviceService;

    @Autowired
    private SipDeviceMapper sipDeviceMapper;

    @Autowired
    private SipDeviceChannelMapper sipDeviceChannelMapper;

    @Autowired
    private IDeviceLogService deviceLogService;

    @Autowired
    private IAlertService alertService;

    @Autowired
    private NotifySendService notifySendService;

    @Autowired
    private IAlertLogService alertLogService;

    @Autowired
    private IDeviceAlertUserService deviceAlertUserService;

    @Autowired
    private ISceneScriptService sceneScriptService;

    public void updateSipDeviceOnlineStatus(Integer timeout) {
        List<SipDevice> devs = sipDeviceMapper.selectOfflineSipDevice(timeout);
        devs.forEach(item -> {
            if (!Objects.equals(item.getDeviceSipId(), "")) {
                //更新iot设备状态
                Device dev = deviceService.selectDeviceBySerialNumber(item.getDeviceSipId());
                if (dev != null && dev.getStatus() == DeviceStatus.ONLINE.getType()) {
                    log.warn("定时任务：=>设备:{} 已经下线，设备超过{}秒没上线！",item.getDeviceSipId(),timeout);
                    dev.setStatus(DeviceStatus.OFFLINE.getType());
                    deviceService.updateDeviceStatusAndLocation(dev,"");
                    DeviceStatusBo bo = DeviceStatusBo.builder()
                            .serialNumber(dev.getSerialNumber())
                            .status(DeviceStatus.OFFLINE)
                            .build();
                    MessageProducer.sendStatusMsg(bo);
                }
                //更新通道状态
                List<SipDeviceChannel> channels = sipDeviceChannelMapper.selectSipDeviceChannelByDeviceSipId(item.getDeviceSipId());
                channels.forEach(citem -> {
                    citem.setStatus(DeviceChannelStatus.offline.getValue());
                    sipDeviceChannelMapper.updateSipDeviceChannel(citem);
                });
            }
        });
    }

    /**
     * 检查设备上报日志
     *
     * @param cycle 设备上报日志周期（分钟）
     * @return void
     */

    public void checkDeviceReportData(String TriggerId, Integer cycle) {
        Calendar calendar = Calendar.getInstance();
        Date now = new Date();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -cycle);
        deviceLogService.selectDeviceReportData(calendar.getTime(), now).forEach(item -> {
            if (!item.getUnReportList().isEmpty()) {
                item.getUnReportList().forEach(s -> {
                    //产生告警 设备序列号 和 id无数据
                    alertPush(TriggerId, item.getSerialNumber(), s);
                });
            }
        });
    }

    void alertPush(String TriggerId, String serialNumber, String identity) {
        // 获取告警场景id
        SceneScript triggerScript = new SceneScript();
        SceneScript alertlog = new SceneScript();
        triggerScript.setId(TriggerId);
        List<SceneScript> list = sceneScriptService.selectSceneScriptList(triggerScript);
        if (!list.isEmpty()) {
            triggerScript = list.get(0);
            alertlog.setId(identity);
            alertlog.setValue("无数据上报");
            alertlog.setSceneId(triggerScript.getSceneId());
        } else {
            return;
        }

        List<AlertLog> alertLogList = new ArrayList<>();
        // 查询设备信息
        Device device = deviceService.selectDeviceBySerialNumber(serialNumber);
        Optional.ofNullable(device).orElseThrow(() -> new ServiceException("告警推送，设备不存在" + "[{" + serialNumber + "}]"));
        // 获取场景相关的告警参数，告警必须要是启动状态
        List<AlertSceneSendVO> sceneSendVOList = alertService.listByAlertIds(alertlog.getSceneId());
        if (CollectionUtils.isEmpty(sceneSendVOList)) {
            return;
        }
        // 获取告警推送参数
        AlertPushParams alertPushParams = new AlertPushParams();
        alertPushParams.setDeviceName(device.getDeviceName());
        alertPushParams.setSerialNumber(serialNumber);
        // 多租户改版查询自己配置的告警用户
        DeviceAlertUser deviceAlertUser = new DeviceAlertUser();
        deviceAlertUser.setDeviceId(device.getDeviceId());
        List<DeviceAlertUser> deviceUserList = deviceAlertUserService.selectDeviceAlertUserList(deviceAlertUser);
        if (CollectionUtils.isNotEmpty(deviceUserList)) {
            alertPushParams.setUserPhoneSet(deviceUserList.stream().map(DeviceAlertUser::getPhoneNumber).filter(StringUtils::isNotEmpty).collect(Collectors.toSet()));
            alertPushParams.setUserIdSet(deviceUserList.stream().map(DeviceAlertUser::getUserId).collect(Collectors.toSet()));
        }
        String address;
        if (StringUtils.isNotEmpty(device.getNetworkAddress())) {
            address = device.getNetworkAddress();
        } else if (StringUtils.isNotEmpty(device.getNetworkIp())) {
            address = device.getNetworkIp();
        } else if (Objects.nonNull(device.getLongitude()) && Objects.nonNull(device.getLatitude())) {
            address = device.getLongitude() + "," + device.getLatitude();
        } else {
            address = "未知地点";
        }
        alertPushParams.setAddress(address);
        alertPushParams.setAlertTime(DateUtils.parseDateToStr(DateUtils.YY_MM_DD_HH_MM_SS, new Date()));
        // 获取告警关联模版id
        for (AlertSceneSendVO alertSceneSendVO : sceneSendVOList) {
            List<AlertNotifyTemplate> alertNotifyTemplateList = alertService.listAlertNotifyTemplate(alertSceneSendVO.getAlertId());
            alertPushParams.setAlertName(alertSceneSendVO.getAlertName());
            for (AlertNotifyTemplate alertNotifyTemplate : alertNotifyTemplateList) {
                alertPushParams.setNotifyTemplateId(alertNotifyTemplate.getNotifyTemplateId());
                notifySendService.alertSend(alertPushParams);
            }
            List<AlertLog> alist = alertLogService.selectAlertLogListByCreateBy(alertlog.getSceneId().toString(), alertlog.getId(), 2);
            if (alist.isEmpty()) {
                AlertLog alertLog = buildAlertLog(alertSceneSendVO, device, alertlog);
                alertLogList.add(alertLog);
            } else {
                //重复未处理告警，只更新告警发生时间
                for (AlertLog alertLog : alist) {
                    alertLog.setCreateTime(new Date());
                    alertLogService.updateAlertLog(alertLog);
                }
            }
        }
        // 保存告警日志
        alertLogService.insertAlertLogBatch(alertLogList);
    }

    private AlertLog buildAlertLog(AlertSceneSendVO alertSceneSendVO, Device device, SceneScript Item) {
        AlertLog alertLog = new AlertLog();
        alertLog.setAlertName(alertSceneSendVO.getAlertName());
        alertLog.setAlertLevel(alertSceneSendVO.getAlertLevel());
        alertLog.setSerialNumber(device.getSerialNumber());
        alertLog.setProductId(device.getProductId());
        alertLog.setDeviceName(device.getDeviceName());
        alertLog.setUserId(device.getTenantId());
        alertLog.setStatus(2);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", Item.getId());
        jsonObject.put("value", Item.getValue());
        jsonObject.put("remark", "");
        alertLog.setDetail(jsonObject.toJSONString());
        alertLog.setCreateBy(Item.getSceneId().toString());
        alertLog.setRemark(Item.getId());
        alertLog.setCreateTime(new Date());
        return alertLog;
    }
}
