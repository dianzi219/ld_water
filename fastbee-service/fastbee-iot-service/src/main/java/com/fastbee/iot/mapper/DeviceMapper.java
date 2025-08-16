package com.fastbee.iot.mapper;

import com.fastbee.common.core.device.DeviceAndProtocol;
import com.fastbee.common.core.thingsModel.ThingsModelValuesInput;
import com.fastbee.iot.domain.Device;
import com.fastbee.iot.domain.DeviceGroup;
import com.fastbee.iot.model.*;
import com.fastbee.iot.model.ThingsModels.ThingsModelValuesOutput;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * 设备Mapper接口
 *
 * @author kerwincui
 * @date 2021-12-16
 */
@Repository
public interface DeviceMapper
{
    /**
     * 查询设备
     *
     * @param deviceId 设备主键
     * @return 设备
     */
    public Device selectDeviceByDeviceId(Long deviceId);

    /**
     * 查询设备和产品总数
     *
     * @return 设备
     */
    public DeviceStatistic selectDeviceProductAlertCount(Long deptId);

    /**
     * 根据设备编号查询设备
     *
     * @param serialNumber 设备主键
     * @return 设备
     */
    public Device selectDeviceBySerialNumber(String serialNumber);


    /**
     * 根据设备编号查询设备数量
     *
     * @param serialNumber 设备主键
     * @return 设备
     */
    public int selectDeviceCountBySerialNumber(String serialNumber);

    /**
     * 根据设备编号查询简介设备
     *
     * @param serialNumber 设备主键
     * @return 设备
     */
    public Device selectShortDeviceBySerialNumber(String serialNumber);


    /**
     * 根据设备编号查询协议编号
     * @param serialNumber 设备编号
     * @return 协议编号
     */
    public ProductCode getProtocolBySerialNumber(String serialNumber);

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
     * 查询设备的物模型值
     *
     * @param serialNumber 设备编号
     * @return 设备
     */
    public ThingsModelValuesOutput selectDeviceThingsModelValueBySerialNumber(String serialNumber);

    /**
     * 修改设备的物模型值
     *
     * @param input 设备ID和物模型值
     * @return 结果
     */
    public int updateDeviceThingsModelValue(ThingsModelValuesInput input);


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
     * 查询设备简短列表
     *
     * @param device 设备
     * @return 设备集合
     */
    public List<DeviceShortOutput> selectDeviceShortList(Device device);

    /**
     * 查询所有设备简短列表
     *
     * @return 设备集合
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
     * 新增设备
     *
     * @param device 设备
     * @return 结果
     */
    public int insertDevice(Device device);

    /**
     * 修改设备
     *
     * @param device 设备
     * @return 结果
     */
    public int updateDevice(Device device);

    /**
     * 更新设备状态
     *
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
     * 通过设备编号修改设备
     *
     * @param device 设备
     * @return 结果
     */
    public int updateDeviceBySerialNumber(Device device);

    /**
     * 删除设备
     *
     * @param deviceId 设备主键
     * @return 结果
     */
    public int deleteDeviceByDeviceId(Long deviceId);

    /**
     * 批量删除设备
     *
     * @param deviceIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteDeviceByDeviceIds(Long[] deviceIds);

    /**
     * 查询设备序列号的数量
     * @param deviceNum
     * @return
     */
    public int getDeviceNumCount(String deviceNum);

    /**
     * 根据设备IDS删除设备分组
     * @param userDeviceGroupIdModel
     * @return
     */
    public int deleteDeviceGroupByDeviceId(UserIdDeviceIdModel userDeviceGroupIdModel);

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
    public List<Device> selectDevicesByProductId(@Param("productId") Long productId,@Param("hasSub") Integer hasSub);

    /**
     * 批量更新设备上线
     * @param devices 设备ids
     */
    public void batchChangeOnline(List<String> devices);

    /**
     * 批量更新设备下线
     * @param devices 设备ids
     */
    public void batchChangeOffline(List<String> devices);


    /**
     * 获取设备MQTT连接参数
     * @param deviceId 设备id
     * @return
     */
    DeviceMqttVO selectMqttConnectData(Long deviceId);

    /**
     * 查询告警日志相关联信息
     * @param deviceNumbers 设备编号集合
     * @return
     */
    List<DeviceRelateAlertLogVO> selectDeviceBySerialNumbers(@Param("deviceNumbers") Set<String> deviceNumbers);

    /**
     * 查询告警日志相关联信息
     * @param deviceNumber 设备编号
     * @return
     */
    DeviceRelateAlertLogVO selectRelateAlertLogBySerialNumber(String deviceNumber);

    /**
     * 根据产品ID获取产品下所有编号
     * @param productId
     * @return
     */
    public String[] getDeviceNumsByProductId(Long productId);

    /**
     * 根据设备编号查询设备状态
     * @param deviceNumbers 设备编号集合
     * @return
     */
    public List<DeviceNumberStatus> selectDeviceStatusByNumbers(String[] deviceNumbers);



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
     * 批量添加设备
     * @param deviceList 设备集合
     * @return void
     */
    int insertBatchDevice(@Param("deviceList") List<Device> deviceList);

    /**
     * 批量校验设备
     * @param serialNumberList
     * @return java.util.List<java.lang.String>
     */
    List<String> checkExistBySerialNumbers(@Param("serialNumberList") List<String> serialNumberList);

    /**
     * 更新设备绑定机构
     * @param tenantId 机构管理员id
     * @param: userName  管理员名称
     * @param: deviceIdList 设备id集合
     * @return int
     */
    int updateTenantIdByDeptIds(@Param("tenantId") Long tenantId, @Param("tenantName") String tenantName, @Param("deviceIdList") List<Long> deviceIdList);

    /**
     * 查询终端用户设备简短列表，主页列表数据
     */
    List<DeviceShortOutput> listTerminalUser(Device device);

    List<Device> listTerminalUserByGroup(Device device);

    /**
     * 设备数量
     * @param tenantId
     * @return int
     */
    int countByTenantId(Long tenantId);

    /**
     * @description:
     * @param: deviceIdList 设备id集合
     * @return: java.util.List<com.fastbee.iot.domain.Device>
     */
    List<Device> selectDeviceListByDeviceIds(@Param("deviceIdList") List<Long> deviceIdList);
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
     * 根据分组id集合查询设备分组
     *
     * @param groupIds 设备分组主键
     * @return 设备分组
     */
    List<DeviceGroup> listDeviceGroupByGroupIds(List<Long> groupIds);


    /**
     * 查询产品下所有设备，返回设备编号
     * @param productId 产品id
     * @return
     */
    public List<String> selectSerialNumbersByProductId(@Param("productId") Long productId);

}
