package com.fastbee.iot.mapper;

import java.util.List;
import com.fastbee.iot.domain.ModbusConfig;
import org.apache.ibatis.annotations.Param;

/**
 * modbus配置Mapper接口
 *
 * @author kerwincui
 * @date 2024-05-22
 */
public interface ModbusConfigMapper
{
    /**
     * 查询modbus配置
     *
     * @param id modbus配置主键
     * @return modbus配置
     */
    public ModbusConfig selectModbusConfigById(Long id);

    /**
     * 查询modbus配置列表
     *
     * @param modbusConfig modbus配置
     * @return modbus配置集合
     */
    public List<ModbusConfig> selectModbusConfigList(ModbusConfig modbusConfig);

    /**
     * 新增modbus配置
     *
     * @param modbusConfig modbus配置
     * @return 结果
     */
    public int insertModbusConfig(ModbusConfig modbusConfig);

    /**
     * 修改modbus配置
     *
     * @param modbusConfig modbus配置
     * @return 结果
     */
    public int updateModbusConfig(ModbusConfig modbusConfig);

    /**
     * 批量添加modbus配置
     * @param list
     * @return
     */
    int insertModbusConfigBatch(List<ModbusConfig> list);

    /**
     * 批量更新modbus配置
     * @param list
     * @return
     */
    int updateModbusConfigBatch(List<ModbusConfig> list);

    /**
     * 删除modbus配置
     *
     * @param id modbus配置主键
     * @return 结果
     */
    public int deleteModbusConfigById(Long id);

    /**
     * 批量删除modbus配置
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteModbusConfigByIds(Long[] ids);

    /**
     * 根据产品id和标识符获取modbus配置
     * @param productId
     * @param identity
     * @return
     */
    public ModbusConfig selectByIdentify(@Param("productId") Long productId, @Param("identity") String identity);

    /**
     * 获取modbus配置简要字段
     * @param config
     * @return
     */
    public List<ModbusConfig> selectShortListByProductId(ModbusConfig config);
}
