package com.fastbee.iot.cache.impl;

import com.fastbee.common.core.mq.DeviceStatusBo;
import com.fastbee.common.core.redis.RedisCache;
import com.fastbee.common.core.redis.RedisKeyBuilder;
import com.fastbee.common.enums.DeviceStatus;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.iot.cache.IDeviceCache;
import com.fastbee.iot.domain.Device;
import com.fastbee.iot.service.IDeviceService;
import com.fastbee.iot.service.ISubGatewayService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author bill
 */
@Service
@Slf4j
public class DeviceCacheImpl implements IDeviceCache {

    @Autowired
    private RedisCache redisCache;
    @Autowired
    private IDeviceService deviceService;


    /**
     * 更新设备状态
     * 如果设备状态保持不变，更新redis设备最新在线时间
     * 如果设备状态更改，更新redis同时，更新MySQL数据库设备状态
     *
     * @param dto dto
     */
    @Override
    public void updateDeviceStatusCache(DeviceStatusBo dto,Device device) {

        Optional.ofNullable(device).orElseThrow(() -> new ServiceException("设备不存在" + "[{" + dto.getSerialNumber() + "}]"));
        if (dto.getStatus() == DeviceStatus.ONLINE) {
            /*redis设备在线列表*/
            redisCache.zSetAdd(RedisKeyBuilder.buildDeviceOnlineListKey(), dto.getSerialNumber(), DateUtils.getTimestampSeconds());
            device.setStatus(DeviceStatus.ONLINE.getType());
        } else {
            /*在redis设备在线列表移除设备*/
            redisCache.zRem(RedisKeyBuilder.buildDeviceOnlineListKey(), dto.getSerialNumber());
            //更新一下mysql的设备状态为离线
            device.setStatus(DeviceStatus.OFFLINE.getType());
        }
        device.setUpdateTime(DateUtils.getNowDate());
        deviceService.updateDeviceStatusAndLocation(device, dto.getIp());

    }

    /**
     * 获取设备在线总数
     *
     * @return 设备在线总数
     */
    @Override
    public long deviceOnlineTotal() {
        return redisCache.zSize(RedisKeyBuilder.buildDeviceOnlineListKey());
    }


    /**
     * 批量更新redis缓存设备状态
     *
     * @param serialNumbers 设备列表
     * @param status        状态
     */
    @Override
    public void updateBatchDeviceStatusCache(List<String> serialNumbers, DeviceStatus status) {
        if (CollectionUtils.isEmpty(serialNumbers)) {
            return;
        }
        for (String serialNumber : serialNumbers) {
            DeviceStatusBo statusBo = new DeviceStatusBo();
            statusBo.setStatus(status);
            statusBo.setSerialNumber(serialNumber);
            //this.updateDeviceStatusCache(statusBo);
        }
    }

    /**
     * 系统停止时，更新所有设备为离线状态
     */
    @PreDestroy
    public void resetDeviceStatus(){
        deviceService.reSetDeviceStatus();
    }


}
