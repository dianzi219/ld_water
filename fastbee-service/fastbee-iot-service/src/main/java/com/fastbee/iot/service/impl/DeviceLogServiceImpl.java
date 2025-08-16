package com.fastbee.iot.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.iot.domain.Device;
import com.fastbee.iot.domain.DeviceLog;
import com.fastbee.iot.domain.ThingsModel;
import com.fastbee.iot.model.DeviceReport;
import com.fastbee.iot.model.HistoryModel;
import com.fastbee.iot.service.IDeviceService;
import com.fastbee.iot.service.IThingsModelService;
import com.fastbee.iot.model.ThingsModelLogCountVO;
import com.fastbee.iot.model.param.DataCenterParam;
import com.fastbee.iot.tdengine.service.ILogService;
import com.fastbee.iot.mapper.DeviceLogMapper;
import com.fastbee.iot.model.MonitorModel;
import com.fastbee.iot.service.IDeviceLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 设备日志Service业务层处理
 *
 * @author kerwincui
 * @date 2022-01-13
 */
@Service
@Slf4j
public class DeviceLogServiceImpl implements IDeviceLogService {
    @Autowired
    private DeviceLogMapper deviceLogMapper;

    @Autowired
    private ILogService logService;

    @Autowired
    private IThingsModelService thingsModelService;

    @Autowired
    private IDeviceService deviceService;

    /**
     * 查询设备日志
     *
     * @param logId 设备日志主键
     * @return 设备日志
     */
    @Override
    public DeviceLog selectDeviceLogByLogId(Long logId) {
        return deviceLogMapper.selectDeviceLogByLogId(logId);
    }

    /**
     * 查询设备日志列表
     *
     * @param deviceLog 设备日志
     * @return 设备日志
     */
    @Override
    public List<DeviceLog> selectDeviceLogList(DeviceLog deviceLog) {
        if (deviceLog.getIsMonitor() == null) {
            deviceLog.setIsMonitor(0);
        }
        return logService.selectDeviceLogList(deviceLog);
    }

    /**
     * 查询设备监测数据
     *
     * @param deviceLog 设备日志
     * @return 设备日志
     */
    @Override
    public List<MonitorModel> selectMonitorList(DeviceLog deviceLog) {
        return logService.selectMonitorList(deviceLog);
    }

    /**
     * 新增设备日志
     *
     * @param deviceLog 设备日志
     * @return 结果
     */
    @Override
    public int insertDeviceLog(DeviceLog deviceLog) {
        deviceLog.setCreateTime(DateUtils.getNowDate());
        return logService.saveDeviceLog(deviceLog);
    }

    /**
     * 修改设备日志
     *
     * @param deviceLog 设备日志
     * @return 结果
     */
    @Override
    public int updateDeviceLog(DeviceLog deviceLog) {
        return deviceLogMapper.updateDeviceLog(deviceLog);
    }

    /**
     * 批量删除设备日志
     *
     * @param logIds 需要删除的设备日志主键
     * @return 结果
     */
    @Override
    public int deleteDeviceLogByLogIds(Long[] logIds) {
        return deviceLogMapper.deleteDeviceLogByLogIds(logIds);
    }

    /**
     * 根据设备Ids批量删除设备日志
     *
     * @param deviceNumber 需要删除数据的设备Ids
     * @return 结果
     */
    @Override
    public int deleteDeviceLogByDeviceNumber(String deviceNumber) {
        return deviceLogMapper.deleteDeviceLogByDeviceNumber(deviceNumber);
    }

    /**
     * 删除设备日志信息
     *
     * @param logId 设备日志主键
     * @return 结果
     */
    @Override
    public int deleteDeviceLogByLogId(Long logId) {
        return deviceLogMapper.deleteDeviceLogByLogId(logId);
    }

    /**
     * 查询设备历史数据
     *
     * @param deviceLog 设备日志
     * @return 设备日志集合
     */
    @Override
    public Map<String, List<HistoryModel>> selectHistoryList(DeviceLog deviceLog) {
        return logService.selectHistoryList(deviceLog);
    }

    @Override
    public List<DeviceReport> selectDeviceReportData(Date beginTime, Date endTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String beginTimeString = formatter.format(beginTime);
        String endTimeString = formatter.format(endTime);
        Device dev = new Device();
        dev.setStatus(3);
        //查询在线设备
        List<Device> deviceList = deviceService.selectDeviceList(dev);
        List<DeviceReport> deviceReportList = new ArrayList<>();
        for (Device device : deviceList) {
            DeviceReport deviceReport = new DeviceReport();
            deviceReport.setSerialNumber(device.getSerialNumber());
            List<DeviceLog> reportList = new ArrayList<>();
            List<String> unReportList = new ArrayList<>();
            // 查询所有物模型id
            ThingsModel thingsModel = new ThingsModel();
            thingsModel.setType(1);
            thingsModel.setIsHistory(1);
            thingsModel.setProductId(device.getProductId());
            List<ThingsModel> list = thingsModelService.selectThingsModelList(thingsModel);
            for (ThingsModel item : list) {
                DeviceLog deviceLog = new DeviceLog();
                deviceLog.setSerialNumber(device.getSerialNumber());
                deviceLog.setIdentity(item.getIdentifier());
                deviceLog.setBeginTime(beginTimeString);
                deviceLog.setEndTime(endTimeString);
                DeviceLog lastdeviceLog = logService.selectLastReport(deviceLog);
                if (lastdeviceLog != null) {
                    reportList.add(lastdeviceLog);
                } else {
                    unReportList.add(item.getIdentifier());
                }
            }
            deviceReport.setReportList(reportList);
            deviceReport.setUnReportList(unReportList);
            deviceReportList.add(deviceReport);
        }
        //log.info("deviceReportList:{}", deviceReportList);
        return deviceReportList;
    }

    @Override
    public List<HistoryModel> listHistory(DeviceLog deviceLog) {
        return logService.listHistory(deviceLog);
    }

    @Override
    public List<JSONObject> listhistoryGroupByCreateTime(DeviceLog deviceLog) {
        List<HistoryModel> modelList = logService.listhistoryGroupByCreateTime(deviceLog);
        List<JSONObject> resultList = new ArrayList<>();
        for (HistoryModel historyModel : modelList) {
            List<String> identityList = StringUtils.str2List(historyModel.getIdentity(), ",", true, true);
            List<String> valueList = StringUtils.str2List(historyModel.getValue(), ",", true, true);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("time", DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, historyModel.getTime()));
            for (int i = 0; i < identityList.size(); i++) {
                jsonObject.put("name" + (i + 1), identityList.get(i));
                jsonObject.put("value" + (i + 1), valueList.get(i));
            }
            resultList.add(jsonObject);
        }
        return resultList;
    }

    @Override
    public List<ThingsModelLogCountVO> countThingsModelInvoke(DataCenterParam dataCenterParam) {
        return logService.countThingsModelInvoke(dataCenterParam);
    }
}
