package com.fastbee.iot.service.impl;

import com.fastbee.common.core.device.DeviceAndProtocol;
import com.fastbee.common.core.mq.SubDeviceBo;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.iot.domain.DeviceJob;
import com.fastbee.iot.domain.ModbusJob;
import com.fastbee.iot.enums.DeviceType;
import com.fastbee.iot.mapper.ModbusJobMapper;
import com.fastbee.iot.model.scenemodel.SceneModelTagCycleVO;
import com.fastbee.iot.service.IDeviceJobService;
import com.fastbee.iot.service.IDeviceService;
import com.fastbee.iot.service.IModbusJobService;
import com.fastbee.iot.service.ISubGatewayService;
import com.fastbee.iot.util.JobCronUtils;
import org.quartz.SchedulerException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 轮训任务列Service业务层处理
 *
 * @author kerwincui
 * @date 2024-07-05
 */
@Service
public class ModbusJobServiceImpl implements IModbusJobService {
    @Resource
    private ModbusJobMapper modbusJobMapper;
    @Lazy
    @Resource
    private IDeviceService deviceService;
    @Resource
    private IDeviceJobService deviceJobService;
    @Resource
    private ISubGatewayService subGatewayService;

    /**
     * 查询轮训任务列
     *
     * @param taskId 轮训任务列主键
     * @return 轮训任务列
     */
    @Override
    public ModbusJob selectModbusJobByTaskId(Long taskId) {
        return modbusJobMapper.selectModbusJobByTaskId(taskId);
    }

    /**
     * 查询轮训任务列列表
     *
     * @param modbusJob 轮训任务列
     * @return 轮训任务列
     */
    @Override
    public List<ModbusJob> selectModbusJobList(ModbusJob modbusJob) {
        List<ModbusJob> modbusJobList = modbusJobMapper.selectModbusJobList(modbusJob);
        if (!CollectionUtils.isEmpty(modbusJobList)) {
            for (ModbusJob job : modbusJobList) {
                job.setRemarkStr(JobCronUtils.handleCronCycle(1, job.getRemark()).getDesc());
            }
        }
        return modbusJobList;

    }

