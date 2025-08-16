package com.fastbee.iot.model;

import com.fastbee.common.core.domain.BaseEntity;

import java.util.Date;
import java.util.List;

/**
 * 创建固件升级任务对象对象 iot_firmware_task
 *
 * @author kami
 */
public class FirmwareTaskInput extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 任务名称 */
    private String taskName;

    /** 关联固件ID */
    private Long firmwareId;

    /** 1:产品固件版本 2:指定设备 */
    private Long upgradeType;

    /** 升级任务描述 */
    private String taskDesc;

    /** 选中的设备总数 */
    private Long deviceAmount;

    /** 预定时间升级 */
    private Date bookTime;

    /** 设备序列号列表 */
    private List<String> deviceList;

    private Long productId;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Long getFirmwareId() {
        return firmwareId;
    }

    public void setFirmwareId(Long firmwareId) {
        this.firmwareId = firmwareId;
    }

    public Long getUpgradeType() {
        return upgradeType;
    }

    public void setUpgradeType(Long upgradeType) {
        this.upgradeType = upgradeType;
    }

    public String getTaskDesc() {
        return taskDesc;
    }

    public void setTaskDesc(String taskDesc) {
        this.taskDesc = taskDesc;
    }

    public Long getDeviceAmount() {
        return deviceAmount;
    }

    public void setDeviceAmount(Long deviceAmount) {
        this.deviceAmount = deviceAmount;
    }

    public Date getBookTime() {
        return bookTime;
    }

    public void setBookTime(Date bookTime) {
        this.bookTime = bookTime;
    }

    public List<String> getDeviceList() {
        return deviceList;
    }

    public void setDeviceList(List<String> deviceList) {
        this.deviceList = deviceList;
    }
}
