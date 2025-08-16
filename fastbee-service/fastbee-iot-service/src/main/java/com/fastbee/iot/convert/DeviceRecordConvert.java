package com.fastbee.iot.convert;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fastbee.iot.domain.DeviceRecord;
import com.fastbee.iot.model.DeviceRecordVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @description: 设备记录转换工具类
 * @author admin
 * @date 2024-07-19 16:14
 * @version 1.0
 */
@Mapper
public interface DeviceRecordConvert {

    DeviceRecordConvert INSTANCE = Mappers.getMapper(DeviceRecordConvert.class);

    /**
     * @description: 单个实体类转换
     * @param: deviceRecord 设备记录
     * @return: com.fastbee.iot.model.DeviceRecordVO
     */
    DeviceRecordVO convertDeviceRecordVO(DeviceRecord deviceRecord);

    /**
     * @description: 集合转换
     * @param: deviceRecordList 设备记录集合
     * @return: java.util.List<com.fastbee.iot.model.DeviceRecordVO>
     */
    List<DeviceRecordVO> convertDeviceRecordVOList(List<DeviceRecord> deviceRecordList);

    Page<DeviceRecordVO> convertDeviceRecordVOPage(Page<DeviceRecord> deviceRecordPage);

}
