package com.fastbee.iot.model;

/**
 * 动作
 * @author kerwincui
 * @date 2021-12-16
 */
public class Action
{
    /** 标识符 */
    private String id;

    /** 名称 */
    private String name;

    /** 值 */
    private String value;

    /** 类型：1=属性重发布，2=功能调用，3=事件发布，4=时序日志存储，5=Mysql日志存储，6=延时 */
    private Integer type;

    private Long triggerId;

    /** 产品ID */
    private Long productId;

    /** 产品名称 */
    private String productName;

    /** 设备ID */
    private Long deviceId;

    /** 设备名称 */
    private String deviceName;

    /** 设备编号 */
    private String serialNumber;

    /**
     * 父物模型id
     */
    private String modelId;

    /**
     * 数组或数组对象值索引,用来区分是数组、数组对象
     */
    private String modelIndex;

    /**
     * 子物模型id
     */
    private String subId;

    /**
     * 父物模显示名称
     */
    private String modelName;

    /**
     * 索引名称
     */
    private String modelIndexName;

    /**
     * 子物模名称
     */
    private String subName;

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelIndexName() {
        return modelIndexName;
    }

    public void setModelIndexName(String modelIndexName) {
        this.modelIndexName = modelIndexName;
    }

    public String getSubName() {
        return subName;
    }

    public void setSubName(String subName) {
        this.subName = subName;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getModelIndex() {
        return modelIndex;
    }

    public void setModelIndex(String modelIndex) {
        this.modelIndex = modelIndex;
    }

    public String getSubId() {
        return subId;
    }

    public void setSubId(String subId) {
        this.subId = subId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

}
