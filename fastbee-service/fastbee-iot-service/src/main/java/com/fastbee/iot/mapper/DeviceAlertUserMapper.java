package com.fastbee.iot.mapper;

import java.util.List;
import com.fastbee.iot.domain.DeviceAlertUser;
import org.apache.ibatis.annotations.Param;

/**
 * 设备告警用户Mapper接口
 *
 * @author kerwincui
 * @date 2024-05-15
 */
public interface DeviceAlertUserMapper
{
    /**
     * 查询设备告警用户
     *
     * @param deviceId 设备告警用户主键
     * @return 设备告警用户
     */
    public DeviceAlertUser selectDeviceAlertUserByDeviceId(Long deviceId);

    /**
     * 查询设备告警用户列表
     *
     * @param deviceAlertUser 设备告警用户
     * @return 设备告警用户集合
     */
    public List<DeviceAlertUser> selectDeviceAlertUserList(DeviceAlertUser deviceAlertUser);

    /**
     * 新增设备告警用户
     *
     * @param deviceAlertUser 设备告警用户
     * @return 结果
     */
    public int insertDeviceAlertUser(DeviceAlertUser deviceAlertUser);

    /**
     * 修改设备告警用户
     *
     * @param deviceAlertUser 设备告警用户
     * @return 结果
     */
    public int updateDeviceAlertUser(DeviceAlertUser deviceAlertUser);

    /**
     * 删除设备告警用户
     *
     * @param deviceId 设备告警用户主键
     * @return 结果
     */
    public int deleteDeviceAlertUserByDeviceId(Long deviceId);

    /**
     * 批量删除设备告警用户
     *
     * @param deviceIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteDeviceAlertUserByDeviceIds(Long[] deviceIds);

    /**
     * 删除设备告警用户
     * @param deviceId 设备id
     * @param: userId 用户id
     * @return int
     */
    int deleteByDeviceIdAndUserId(@Param("deviceId") Long deviceId, @Param("userId") Long userId);
}
