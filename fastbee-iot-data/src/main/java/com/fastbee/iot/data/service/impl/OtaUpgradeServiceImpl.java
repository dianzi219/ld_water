package com.fastbee.iot.data.service.impl;

import com.fastbee.common.core.mq.ota.OtaUpgradeDelayTask;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.iot.data.service.IOtaTaskUpgradeService;
import com.fastbee.iot.data.service.IOtaUpgradeService;
import com.fastbee.iot.domain.Device;
import com.fastbee.iot.domain.Firmware;
import com.fastbee.iot.service.IDeviceService;
import com.fastbee.iot.service.IFirmwareService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * OTA延迟升级实现
 *
 * @author bill
 */
@Service
@Slf4j
public class OtaUpgradeServiceImpl implements IOtaUpgradeService {

    @Autowired
    private IFirmwareService firmwareService;
    @Autowired
    private IDeviceService deviceService;
    @Resource
    private IOtaTaskUpgradeService otaTaskUpgradeService;



    @Override
    public void upgrade(OtaUpgradeDelayTask task) {
        //查询固件版本信息
        Firmware firmware = firmwareService.getById(task.getFirmwareId());
        Optional.ofNullable(firmware).orElseThrow(() -> new ServiceException("固件版本不存在!"));
        // 处理单个或多个设备的OTA升级，根据设备单个升级
        List<String> deviceList = task.getDevices();
        // 根据产品查询设备整个产品升级处理
        if (task.getUpgradeType() == 1) {
            deviceList = deviceService.selectSerialNumbersByProductId(firmware.getProductId());
        }
        if (CollectionUtils.isEmpty(deviceList)) {
            return;
        }
        for (String serialNumber : deviceList) {
            // 多线程执行升级
            otaTaskUpgradeService.upgrade(task.getTaskId(), serialNumber, firmware);
        }
    }

}
