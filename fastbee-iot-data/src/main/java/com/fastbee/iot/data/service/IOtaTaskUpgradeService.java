package com.fastbee.iot.data.service;


import com.fastbee.iot.domain.Firmware;

/**
 * OTA异步升级
 */
public interface IOtaTaskUpgradeService {

    /**
     * 固件升级异步方法
     * @param taskId
     * @param serialNumber
     * @param firmware
     */
    public void upgrade(Long taskId, String serialNumber, Firmware firmware);

}