    /**
     * 新增轮训任务列
     *
     * @param modbusJob 轮训任务列
     * @return 结果
     */
    @Override
    public int insertModbusJob(ModbusJob modbusJob) {
        try {
            //查询关联的网关设备
            DeviceAndProtocol deviceAndProtocol = deviceService.selectProtocolBySerialNumber(modbusJob.getSubSerialNumber());
            //如果是网关子设备
            if (deviceAndProtocol.getDeviceType() == DeviceType.SUB_GATEWAY.getCode()) {
                //如果有绑定网关
                if (!Objects.isNull(deviceAndProtocol.getGwDeviceId())) {
                    DeviceJob deviceJob = new DeviceJob();
                    deviceJob.setDeviceId(deviceAndProtocol.getGwDeviceId());
                    deviceJob.setRemark(modbusJob.getRemark());
                    deviceJob.setJobType(5);
                    List<DeviceJob> list = deviceJobService.selectJobList(deviceJob);
                    if (CollectionUtils.isEmpty(list)) {
                        //如果不存在定时任务则新建
                        deviceJob.setJobName("modbus poll");
                        deviceJob.setJobGroup("MODBUS");
                        deviceJob.setMisfirePolicy("2");
                        deviceJob.setConcurrent("1");
                        deviceJob.setIsAdvance(0);
                        deviceJob.setStatus("0");
                        deviceJob.setSerialNumber(deviceAndProtocol.getGwSerialNumber());
                        deviceJob.setProductId(deviceAndProtocol.getGwProductId());
                        deviceJob.setCreateTime(DateUtils.getNowDate());
                        //处理时间转换,这里原始的表达式放在remark
                        SceneModelTagCycleVO sceneModelTagCycleVO = JobCronUtils.handleCronCycle(1, deviceJob.getRemark());
                        deviceJob.setCronExpression(sceneModelTagCycleVO.getCron());
                        deviceJobService.insertJob(deviceJob);
                        modbusJob.setJobId(deviceJob.getJobId());
                    } else {
                        DeviceJob job = list.get(0);
                        modbusJob.setJobId(job.getJobId());
                    }
                } else {
                    throw new ServiceException("请先绑定网关");
                }
                modbusJob.setCreateTime(DateUtils.getNowDate());
                modbusJob.setDeviceType(deviceAndProtocol.getDeviceType());
                return modbusJobMapper.insertModbusJob(modbusJob);
            } else {
                //直连设备
                DeviceJob deviceJob = new DeviceJob();
                deviceJob.setDeviceId(modbusJob.getSubDeviceId());
                deviceJob.setRemark(modbusJob.getRemark());
                deviceJob.setJobType(5);
                List<DeviceJob> list = deviceJobService.selectJobList(deviceJob);
                if (CollectionUtils.isEmpty(list)) {
                    //创建定时任务
                    //如果不存在定时任务则新建
                    deviceJob.setJobName("modbus poll");
                    deviceJob.setJobGroup("MODBUS");
                    deviceJob.setMisfirePolicy("2");
                    deviceJob.setConcurrent("1");
                    deviceJob.setIsAdvance(0);
                    deviceJob.setStatus("0");
                    deviceJob.setSerialNumber(deviceAndProtocol.getSerialNumber());
                    deviceJob.setProductId(deviceAndProtocol.getProductId());
                    deviceJob.setCreateTime(DateUtils.getNowDate());
                    //处理时间转换,这里原始的表达式放在remark
                    SceneModelTagCycleVO sceneModelTagCycleVO = JobCronUtils.handleCronCycle(1, deviceJob.getRemark());
                    deviceJob.setCronExpression(sceneModelTagCycleVO.getCron());
                    deviceJobService.insertJob(deviceJob);
                    modbusJob.setJobId(deviceJob.getJobId());
                } else {
                    DeviceJob job = list.get(0);
                    modbusJob.setJobId(job.getJobId());
                }
                modbusJob.setCreateTime(DateUtils.getNowDate());
                modbusJob.setDeviceType(deviceAndProtocol.getDeviceType());
                return modbusJobMapper.insertModbusJob(modbusJob);
            }
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * 修改轮训任务列
     *
     * @param modbusJob 轮训任务列
     * @return 结果
     */
    @Override
    public int updateModbusJob(ModbusJob modbusJob) {
        return modbusJobMapper.updateModbusJob(modbusJob);
    }

    /**
     * 批量删除轮训任务列
     *
     * @param taskIds 需要删除的轮训任务列主键
     * @return 结果
     */
    @Override
    public int deleteModbusJobByTaskIds(Long[] taskIds) {
        return modbusJobMapper.deleteModbusJobByTaskIds(taskIds);
    }

    /**
     * 删除轮训任务列信息
     *
     * @param modbusJob 轮训任务列主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteModbusJobByTaskId(ModbusJob modbusJob) {
        try {
            //先删除轮询任务
            int result = modbusJobMapper.deleteModbusJobByTaskId(modbusJob.getTaskId());
            //查询是否有轮询任务
            Long jobId = modbusJob.getJobId();
            ModbusJob job = new ModbusJob();
            job.setJobId(jobId);
            List<ModbusJob> modbusJobList = selectModbusJobList(job);
            if (CollectionUtils.isEmpty(modbusJobList)) {
                //删除任务
                deviceJobService.deleteJobByIds(new Long[]{jobId});
            }
            return result;
        } catch (SchedulerException e) {
            return 0;
        }
    }


    /**
     * 根据设备类型获取所有轮询任务
     * @param deviceType
     * @param serialNumber
     * @return
     */
    @Override
    public List<ModbusJob> selectDevicesJobByDeviceType(DeviceType deviceType,String serialNumber){
        ModbusJob modbusJob = new ModbusJob();
        modbusJob.setStatus("0");
        switch (deviceType){
            case DIRECT_DEVICE:
            case SUB_GATEWAY:
                //直连设备直接查询
                modbusJob.setSubSerialNumber(serialNumber);
                return this.selectModbusJobList(modbusJob);
            case GATEWAY:
                //网关设备需要查询所有子设备的modbus任务
                List<SubDeviceBo> subDeviceList = subGatewayService.getSubDeviceListByGw(serialNumber);
                List<String> serialNumberList = subDeviceList.stream().map(SubDeviceBo::getSubDeviceNo).collect(Collectors.toList());
                modbusJob.setSubDeviceList(serialNumberList);
                return this.selectModbusJobList(modbusJob);
            default:
                return new ArrayList<>();
        }
    }
}
