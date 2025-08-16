package com.fastbee.iot.service;

import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.iot.domain.DeviceJob;
import com.fastbee.iot.domain.ModbusConfig;
import com.fastbee.iot.model.modbus.ModbusDataImport;
import com.fastbee.iot.model.modbus.ModbusIoImport;
import org.apache.ibatis.annotations.Param;
import org.quartz.SchedulerException;

import java.util.List;
import java.util.Map;

/**
 * modbus配置Service接口
 *
 * @author kerwincui
 * @date 2024-05-22
 */
public interface IModbusConfigService
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
     * 批量添加或更新modbus配置
     * @param modbusConfigList
     * @param productId
     * @return
     */
    void addOrUpModbusConfigBatch(List<ModbusConfig> modbusConfigList, Long productId,Long[] ids);


    /**
     * 批量删除modbus配置
     *
     * @param ids 需要删除的modbus配置主键集合
     * @return 结果
     */
    public int deleteModbusConfigByIds(Long[] ids);

    /**
     * 删除modbus配置信息
     *
     * @param id modbus配置主键
     * @return 结果
     */
    public int deleteModbusConfigById(Long id);

    /**
     * 寄存器导入
     * @param list
     * @return
     */
    public AjaxResult importIOModbus(List<ModbusIoImport> list, Long productId, Integer type);

    /**
     * 数据寄存器导入
     * @param list
     * @return
     */
    public AjaxResult importDataModbus(List<ModbusDataImport> list, Long productId, Integer type);

    /**
     * 转换IO寄存器导出
     * @param config
     * @return
     */
    public List<ModbusIoImport> exportTransIO(ModbusConfig config);

    /**
     * 转换数据寄存器导出
     * @param config
     * @return
     */
    public List<ModbusDataImport> exportTransData(ModbusConfig config);

    /**
     * 根据产品id和标识符获取modbus配置
     * @param productId
     * @param identity
     * @return
     */
    ModbusConfig selectByIdentify(Long productId,String identity);

    /**
     * 获取modbus配置简要字段
     * @param config
     * @return
     */
    List<ModbusConfig> selectShortListByProductId(ModbusConfig config);

    /**
     * 获取组装后的modbus参数配置集合
     * @param productId
     * @return
     */
     Map<String,List<ModbusConfig>> getPollingStr(Long productId);


}
