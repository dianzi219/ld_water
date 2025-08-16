package com.fastbee.iot.service;

import java.util.List;
import com.fastbee.iot.domain.ModbusJob;
import com.fastbee.iot.enums.DeviceType;
import org.quartz.SchedulerException;

/**
 * 轮训任务列Service接口
 * 
 * @author kerwincui
 * @date 2024-07-05
 */
public interface IModbusJobService 
{
    /**
     * 查询轮训任务列
     * 
     * @param taskId 轮训任务列主键
     * @return 轮训任务列
     */
    public ModbusJob selectModbusJobByTaskId(Long taskId);

    /**
     * 查询轮训任务列列表
     * 
     * @param modbusJob 轮训任务列
     * @return 轮训任务列集合
     */
    public List<ModbusJob> selectModbusJobList(ModbusJob modbusJob);

    /**
     * 新增轮训任务列
     * 
     * @param modbusJob 轮训任务列
     * @return 结果
     */
    public int insertModbusJob(ModbusJob modbusJob);

    /**
     * 修改轮训任务列
     * 
     * @param modbusJob 轮训任务列
     * @return 结果
     */
    public int updateModbusJob(ModbusJob modbusJob);

    /**
     * 批量删除轮训任务列
     * 
     * @param taskIds 需要删除的轮训任务列主键集合
     * @return 结果
     */
    public int deleteModbusJobByTaskIds(Long[] taskIds);

    /**
     * 删除轮训任务列信息
     * 
     * @param modbusJob 轮训任务列主键
     * @return 结果
     */
    public int deleteModbusJobByTaskId(ModbusJob modbusJob) throws SchedulerException;

    /**
     * 根据设备类型获取所有轮询任务
     * @param deviceType
     * @param serialNumber
     * @return
     */
    List<ModbusJob> selectDevicesJobByDeviceType(DeviceType deviceType,String serialNumber);
}
