package com.fastbee.iot.convert;

import com.fastbee.iot.domain.FirmwareTask;
import com.fastbee.iot.model.FirmwareTaskInput;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @description: 网关与子产品关联对象
 * @author admin
 * @date 2024-07-19 16:14
 * @version 1.0
 */
@Mapper
public interface FirmwareTaskConvert {

    FirmwareTaskConvert INSTANCE = Mappers.getMapper(FirmwareTaskConvert.class);

    /**
     * @description: 单个实体类转换
     * @param: deviceRecord 设备记录
     * @return: com.fastbee.iot.model.DeviceRecordVO
     */
    FirmwareTaskInput convertFirmwareTaskInput(FirmwareTask firmwareTask);

    /**
     * @description: 集合转换
     * @param: deviceRecordList 设备记录集合
     * @return: java.util.List<com.fastbee.iot.model.DeviceRecordVO>
     */
    List<FirmwareTaskInput> convertFirmwareTaskInputList(List<FirmwareTask> FirmwareTaskList);

    /**
     * @description: 单个实体类转换
     * @param: deviceRecord 设备记录
     * @return: com.fastbee.iot.model.DeviceRecordVO
     */
    FirmwareTask inputConvertFirmwareTask(FirmwareTaskInput firmwareTaskInput);

}
