package com.fastbee.iot.service;

import java.util.List;

import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.iot.domain.DeviceAlertUser;
import com.fastbee.iot.model.DeviceAlertUserVO;

/**
 * 设备告警用户Service接口
 *
 * @author kerwincui
 * @date 2024-05-15
 */
public interface IDeviceAlertUserService
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
     * @param deviceAlertUserVO 设备告警用户
     * @return 结果
     */
    public int insertDeviceAlertUser(DeviceAlertUserVO deviceAlertUserVO);

    /**
     * 修改设备告警用户
     *
     * @param deviceAlertUser 设备告警用户
     * @return 结果
     */
    public int updateDeviceAlertUser(DeviceAlertUser deviceAlertUser);

    /**
     * 批量删除设备告警用户
     *
     * @param deviceIds 需要删除的设备告警用户主键集合
     * @return 结果
     */
    public int deleteDeviceAlertUserByDeviceIds(Long[] deviceIds);

    /**
     * 删除设备告警用户信息
     *
     * @param deviceId 设备告警用户主键
     * @return 结果
     */
    public int deleteDeviceAlertUserByDeviceId(Long deviceId);

    /**
     * 删除设备告警用户
     * @param deviceId 设备id
     * @param: userId 用户id
     * @return int
     */
    int deleteByDeviceIdAndUserId(Long deviceId, Long userId);

    /**
     * 查询用户
     * @param sysUser 系统用户
     * @return java.util.List<com.fastbee.common.core.domain.entity.SysUser>
     */
    List<SysUser> selectUserList(SysUser sysUser);
}
