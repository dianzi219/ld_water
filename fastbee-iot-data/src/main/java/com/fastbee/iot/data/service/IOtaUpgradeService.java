package com.fastbee.iot.data.service;

import com.fastbee.common.core.mq.ota.OtaUpgradeDelayTask;

/**
 * OTA升级
 * @author bill
 */
public interface IOtaUpgradeService {

    /**
     * OTA延迟升级任务发送
     * @param task
     */
    public void upgrade(OtaUpgradeDelayTask task);

}
