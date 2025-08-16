package com.fastbee.iot.service.impl;

import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.iot.domain.DeviceAlertUser;
import com.fastbee.iot.mapper.DeviceAlertUserMapper;
import com.fastbee.iot.model.DeviceAlertUserVO;
import com.fastbee.iot.service.IDeviceAlertUserService;
import com.fastbee.system.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设备告警用户Service业务层处理
 *
 * @author kerwincui
 * @date 2024-05-15
 */
@Service
public class DeviceAlertUserServiceImpl implements IDeviceAlertUserService
{
    @Resource
    private DeviceAlertUserMapper deviceAlertUserMapper;
    @Resource
    private SysUserMapper sysUserMapper;

    /**
     * 查询设备告警用户
     *
     * @param deviceId 设备告警用户主键
     * @return 设备告警用户
     */
    @Override
    public DeviceAlertUser selectDeviceAlertUserByDeviceId(Long deviceId)
    {
        return deviceAlertUserMapper.selectDeviceAlertUserByDeviceId(deviceId);
    }

    /**
     * 查询设备告警用户列表
     *
     * @param deviceAlertUser 设备告警用户
     * @return 设备告警用户
     */
    @Override
    public List<DeviceAlertUser> selectDeviceAlertUserList(DeviceAlertUser deviceAlertUser)
    {
        return deviceAlertUserMapper.selectDeviceAlertUserList(deviceAlertUser);
    }

    /**
     * 新增设备告警用户
     *
     * @param deviceAlertUserVO 设备告警用户
     * @return 结果
     */
    @Override
    public int insertDeviceAlertUser(DeviceAlertUserVO deviceAlertUserVO)
    {
        DeviceAlertUser deviceAlertUser = new DeviceAlertUser();
        deviceAlertUser.setDeviceId(deviceAlertUserVO.getDeviceId());
        List<DeviceAlertUser> alertUserList = this.selectDeviceAlertUserList(deviceAlertUser);
        List<Long> userIdList = alertUserList.stream().map(DeviceAlertUser::getUserId).collect(Collectors.toList());
        List<Long> newUserIdList = deviceAlertUserVO.getUserIdList();
        newUserIdList.removeAll(userIdList);
        for (Long userId : newUserIdList) {
            DeviceAlertUser deviceAlertUser1 = new DeviceAlertUser();
            deviceAlertUser1.setUserId(userId);
            deviceAlertUser1.setDeviceId(deviceAlertUserVO.getDeviceId());
            deviceAlertUserMapper.insertDeviceAlertUser(deviceAlertUser1);
        }
        return 1;
    }

    /**
     * 修改设备告警用户
     *
     * @param deviceAlertUser 设备告警用户
     * @return 结果
     */
    @Override
    public int updateDeviceAlertUser(DeviceAlertUser deviceAlertUser)
    {
        return deviceAlertUserMapper.updateDeviceAlertUser(deviceAlertUser);
    }

    /**
     * 批量删除设备告警用户
     *
     * @param deviceIds 需要删除的设备告警用户主键
     * @return 结果
     */
    @Override
    public int deleteDeviceAlertUserByDeviceIds(Long[] deviceIds)
    {
        return deviceAlertUserMapper.deleteDeviceAlertUserByDeviceIds(deviceIds);
    }

    /**
     * 删除设备告警用户信息
     *
     * @param deviceId 设备告警用户主键
     * @return 结果
     */
    @Override
    public int deleteDeviceAlertUserByDeviceId(Long deviceId)
    {
        return deviceAlertUserMapper.deleteDeviceAlertUserByDeviceId(deviceId);
    }

    @Override
    public int deleteByDeviceIdAndUserId(Long deviceId, Long userId) {
        return deviceAlertUserMapper.deleteByDeviceIdAndUserId(deviceId, userId);
    }

    @Override
    public List<SysUser> selectUserList(SysUser sysUser) {
        return sysUserMapper.selectUserList(sysUser);
    }
}
