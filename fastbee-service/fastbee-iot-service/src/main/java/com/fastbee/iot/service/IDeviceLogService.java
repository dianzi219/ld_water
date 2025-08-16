package com.fastbee.iot.service;

import com.alibaba.fastjson2.JSONObject;
import com.fastbee.iot.domain.DeviceLog;
import com.fastbee.iot.model.DeviceReport;
import com.fastbee.iot.model.HistoryModel;
import com.fastbee.iot.model.MonitorModel;
import java.util.Date;
import com.fastbee.iot.model.ThingsModelLogCountVO;
import com.fastbee.iot.model.param.DataCenterParam;
import java.util.List;
import java.util.Map;

/**
 * 设备日志Service接口
 *
 * @author kerwincui
 * @date 2022-01-13
 */
public interface IDeviceLogService
{
    /**
     * 查询设备日志
     *
     * @param logId 设备日志主键
     * @return 设备日志
     */
    public DeviceLog selectDeviceLogByLogId(Long logId);

    /**
     * 查询设备日志列表
     *
     * @param deviceLog 设备日志
     * @return 设备日志集合
     */
    public List<DeviceLog> selectDeviceLogList(DeviceLog deviceLog);

    /**
     * 查询设备监测数据
     *
     * @param deviceLog 设备日志
     * @return 设备日志集合
     */
    public List<MonitorModel> selectMonitorList(DeviceLog deviceLog);

    /**
     * 新增设备日志
     *
     * @param deviceLog 设备日志
     * @return 结果
     */
    public int insertDeviceLog(DeviceLog deviceLog);

    /**
     * 修改设备日志
     *
     * @param deviceLog 设备日志
     * @return 结果
     */
    public int updateDeviceLog(DeviceLog deviceLog);

    /**
     * 批量删除设备日志
     *
     * @param logIds 需要删除的设备日志主键集合
     * @return 结果
     */
    public int deleteDeviceLogByLogIds(Long[] logIds);

    /**
     * 删除设备日志信息
     *
     * @param logId 设备日志主键
     * @return 结果
     */
    public int deleteDeviceLogByLogId(Long logId);

    /**
     * 根据设备编号批量删除设备日志
     *
     * @param deviceNumber 需要删除的设备日志ID
     * @return 结果
     */
    public int deleteDeviceLogByDeviceNumber(String deviceNumber);

    /**
     * 查询设备历史数据
     *
     * @param deviceLog 设备日志
     * @return 设备日志集合
     */
    public Map<String, List<HistoryModel>> selectHistoryList(DeviceLog deviceLog);
    public List<DeviceReport> selectDeviceReportData(Date beginTime, Date endTime);
    /**
     * 查询物模型历史数据
     * @param deviceLog 设备日志
     * @return java.util.List<com.fastbee.iot.model.HistoryModel>
     */
    List<HistoryModel> listHistory(DeviceLog deviceLog);

    List<JSONObject> listhistoryGroupByCreateTime(DeviceLog deviceLog);

    /**
     * 统计设备物模型指令下发数量
     * @param dataCenterParam 传参
     * @return com.fastbee.common.core.domain.AjaxResult
     */
    List<ThingsModelLogCountVO> countThingsModelInvoke(DataCenterParam dataCenterParam);
}
