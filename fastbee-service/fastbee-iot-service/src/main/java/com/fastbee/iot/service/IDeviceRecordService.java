package com.fastbee.iot.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fastbee.iot.domain.DeviceRecord;
import com.fastbee.iot.model.DeviceRecordVO;

/**
 * 设备记录Service接口
 *
 * @author zhangzhiyi
 * @date 2024-07-16
 */
public interface IDeviceRecordService
{
    /**
     * 查询设备记录
     *
     * @param id 设备记录主键
     * @return 设备记录
     */
    public DeviceRecord selectDeviceRecordById(Long id);

    /**
     * 查询设备记录列表
     *
     * @param deviceRecord 设备记录
     * @return 设备记录集合
     */
    public Page<DeviceRecordVO> selectDeviceRecordList(DeviceRecord deviceRecord);

    /**
     * 新增设备记录
     *
     * @param deviceRecord 设备记录
     * @return 结果
     */
    public int insertDeviceRecord(DeviceRecord deviceRecord);

    /**
     * 修改设备记录
     *
     * @param deviceRecord 设备记录
     * @return 结果
     */
    public int updateDeviceRecord(DeviceRecord deviceRecord);

    /**
     * 批量删除设备记录
     *
     * @param ids 需要删除的设备记录主键集合
     * @return 结果
     */
    public int deleteDeviceRecordByIds(Long[] ids);

    /**
     * 删除设备记录信息
     *
     * @param id 设备记录主键
     * @return 结果
     */
    public int deleteDeviceRecordById(Long id);
}
