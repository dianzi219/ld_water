package com.fastbee.iot.cache;

import com.fastbee.common.core.mq.DeviceStatusBo;
import com.fastbee.common.enums.DeviceStatus;
import com.fastbee.iot.domain.Device;

import java.util.List;

/**
 * 设备缓存
 * @author bill
 */
public interface IDeviceCache {

    /**
     * 更新设备状态
     * @param dto dto
     */
    public void updateDeviceStatusCache(DeviceStatusBo dto,Device device);

    /**
     * 获取设备在线总数
     * @return 设备在线总数
     */
    public long deviceOnlineTotal();


    /**
     * 批量更新redis缓存设备状态
     * @param serialNumbers 设备列表
     * @param status 状态
     */
    void updateBatchDeviceStatusCache(List<String> serialNumbers, DeviceStatus status);

}
