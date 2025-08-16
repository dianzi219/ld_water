package com.fastbee.iot.service;

import com.fastbee.iot.domain.AlertLog;
import com.fastbee.iot.model.AlertCountVO;
import com.fastbee.iot.model.DeviceAlertCount;
import com.fastbee.iot.model.param.DataCenterParam;

import java.util.List;

/**
 * 设备告警Service接口
 * 
 * @author kerwincui
 * @date 2022-01-13
 */
public interface IAlertLogService 
{
    /**
     * 查询设备告警
     * 
     * @param alertLogId 设备告警主键
     * @return 设备告警
     */
    public AlertLog selectAlertLogByAlertLogId(Long alertLogId);

    /**
     * 查询设备告警列表
     * 
     * @param alertLog 设备告警
     * @return 设备告警集合
     */
    public List<AlertLog> selectAlertLogList(AlertLog alertLog);
    public List<AlertLog> selectAlertLogListByCreateBy(String createBy, String remark, Integer status);
    /**
     * 查询设备告警列表总数
     *
     * @param alertLog 设备告警
     * @return 设备告警集合
     */
    public Long selectAlertLogListCount(AlertLog alertLog);

    public List<DeviceAlertCount> selectDeviceAlertCount();
    public DeviceAlertCount selectDeviceAlertCountBySN(String serialNumber);
    public List<DeviceAlertCount> selectSceneAlertCount();
    public DeviceAlertCount selectSceneAlertCountBySceneId(String sceneId);
    /**
     * 新增设备告警
     * 
     * @param alertLog 设备告警
     * @return 结果
     */
    public int insertAlertLog(AlertLog alertLog);
    public int insertAlertLogBatch(List<AlertLog> alertLogList);

    /**
     * 修改设备告警
     * 
     * @param alertLog 设备告警
     * @return 结果
     */
    public int updateAlertLog(AlertLog alertLog);

    public int updateAlertLogStatus(AlertLog alertLog);
    /**
     * 批量删除设备告警
     * 
     * @param alertLogIds 需要删除的设备告警主键集合
     * @return 结果
     */
    public int deleteAlertLogByAlertLogIds(Long[] alertLogIds);

    /**
     * 删除设备告警信息
     * 
     * @param alertLogId 设备告警主键
     * @return 结果
     */
    public int deleteAlertLogByAlertLogId(Long alertLogId);

    /**
     * 通过设备编号删除设备告警信息
     *
     * @param serialNumber 设备告警主键
     * @return 结果
     */
    public int deleteAlertLogBySerialNumber(String serialNumber);

    /**
     * 统计告警处理信息
     * @param dataCenterParam 传参
     * @return com.fastbee.iot.model.AlertCountVO
     */
    List<AlertCountVO> countAlertProcess(DataCenterParam dataCenterParam);

    /**
     * 统计告警级别信息
     * @param dataCenterParam 传参
     * @return com.fastbee.iot.model.AlertCountVO
     */
    List<AlertCountVO> countAlertLevel(DataCenterParam dataCenterParam);
}
