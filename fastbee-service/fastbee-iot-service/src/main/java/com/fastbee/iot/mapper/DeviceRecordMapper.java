package com.fastbee.iot.mapper;

import java.util.List;

import com.fastbee.framework.mybatis.mapper.BaseMapperX;
import com.fastbee.iot.domain.DeviceRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设备记录Mapper接口
 *
 * @author zhangzhiyi
 * @date 2024-07-16
 */
@Mapper
public interface DeviceRecordMapper extends BaseMapperX<DeviceRecord>
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
    public List<DeviceRecord> selectDeviceRecordList(DeviceRecord deviceRecord);

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
     * 删除设备记录
     *
     * @param id 设备记录主键
     * @return 结果
     */
    public int deleteDeviceRecordById(Long id);

    /**
     * 批量删除设备记录
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteDeviceRecordByIds(Long[] ids);
}
