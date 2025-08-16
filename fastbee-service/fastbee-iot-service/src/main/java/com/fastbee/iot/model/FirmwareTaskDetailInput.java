package com.fastbee.iot.model;

import com.fastbee.common.core.domain.BaseEntity;

import java.util.Date;
import java.util.List;

/**
 * 固件详情页面查询实体类
 * @author kami
 */
public class FirmwareTaskDetailInput extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 任务主键 */
    private Long taskId;

    /** 关联固件ID */
    private Long firmwareId;

    /** 设备名称 */
    private String deviceName;

    /** 设备序列号 */
    private String serialNumber;


    /** 0:等待升级 1:已发送设备 2:设备收到  3:升级成功 4:升级失败 */
    private Integer upgradeStatus;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getFirmwareId() {
        return firmwareId;
    }

    public void setFirmwareId(Long firmwareId) {
        this.firmwareId = firmwareId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Integer getUpgradeStatus() {
        return upgradeStatus;
    }

    public void setUpgradeStatus(Integer upgradeStatus) {
        this.upgradeStatus = upgradeStatus;
    }
}
