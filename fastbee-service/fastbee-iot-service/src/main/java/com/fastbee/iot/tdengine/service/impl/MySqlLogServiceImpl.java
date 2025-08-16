package com.fastbee.iot.tdengine.service.impl;

import com.fastbee.iot.domain.Device;
import com.fastbee.iot.domain.DeviceLog;
import com.fastbee.iot.mapper.EventLogMapper;
import com.fastbee.iot.mapper.FunctionLogMapper;
import com.fastbee.iot.model.DeviceStatistic;
import com.fastbee.iot.model.HistoryModel;
import com.fastbee.iot.model.ThingsModelLogCountVO;
import com.fastbee.iot.model.param.DataCenterParam;
import com.fastbee.iot.tdengine.service.ILogService;
import com.fastbee.iot.mapper.DeviceLogMapper;
import com.fastbee.iot.model.MonitorModel;
import com.fastbee.iot.tdengine.service.model.TdLogDto;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 类名: MySqlLogServiceImpl
 * 描述: MySQL存储日志实现类
 * 时间: 2022/5/22,0022 13:37
 * 开发人: admin
 */
@Slf4j
public class MySqlLogServiceImpl implements ILogService {

    private DeviceLogMapper deviceLogMapper;

    private EventLogMapper eventLogMapper;

    private FunctionLogMapper functionLogMapper;

    public MySqlLogServiceImpl(DeviceLogMapper _deviceLogMapper,EventLogMapper _eventLogMapper,FunctionLogMapper _functionLogMapper) {
        this.deviceLogMapper = _deviceLogMapper;
        this.eventLogMapper = _eventLogMapper;
        this.functionLogMapper = _functionLogMapper;
    }

    /***
     * 新增设备日志
     * @return
     */
    @Override
    public int saveDeviceLog(DeviceLog deviceLog) {
        return deviceLogMapper.insertDeviceLog(deviceLog);
    }

    @Override
    public int saveBatch(TdLogDto dto) {
        return deviceLogMapper.saveBatch(dto.getList());
    }

    /***
     * 根据设备ID删除设备日志
     * @return
     */
    @Override
    public int deleteDeviceLogByDeviceNumber(String deviceNumber) {
        return deviceLogMapper.deleteDeviceLogByDeviceNumber(deviceNumber);
    }

    /***
     * 设备属性、功能、事件和监测数据总数
     * @return
     */
    @Override
    public DeviceStatistic selectCategoryLogCount(Device device) {
        DeviceStatistic statistic = deviceLogMapper.selectDeviceLogCount(device.getTenantId());
        statistic.setEventCount(eventLogMapper.selectEventLogCount(device.getTenantId()));
        statistic.setEventCount(functionLogMapper.selectFunctionLogCount(device.getTenantId()));
        return statistic;
    }

    /***
     * 日志列表
     * @return
     */
    @Override
    public List<DeviceLog> selectDeviceLogList(DeviceLog deviceLog) {
        return deviceLogMapper.selectDeviceLogList(deviceLog);
    }

    /***
     * 监测数据列表
     * @return
     */
    @Override
    public List<MonitorModel> selectMonitorList(DeviceLog deviceLog) {
        return deviceLogMapper.selectMonitorList(deviceLog);
    }

    /**
     * 查询历史数据  is_Montor=1 或 is_history=1
     */
    @Override
    public Map<String, List<HistoryModel>> selectHistoryList(DeviceLog deviceLog) {
        List<HistoryModel> historyList = deviceLogMapper.selectHistoryList(deviceLog);
        return historyList.stream().collect(Collectors.groupingBy(HistoryModel::getIdentity));
    }

    @Override
    public List<HistoryModel> listHistory(DeviceLog deviceLog) {
        return deviceLogMapper.listHistory(deviceLog);
    }

    @Override
    public List<HistoryModel> listhistoryGroupByCreateTime(DeviceLog deviceLog) {
        return deviceLogMapper.listhistoryGroupByCreateTime(deviceLog);
    }

    /**
     * 场景统计变量历史值
     *
     * @param deviceLog 实体类
     * @return java.lang.String
     */
    @Override
    public List<String> selectStatsValue(DeviceLog deviceLog) {
        return deviceLogMapper.selectStatsValue(deviceLog);
    }

    @Override
    public List<ThingsModelLogCountVO> countThingsModelInvoke(DataCenterParam dataCenterParam) {
        return deviceLogMapper.countThingsModelInvoke(dataCenterParam);
    }

    @Override
    public DeviceLog selectLastReport(DeviceLog deviceLog) {
        return deviceLogMapper.selectLastReport(deviceLog);
    }
}
