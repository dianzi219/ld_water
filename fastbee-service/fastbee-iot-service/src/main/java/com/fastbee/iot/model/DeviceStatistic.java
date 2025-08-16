package com.fastbee.iot.model;

/**
 * id和name
 *
 * @author kerwincui
 * @date 2021-12-16
 */
public class DeviceStatistic
{
    /** 设备数量 **/
    private int deviceCount;

    /** 设备数量 **/
    private int deviceOnlineCount;

    /** 产品数量 **/
    private int productCount;

    /** 告警 **/
    private long alertCount;

    /** 属性上报 **/
    private long propertyCount;

    /** 功能上报 **/
    private long functionCount;

    /** 事件上报 **/
    private long eventCount;

    /** 监测数据上报 **/
    private long monitorCount;

    /** 告警设备数量 **/
    private long alertDeviceCount;

    /** 设备离线数量 **/
    private int deviceOfflineCount;

    /** 告警未处理数量 **/
    private long alertNotProcessedCount;

    /** 告警已处理数量 **/
    private long alertProcessedCount;

    public long getAlertNotProcessedCount() {
        return alertNotProcessedCount;
    }

    public void setAlertNotProcessedCount(long alertNotProcessedCount) {
        this.alertNotProcessedCount = alertNotProcessedCount;
    }

    public long getAlertProcessedCount() {
        return alertProcessedCount;
    }

    public void setAlertProcessedCount(long alertProcessedCount) {
        this.alertProcessedCount = alertProcessedCount;
    }


    public long getAlertDeviceCount() {
        return alertDeviceCount;
    }

    public void setAlertDeviceCount(long alertDeviceCount) {
        this.alertDeviceCount = alertDeviceCount;
    }

    public int getDeviceOfflineCount() {
        return deviceOfflineCount;
    }

    public void setDeviceOfflineCount(int deviceOfflineCount) {
        this.deviceOfflineCount = deviceOfflineCount;
    }

    public int getDeviceOnlineCount() {
        return deviceOnlineCount;
    }

    public void setDeviceOnlineCount(int deviceOnlineCount) {
        this.deviceOnlineCount = deviceOnlineCount;
    }

    public long getMonitorCount() {
        return monitorCount;
    }

    public void setMonitorCount(long monitorCount) {
        this.monitorCount = monitorCount;
    }

    public int getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
    }

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }

    public long getAlertCount() {
        return alertCount;
    }

    public void setAlertCount(long alertCount) {
        this.alertCount = alertCount;
    }

    public long getPropertyCount() {
        return propertyCount;
    }

    public void setPropertyCount(long propertyCount) {
        this.propertyCount = propertyCount;
    }

    public long getFunctionCount() {
        return functionCount;
    }

    public void setFunctionCount(long functionCount) {
        this.functionCount = functionCount;
    }

    public long getEventCount() {
        return eventCount;
    }

    public void setEventCount(long eventCount) {
        this.eventCount = eventCount;
    }
}
