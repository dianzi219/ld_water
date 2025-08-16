package com.fastbee.iot.data.job;

import com.fastbee.base.session.Session;
import com.fastbee.common.enums.DeviceStatus;
import com.fastbee.iot.data.service.IMqttMessagePublish;
import com.fastbee.iot.domain.Device;
import com.fastbee.iot.model.DeviceStatusVO;
import com.fastbee.iot.service.IDeviceService;
import com.fastbee.mqtt.manager.SessionManger;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 定时同步设备状态 -- netty版本mqtt使用
 * @author gsb
 * @date 2024/4/11 10:33
 */
@Component
public class SyncDeviceStatusJob {

    @Resource
    private IDeviceService deviceService;
    @Resource
    private IMqttMessagePublish mqttMessagePublish;
    @Value("${server.broker.enabled}")
    private Boolean enabled;

    /**
     * 定期同步设备状态
     *  1.将异常在线设备变更为离线状态
     *  2.将离线设备但实际在线设备变更为在线
     */
    public void syncDeviceStatus() {
        if (enabled) {
            //获取所有已激活并不是禁用的设备
            List<DeviceStatusVO> deviceStatusVOList = deviceService.selectDeviceActive();
            if (!CollectionUtils.isEmpty(deviceStatusVOList)) {
                for (DeviceStatusVO statusVO : deviceStatusVOList) {
                    Session session = SessionManger.getSession(statusVO.getSerialNumber());
                    Device device = new Device();
                    device.setSerialNumber(statusVO.getSerialNumber());
                    device.setRssi(statusVO.getRssi());
                    device.setIsShadow(statusVO.getIsShadow());
                    // 如果session中设备在线，数据库状态离线 ,则更新设备的状态为在线
                    if (!Objects.isNull(session) && statusVO.getStatus() == DeviceStatus.OFFLINE.getType()) {
                        device.setStatus(DeviceStatus.ONLINE.getType());
                        deviceService.updateDeviceStatus(device);
                        mqttMessagePublish.pushDeviceStatus(device, DeviceStatus.ONLINE);
                    }
                    if (Objects.isNull(session) && statusVO.getStatus() == DeviceStatus.ONLINE.getType()) {
                        device.setStatus(DeviceStatus.OFFLINE.getType());
                        deviceService.updateDeviceStatus(device);
                        mqttMessagePublish.pushDeviceStatus(device, DeviceStatus.OFFLINE);
                    }
                }
            }
        }
    }
}
