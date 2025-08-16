package com.fastbee.iot.mapper;

import com.fastbee.framework.mybatis.mapper.BaseMapperX;
import com.fastbee.iot.domain.ModbusParams;

/**
 * 产品modbus配置参数Mapper接口
 * @date 2024-08-20
 */
public interface ModbusParamsMapper extends BaseMapperX<ModbusParams>
{
    /**
     * 根据产品io获取modbus配置
     * @param productId
     * @return
     */
    ModbusParams getModbusParamsByProductId(Long productId);

    /**
     * 根据设备id获取modbus配置
     * @param deviceId
     * @return
     */
    ModbusParams getModbusParamsByDeviceId(Long deviceId);
}
