package com.fastbee.iot.mapper;

import java.util.List;

import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.iot.domain.DeviceShare;
import com.fastbee.iot.domain.DeviceUser;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 设备分享Mapper接口
 *
 * @author kerwincui
 * @date 2024-04-03
 */
@Repository
public interface DeviceShareMapper
{
    /**
     * 根据用户id、设备Id获取分享详情
     * @param deviceId
     * @param userId
     * @return
     */
    DeviceShare selectDeviceShareByDeviceIdAndUserId(@Param("deviceId") Long deviceId, @Param("userId") Long userId);

    /**
     * 查询设备分享
     *
     * @param deviceId 设备分享主键
     * @return 设备分享
     */
    public List<DeviceShare> selectDeviceShareByDeviceId(Long deviceId);

    /**
     * 查询设备分享列表
     *
     * @param deviceShare 设备分享
     * @return 设备分享集合
     */
    public List<DeviceShare> selectDeviceShareList(DeviceShare deviceShare);

    /**
     * 新增设备分享
     *
     * @param deviceShare 设备分享
     * @return 结果
     */
    public int insertDeviceShare(DeviceShare deviceShare);

    /**
     * 修改设备分享
     *
     * @param deviceShare 设备分享
     * @return 结果
     */
    public int updateDeviceShare(DeviceShare deviceShare);

    /**
     * 删除设备分享
     *
     * @param deviceId 设备分享主键
     * @return 结果
     */
    public int deleteDeviceShareByDeviceId(Long deviceId);

    /**
     * 批量删除设备分享
     *
     * @param deviceIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteDeviceShareByDeviceIds(Long[] deviceIds);

    /**
     * 查询分享设备用户
     *
     * @param deviceUser 设备分享
     * @return 设备用户集合
     */
    public SysUser selectShareUser(DeviceShare share);

    /**
     * 删除分享设备
     * @param deviceShare
     * @return int
     */
    int deleteDeviceShareByDeviceIdAndUserId(DeviceShare deviceShare);
}
