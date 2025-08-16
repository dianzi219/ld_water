package com.fastbee.iot.service.impl;

import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.iot.domain.DeviceShare;
import com.fastbee.iot.domain.DeviceUser;
import com.fastbee.iot.mapper.DeviceUserMapper;
import com.fastbee.iot.model.UserIdDeviceIdModel;
import com.fastbee.iot.service.IDeviceShareService;
import com.fastbee.iot.service.IDeviceUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 设备用户Service业务层处理
 *
 * @author kerwincui
 * @date 2021-12-16
 */
@Service
public class DeviceUserServiceImpl implements IDeviceUserService
{
    @Autowired
    private DeviceUserMapper deviceUserMapper;
    @Resource
    private IDeviceShareService deviceShareService;

    /**
     * 查询设备用户
     *
     * @param deviceId 设备用户主键
     * @return 设备用户
     */
    @Override
    public DeviceUser selectDeviceUserByDeviceId(Long deviceId)
    {
        return deviceUserMapper.selectDeviceUserByDeviceId(deviceId);
    }

    /**
     * 查询设备用户列表
     *
     * @param deviceUser 设备用户
     * @return 设备用户
     */
    @Override
    public List<DeviceUser> selectDeviceUserList(DeviceUser deviceUser)
    {
        return deviceUserMapper.selectDeviceUserList(deviceUser);
    }

    /**
     * 查询设备分享用户
     *
     * @param deviceUser 设备用户
     * @return 设备用户
     */
    @Override
    public SysUser selectShareUser(DeviceUser deviceUser)
    {
        return deviceUserMapper.selectShareUser(deviceUser);
    }

    /**
     * 新增设备用户
     *
     * @param deviceUser 设备用户
     * @return 结果
     */
    @Override
    public int insertDeviceUser(DeviceUser deviceUser)
    {
        List<DeviceUser> deviceUsers = selectDeviceUserList(deviceUser);
        if (!deviceUsers.isEmpty()) { throw new RuntimeException("该用户已添加, 禁止重复添加");}
        deviceUser.setCreateTime(DateUtils.getNowDate());
        return deviceUserMapper.insertDeviceUser(deviceUser);
    }

    /**
     * 修改设备用户
     *
     * @param deviceUser 设备用户
     * @return 结果
     */
    @Override
    public int updateDeviceUser(DeviceUser deviceUser)
    {
        deviceUser.setUpdateTime(DateUtils.getNowDate());
        return deviceUserMapper.updateDeviceUser(deviceUser);
    }

    /**
     * 批量删除设备用户
     *
     * @param deviceIds 需要删除的设备用户主键
     * @return 结果
     */
    @Override
    public int deleteDeviceUserByDeviceIds(Long[] deviceIds)
    {
        return deviceUserMapper.deleteDeviceUserByDeviceIds(deviceIds);
    }

    /**
     * 删除设备用户信息
     *
     * @param deviceId 设备用户主键
     * @return 结果
     */
    @Override
    public int deleteDeviceUserByDeviceId(Long deviceId)
    {
        return deviceUserMapper.deleteDeviceUserByDeviceId(new UserIdDeviceIdModel(null,deviceId));
    }

    @Override
    public int insertDeviceUserList(List<DeviceUser> deviceUsers) {
        try {
            deviceUsers.forEach(deviceUser -> {
                deviceUser.setCreateTime(DateUtils.getNowDate());
            });
            return deviceUserMapper.insertDeviceUserList(deviceUsers);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("存在设备已经与用户绑定");
        }
    }

    @Override
    public DeviceUser selectDeviceUserByDeviceIdAndUserId(Long deviceId, Long userId) {
        return deviceUserMapper.selectDeviceUserByDeviceIdAndUserId(deviceId, userId);
    }

    @Override
    public int deleteDeviceUser(DeviceUser deviceUser) {
        return deviceUserMapper.deleteDeviceUser(deviceUser);
    }

    /**
     * 获取设备用户与分享用户
     * @param deviceId
     * @return
     */
    public List<DeviceUser> getDeviceUserAndShare(Long deviceId){
        List<DeviceUser> result = new ArrayList<>();
        //获取设备用户
        DeviceUser deviceUser = this.selectDeviceUserByDeviceId(deviceId);
        Optional.ofNullable(deviceUser).ifPresent(result::add);
        //获取分享用户
        List<DeviceShare> deviceShareList = deviceShareService.selectDeviceShareByDeviceId(deviceId);
        if (!CollectionUtils.isEmpty(deviceShareList)) {
            for (DeviceShare deviceShare : deviceShareList) {
                DeviceUser user = new DeviceUser();
                user.setDeviceId(deviceShare.getDeviceId());
                user.setUserId(deviceShare.getUserId());
                user.setPhonenumber(deviceShare.getPhonenumber());
                result.add(user);
            }
        }
        return result;
    }
}
