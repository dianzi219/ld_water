package com.fastbee.iot.convert;

import com.fastbee.iot.domain.ProductModbusJob;
import com.fastbee.iot.domain.ProductSubGateway;
import com.fastbee.iot.model.gateWay.ProductSubGatewayVO;
import com.fastbee.iot.model.modbus.ProductModbusJobVO;
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
public interface ProductModbusJobConvert {

    ProductModbusJobConvert INSTANCE = Mappers.getMapper(ProductModbusJobConvert.class);

    /**
     * @description: 单个实体类转换
     * @param: deviceRecord 设备记录
     * @return: com.fastbee.iot.model.DeviceRecordVO
     */
    ProductModbusJobVO convertProductModbusJobVO(ProductModbusJob productModbusJob);

    /**
     * @description: 集合转换
     * @param: deviceRecordList 设备记录集合
     * @return: java.util.List<com.fastbee.iot.model.DeviceRecordVO>
     */
    List<ProductModbusJobVO> convertProductModbusJobVOList(List<ProductModbusJob> ProductModbusJobList);

}
