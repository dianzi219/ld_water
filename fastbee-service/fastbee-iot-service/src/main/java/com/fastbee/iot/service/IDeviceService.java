package com.fastbee.iot.service;

import com.fastbee.common.core.device.DeviceAndProtocol;
import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.core.thingsModel.ThingsModelSimpleItem;
import com.fastbee.common.enums.DeviceStatus;
import com.fastbee.iot.domain.Device;
import com.fastbee.iot.domain.DeviceGroup;
import com.fastbee.iot.model.*;
import com.fastbee.iot.model.ThingsModels.ThingsModelShadow;
import com.fastbee.common.core.thingsModel.ThingsModelValuesInput;
import com.fastbee.iot.model.dto.ThingsModelDTO;
import org.quartz.SchedulerException;

import java.util.List;

/**
 * 设备Service接口
 *
 * @author kerwincui
 * @date 2021-12-16
 */
public interface IDeviceService
{
    /**
     * 查询设备
     *
     * @param deviceId 设备主键
     * @return 设备
     */
    public Device selectDeviceByDeviceId(Long deviceId);

    /**
     * 查询设备统计信息
     *
     * @return 设备
     */
    public DeviceStatistic selectDeviceStatistic();

    /**
     * 根据设备编号查询设备
     *
     * @param serialNumber 设备主键
     * @return 设备
     */
    public Device selectDeviceBySerialNumber(String serialNumber);

    /**
     * 根据设备编号查询协议编号
     * @param serialNumber 设备编号
     * @return 协议编号
     */
    public ProductCode getProtocolBySerialNumber(String serialNumber);

    /**
     * 根据设备编号查询简洁设备
     *
     * @param serialNumber 设备主键
     * @return 设备
     */
    public Device selectShortDeviceBySerialNumber(String serialNumber);

    /**
     * 根据设备编号查询设备认证信息
     *
     * @param model 设备编号和产品ID
     * @return 设备
     */
    public ProductAuthenticateModel selectProductAuthenticate(AuthenticateInputModel model);

    /**
     * 查询设备和运行状态
     *
     * @param deviceId 设备主键
     * @return 设备
     */
    public DeviceShortOutput selectDeviceRunningStatusByDeviceId(Long deviceId);

    /**
     * 上报设备的物模型
     * @param input
     * @return
     */
    public List<ThingsModelSimpleItem> reportDeviceThingsModelValue(ThingsModelValuesInput input, int type, boolean isShadow);

    /**
     * 查询设备列表
     *
     * @param device 设备
     * @return 设备集合
     */
    public List<Device> selectDeviceList(Device device);

    /**
     * 查询未分配授权码设备列表
     *
     * @param device 设备
     * @return 设备集合
     */
    public List<Device> selectUnAuthDeviceList(Device device);

    /**
     * 查询分组可添加设备分页列表
     *
     * @param device 设备
     * @return 设备集合
     */
    public List<Device> selectDeviceListByGroup(Device device);

    /**
     * 查询所有设备简短列表
     *
     * @return 设备
     */
    public List<DeviceAllShortOutput> selectAllDeviceShortList(Device device);

    /**
     * 根据产品ID查询产品下所有设备的编号
     *
     * @return 设备集合
     */
    public List<String> selectSerialNumberByProductId(Long productId);

    /**
     * 获取产品下面的设备数量
     *
     * @param productId 产品
     * @return 结果
     */
    public int selectDeviceCountByProductId(Long productId);

    /**
     * 查询设备简短列表
     *
     * @param device 设备
     * @return 设备集合
     */
    public List<DeviceShortOutput> selectDeviceShortList(Device device);

    /**
     * 新增设备
     *
     * @param device 设备
     * @return 结果
     */
    public Device insertDevice(Device device);

    /**
     * 设备关联用户
     *
     * @param deviceRelateUserInput 设备
     * @return 结果
     */
    public AjaxResult deviceRelateUser(DeviceRelateUserInput deviceRelateUserInput);

    /**
     * 设备认证后自动添加设备
     *
     * @return 结果
     */
    public int insertDeviceAuto(String serialNumber,Long userId,Long productId);

    /**
     * 获取设备设置的影子
     * @param device
     * @return
     */
    public ThingsModelShadow getDeviceShadowThingsModel(Device device);

    /**
     * 修改设备
     *
     * @param device 设备
     * @return 结果
     */
    public AjaxResult updateDevice(Device device);

    /**
     * 更新设备状态和定位
     * @param device 设备
     * @return 结果
     */
    public int updateDeviceStatusAndLocation(Device device,String ipAddress);

