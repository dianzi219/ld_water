package com.fastbee.iot.model;

import com.fastbee.common.core.domain.BaseEntity;

import java.util.Date;
import java.util.List;

/**
 * 固件升级详情页面设备数量统计
 * @author kami
 */
public class FirmwareTaskDeviceStatistic{

    /** 设备数量 **/
    private int deviceCount;


    /**
     * 0:等待升级 1:已发送设备 2:设备收到 ===> 正在升级
     * 3:升级成功 ===> 升级成功
     * 4:升级失败 ===> 升级失败
     *
     * */
    private Integer upgradeStatus;

    public int getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
    }

    public Integer getUpgradeStatus() {
        return upgradeStatus;
    }

    public void setUpgradeStatus(Integer upgradeStatus) {
        this.upgradeStatus = upgradeStatus;
    }
}
