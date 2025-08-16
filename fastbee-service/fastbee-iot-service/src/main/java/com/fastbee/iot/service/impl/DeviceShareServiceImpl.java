package com.fastbee.iot.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.iot.domain.Device;
import com.fastbee.iot.domain.DeviceUser;
import com.fastbee.iot.mapper.DeviceMapper;
import com.fastbee.iot.mapper.SceneDeviceMapper;
import com.fastbee.iot.service.IDeviceUserService;
import com.fastbee.iot.service.ISceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fastbee.iot.mapper.DeviceShareMapper;
import com.fastbee.iot.domain.DeviceShare;
import com.fastbee.iot.service.IDeviceShareService;

import javax.annotation.Resource;

/**
 * 设备分享Service业务层处理
 */
@Service
public class DeviceShareServiceImpl implements IDeviceShareService {
    @Autowired
    private DeviceShareMapper deviceShareMapper;
    @Resource
    private IDeviceUserService deviceUserService;

    @Resource
    private SceneDeviceMapper sceneDeviceMapper;
    @Resource
    private DeviceMapper deviceMapper;
    @Resource
    private ISceneService sceneService;

    /**
     * 根据用户id、设备Id获取分享详情
     * @param deviceId
     * @param userId
     * @return
     */
    @Override
    public DeviceShare selectDeviceShareByDeviceIdAndUserId(Long deviceId,Long userId){
        return deviceShareMapper.selectDeviceShareByDeviceIdAndUserId(deviceId,userId);
    }


    /**
     * 查询设备分享 -根据设备id查询可能有多个分享用户
     *
     * @param deviceId 设备分享主键
     * @return 设备分享
     */
    @Override
    public List<DeviceShare> selectDeviceShareByDeviceId(Long deviceId) {
        return deviceShareMapper.selectDeviceShareByDeviceId(deviceId);
    }

    /**
     * 查询设备分享列表
     *
     * @param deviceShare 设备分享
     * @return 设备分享
     */
    @Override
    public List<DeviceShare> selectDeviceShareList(DeviceShare deviceShare) {
        List<DeviceShare> result = new ArrayList<>();
        //将当前设备的所属设备用户添加到第一列
        DeviceUser deviceUser = deviceUserService.selectDeviceUserByDeviceId(deviceShare.getDeviceId());
        if (!Objects.isNull(deviceUser)) {
            DeviceShare share = new DeviceShare();
            share.setDeviceId(deviceUser.getDeviceId())
                    .setUserId(deviceUser.getUserId())
                    .setIsOwner(1)
                    .setUserName(deviceUser.getUserName())
                    .setPhonenumber(deviceUser.getPhonenumber())
                    .setCreateTime(deviceUser.getCreateTime());
            result.add(share);
        }
        List<DeviceShare> deviceShareList = deviceShareMapper.selectDeviceShareList(deviceShare);
        deviceShareList.stream().forEach(d -> d.setIsOwner(0));
        result.addAll(deviceShareList);
        return result;
    }

    /**
     * 新增设备分享
     *
     * @param deviceShare 设备分享
     * @return 结果
     */
    @Override
    public int insertDeviceShare(DeviceShare deviceShare) {
        assert !Objects.isNull(deviceShare.getUserId()) : "设备用户ID不能为空";
        assert !Objects.isNull(deviceShare.getDeviceId()) : "设备ID不能为空";
        DeviceShare queryDeviceShare = this.selectDeviceShareByDeviceIdAndUserId(deviceShare.getDeviceId(), deviceShare.getUserId());
        if (!Objects.isNull(queryDeviceShare)) throw new RuntimeException("该用户已添加, 禁止重复添加");
        deviceShare.setCreateTime(DateUtils.getNowDate());
        return deviceShareMapper.insertDeviceShare(deviceShare);
    }

    /**
     * 修改设备分享
     *
     * @param deviceShare 设备分享
     * @return 结果
     */
    @Override
    public int updateDeviceShare(DeviceShare deviceShare) {
        deviceShare.setUpdateTime(DateUtils.getNowDate());
        return deviceShareMapper.updateDeviceShare(deviceShare);
    }

    /**
     * 批量删除设备分享
     *
     * @param deviceIds 需要删除的设备分享主键
     * @return 结果
     */
    @Override
    public int deleteDeviceShareByDeviceIds(Long[] deviceIds) {
        return deviceShareMapper.deleteDeviceShareByDeviceIds(deviceIds);
    }

    /**
     * 删除设备分享信息
     *
     * @param deviceId 设备分享主键
     * @return 结果
     */
    @Override
    public int deleteDeviceShareByDeviceId(Long deviceId) {
        return deviceShareMapper.deleteDeviceShareByDeviceId(deviceId);
    }

    /**
     * 查询分享设备用户
     *
     * @param  deviceUser 设备分享
     * @return 设备用户集合
     */
    @Override
    public SysUser selectShareUser(DeviceShare share){
        return deviceShareMapper.selectShareUser(share);
    }

    @Override
    public int deleteDeviceShareByDeviceIdAndUserId(DeviceShare deviceShare) {
        // 把分享用户配置的场景删掉
        Device device = deviceMapper.selectDeviceByDeviceId(deviceShare.getDeviceId());
        if (null != device) {
            Long[] sceneIds = sceneDeviceMapper.listSceneIdByDeviceIdAndUserId(device.getSerialNumber(), Collections.singletonList(deviceShare.getUserId()));
            if (null != sceneIds && sceneIds.length > 0) {
                sceneService.deleteSceneBySceneIds(sceneIds);
            }
        }
        return deviceShareMapper.deleteDeviceShareByDeviceIdAndUserId(deviceShare);
    }

}
