package com.fastbee.iot.data.service;

import java.util.List;

import com.fastbee.common.enums.OTAUpgrade;
import com.fastbee.iot.domain.FirmwareTaskDetail;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fastbee.iot.model.FirmwareTaskDetailInput;
import com.fastbee.iot.model.FirmwareTaskDetailOutput;
import com.fastbee.iot.model.FirmwareTaskDeviceStatistic;

/**
 * 固件升级任务详细对象Service接口
 *
 * @author kerwincui
 * @date 2024-08-18
 */
public interface IFirmwareTaskDetailService extends IService<FirmwareTaskDetail>
{

    /**
     * 查询固件升级任务详细对象列表
     *
     * @param firmwareTaskDetail 固件升级任务详细对象
     * @return 固件升级任务详细对象集合
     */
    public List<FirmwareTaskDetail> selectFirmwareTaskDetailList(FirmwareTaskDetail firmwareTaskDetail);


    /**
     * 查询固件升级任务详细
     * @param taskId
     * @param serialNumber
     * @return
     */
    public FirmwareTaskDetail selectFirmwareTaskDetailByTaskIdAndSerialNumber(Long taskId, String serialNumber);



    /**
     * 更新升级状态
     * @param taskId
     * @param serialNumber
     * @param otaUpgrade
     */
    public void updateStatus(Long taskId, String serialNumber, OTAUpgrade otaUpgrade);

    public List<FirmwareTaskDetailOutput> selectFirmwareTaskDetailListByFirmwareId(FirmwareTaskDetailInput firmwareTaskDetailInput);

    public List<FirmwareTaskDeviceStatistic> deviceStatistic(FirmwareTaskDetailInput firmwareTaskDetailInput);
}
