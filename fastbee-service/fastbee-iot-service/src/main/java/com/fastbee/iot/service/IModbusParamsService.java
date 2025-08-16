package com.fastbee.iot.service;

import java.util.List;
import com.fastbee.iot.domain.ModbusParams;
import com.baomidou.mybatisplus.extension.service.IService;
/**
 * 产品modbus配置参数Service接口
 *
 * @author zhuangpeng.li
 * @date 2024-08-20
 */
public interface IModbusParamsService extends IService<ModbusParams>
{

    /**
     * 查询产品modbus配置参数列表
     *
     * @param modbusParams 产品modbus配置参数
     * @return 产品modbus配置参数集合
     */
     List<ModbusParams> selectModbusParamsList(ModbusParams modbusParams);

    /**
     * 查询产品modbus配置参数
     *
     * @param id 主键
     * @return 产品modbus配置参数
     */
     ModbusParams selectModbusParamsById(Long id);

    /**
     * 查询产品modbus配置参数
     *
     * @param id 主键
     * @return 产品modbus配置参数
     */
    ModbusParams queryByIdWithCache(Long id);

    /**
     * 新增产品modbus配置参数
     *
     * @param modbusParams 产品modbus配置参数
     * @return 是否新增成功
     */
    Boolean insertWithCache(ModbusParams modbusParams);

    /**
     * 更新或新增
     * @param modbusParams
     */
    boolean addOrUpdate(ModbusParams modbusParams);

    /**
     * 修改产品modbus配置参数
     *
     * @param modbusParams 产品modbus配置参数
     * @return 是否修改成功
     */
    Boolean updateWithCache(ModbusParams modbusParams);

    /**
     * 校验并批量删除产品modbus配置参数信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithCacheByIds(Long[] ids, Boolean isValid);

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

    /**
     * @description: 根据产品id查询配置
     * @param: editSubProductIdList 产品id集合
     * @return: java.util.List<com.fastbee.iot.domain.ModbusParams>
     */
    List<ModbusParams> selectModbusParamsListByProductIds(List<Long> productIdList);
}