    /**
     * 更新设备状态
     * @param device 设备
     * @return 结果
     */
    public int updateDeviceStatus(Device device);

    /**
     * 更新固件版本
     * @param device
     * @return
     */
    public int updateDeviceFirmwareVersion(Device device);


    /**
     * 上报设备信息
     * @param device 设备
     * @return 结果
     */
    public int reportDevice(Device device,Device deviceentity);

    /**
     * 删除设备
     *
     * @param deviceId 需要删除的设备主键集合
     * @return 结果
     */
    public AjaxResult deleteDeviceByDeviceId(Long deviceId) throws SchedulerException;

    /**
     * 生成设备唯一编号
     * @return 结果
     */
    public String generationDeviceNum(Integer type);

    /**
     * 重置设备状态
     * @return 结果
     */
    public int resetDeviceStatus(String deviceNum);

    /**
     * 根据设备编号查询协议编码
     * @param serialNumber 设备编号
     * @return
     */
    public DeviceAndProtocol selectProtocolBySerialNumber(String serialNumber);

    /**
     * 查询产品下所有设备，返回设备编号
     * @param productId 产品id
     * @return
     */
    public List<Device> selectDevicesByProductId(Long productId,Integer hasSub);


    /**
     * 批量更新设备状态
     * @param serialNumbers 设备ids
     * @param status 状态
     */
    public void batchChangeStatus(List<String> serialNumbers , DeviceStatus status);


    /**
     * 根据设备编号查询设备信息 -不带缓存物模型值
     * @param serialNumber
     * @return
     */
    public Device selectDeviceNoModel(String serialNumber);

    /**
     * 获取设备MQTT连接参数
     * @param deviceId 设备id
     * @return
     */
    public DeviceMqttConnectVO getMqttConnectData(Long deviceId);

    /**
     * 根据产品ID获取产品下所有编号
     * @param productId
     * @return
     */
    public String[] getDeviceNumsByProductId(Long productId);

    /**
     * 重置设备状态
     */
    public void reSetDeviceStatus();

    /**
     * 获取所有已经激活并不是禁用的设备
     * @return
     */
    List<DeviceStatusVO> selectDeviceActive();

    /**
     * 批量导入设备
     * @param deviceImportVOList 模板数据
     * @param: productId 产品id
     * @return java.lang.String
     */
    String importDevice(List<DeviceImportVO> deviceImportVOList, Long productId);

    /**
     * 批量导入分配设备
     * @param deviceAssignmentVOS 设备集合
     * @param: productId
     * @param: deptId
     * @return java.lang.String
     */
    String importAssignmentDevice(List<DeviceAssignmentVO> deviceAssignmentVOS, Long productId, Long deptId);

    /**
     * 分配设备
     * @param deptId 机构id
     * @param: deviceIds 设备id字符串
     * @return com.fastbee.common.core.domain.AjaxResult
     */
    AjaxResult assignment(Long deptId, String deviceIds);

    /**
     * 回收设备
     * @param: deviceIds 设备id字符串
     * @return com.fastbee.common.core.domain.AjaxResult
     */
    AjaxResult recovery(String deviceIds, Long recoveryDeptId);

    /**
     * 查询终端用户设备简短列表，主页列表数据
     */
    List<DeviceShortOutput> listTerminalUser(Device device);

    List<Device> listTerminalUserByGroup(Device device);

    /**
     * 根据监控设备channelId获取设备
     * @param channelId
     * @return
     */
    Device selectDeviceByChannelId(String channelId);

    /**
     * 查询设备状态以及传输协议
     * @param serialNumber
     * @return
     */
    DeviceStatusVO selectDeviceStatusAndTransportStatus(String serialNumber);

    /**
     * 获取设备物模型概况
     * @param deviceId 设备id
     * @return java.util.List<com.fastbee.iot.model.ThingsModelItem.ThingsModelShortVO>
     */
    List<ThingsModelDTO> listThingsModel(Long deviceId);

    public void updateByOrder(Long userId,Long deviceId);

    /**
     * 根据分组id集合查询设备分组
     *
     * @param groupIds 设备分组主键
     * @return 设备分组
     */
    List<DeviceGroup> listDeviceGroupByGroupIds(List<Long> groupIds);


    /**
     * 查询产品下所有设备，返回设备编号
     *
     * @param productId 产品id
     * @return
     */
    public List<String> selectSerialNumbersByProductId(Long productId);

}
