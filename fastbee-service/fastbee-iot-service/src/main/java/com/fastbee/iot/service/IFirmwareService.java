package com.fastbee.iot.service;

import java.util.List;
import com.fastbee.iot.domain.Firmware;
import com.baomidou.mybatisplus.extension.service.IService;
/**
 * 产品固件Service接口
 *
 * @author kerwincui
 * @date 2024-08-18
 */
public interface IFirmwareService extends IService<Firmware>
{

    /**
     * 查询产品固件列表
     *
     * @param firmware 产品固件
     * @return 产品固件集合
     */
     List<Firmware> selectFirmwareList(Firmware firmware);


    /**
     * 查询待升级固件版本列表
     * @param firmware
     * @return
     */
     List<Firmware> selectUpGradeVersionList(Firmware firmware);

    /**
     * 新增产品固件
     *
     * @param firmware 产品固件
     * @return 结果
     */
     int insertFirmware(Firmware firmware);


    /**
     * 查询设备最新固件
     * @param deviceId 产品固件主键
     * @param firmwareType 固件类型
     * @return 产品固件
     */
    public Firmware selectLatestFirmware(Long deviceId, Long firmwareType);

    /**
     *
     * @param
     * @return
     */
    int deleteBatchByIds(List<Long> firmwareIdList);
}
