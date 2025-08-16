package com.fastbee.iot.service.impl;

import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.core.protocol.modbus.ModbusCode;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.SecurityUtils;
import com.fastbee.common.utils.poi.ExcelUtil;
import com.fastbee.iot.cache.IModbusConfigCache;
import com.fastbee.iot.domain.DeviceJob;
import com.fastbee.iot.domain.ModbusConfig;
import com.fastbee.iot.domain.ThingsModel;
import com.fastbee.iot.mapper.ModbusConfigMapper;
import com.fastbee.iot.model.ThingsModels.PropertyDto;
import com.fastbee.iot.model.ThingsModels.ThingsModelValueItem;
import com.fastbee.iot.model.modbus.ModbusDataImport;
import com.fastbee.iot.model.modbus.ModbusIoImport;
import com.fastbee.iot.service.IDeviceJobService;
import com.fastbee.iot.service.IModbusConfigService;
import com.fastbee.iot.service.IThingsModelService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.quartz.SchedulerException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * modbus配置Service业务层处理
 *
 * @author kerwincui
 * @date 2024-05-22
 */
@Service
@Slf4j
public class ModbusConfigServiceImpl implements IModbusConfigService
{
    @Resource
    private ModbusConfigMapper modbusConfigMapper;
    @Resource
    private IThingsModelService thingsModelService;
    @Resource
    private IModbusConfigCache modbusConfigCache;

    /**
     * 查询modbus配置
     *
     * @param id modbus配置主键
     * @return modbus配置
     */
    @Override
    public ModbusConfig selectModbusConfigById(Long id)
    {
        return modbusConfigMapper.selectModbusConfigById(id);
    }

    /**
     * 查询modbus配置列表
     *
     * @param modbusConfig modbus配置
     * @return modbus配置
     */
    @Override
    public List<ModbusConfig> selectModbusConfigList(ModbusConfig modbusConfig)
    {
        return modbusConfigMapper.selectModbusConfigList(modbusConfig);
    }

    /**
     * 新增modbus配置
     *
     * @param modbusConfig modbus配置
     * @return 结果
     */
    @Override
    public int insertModbusConfig(ModbusConfig modbusConfig)
    {
        modbusConfig.setCreateTime(DateUtils.getNowDate());
        return modbusConfigMapper.insertModbusConfig(modbusConfig);
    }

    /**
     * 修改modbus配置
     *
     * @param modbusConfig modbus配置
     * @return 结果
     */
    @Override
    public int updateModbusConfig(ModbusConfig modbusConfig)
    {
        modbusConfig.setUpdateTime(DateUtils.getNowDate());
        return modbusConfigMapper.updateModbusConfig(modbusConfig);
    }

    /**
     * 批量添加或更新modbus配置
     * @param modbusConfigList
     * @param productId
     * @return
     */
    @Override
    public void addOrUpModbusConfigBatch(List<ModbusConfig> modbusConfigList, Long productId,Long[] ids){
        assert !Objects.isNull(productId) : "产品id不能为空";
        assert !CollectionUtils.isEmpty(modbusConfigList) : "modbus配置数据不能为空";
        if (!CollectionUtils.isEmpty(Arrays.asList(ids))){
            //要删除的id集合不为空，则先进行删除操作
            this.deleteModbusConfigByIds(ids);
        }
        for (ModbusConfig config : modbusConfigList) {
            if (Objects.isNull(config.getId())){
                config.setCreateBy(SecurityUtils.getUsername());
                config.setCreateTime(DateUtils.getNowDate());
                config.setProductId(productId);
                this.insertModbusConfig(config);
            }else {
                config.setUpdateBy(SecurityUtils.getUsername());
                config.setUpdateTime(DateUtils.getNowDate());
                config.setProductId(productId);
                this.updateModbusConfig(config);
            }
        }
        modbusConfigCache.setModbusConfigCacheByProductId(productId);
    }

    /**
     * 批量删除modbus配置
     *
     * @param ids 需要删除的modbus配置主键
     * @return 结果
     */
    @Override
    public int deleteModbusConfigByIds(Long[] ids)
    {
        return modbusConfigMapper.deleteModbusConfigByIds(ids);
    }

    /**
     * 删除modbus配置信息
     *
     * @param id modbus配置主键
     * @return 结果
     */
    @Override
    public int deleteModbusConfigById(Long id)
    {
        return modbusConfigMapper.deleteModbusConfigById(id);
    }

    /**
     * IO寄存器导入
     * @param list
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult importIOModbus(List<ModbusIoImport> list, Long productId, Integer type){
        assert !CollectionUtils.isEmpty(list) : "导入IO寄存器列表为空";
        assert !Objects.isNull(productId) : "产品id为空";
        int success = 0;
        int failure = 0;
        StringBuilder succSb = new StringBuilder();
        StringBuilder failSb = new StringBuilder();
        for (ModbusIoImport ioModbus : list) {
            try {
                ThingsModelValueItem thingModels = thingsModelService.getSingleThingModels(productId, ioModbus.getIdentifier());
                String dataType = thingModels.getDatatype().getType();
                ModbusConfig select = this.selectByIdentify(productId, ioModbus.getIdentifier());
                if (Objects.isNull(select)) {
                    ModbusConfig modbusConfig = new ModbusConfig();
                    BeanUtils.copyProperties(ioModbus, modbusConfig);
                    modbusConfig.setCreateTime(DateUtils.getNowDate());
                    modbusConfig.setProductId(productId);
                    modbusConfig.setDelFlag("0");
                    modbusConfig.setType(type);
                    this.insertModbusConfig(modbusConfig);
                    success++;
                    succSb.append("<br/>").append(success).append(",ADDRESS: ").append(ioModbus.getIdentifier());
                }else {
                    failure++;
                    failSb.append("<br/>").append(failure).append(",repeat: ").append(ioModbus.getIdentifier()).append("Import Error");
                }
                //}else {
                //    failure++;
                //    failSb.append("<br/>").append(failure).append(",NO IO TYPE: ").append(ioModbus.getIdentifier()).append("Import Error");
                //}
            }catch (Exception e){
                log.error("error：", e);
                failure++;
                failSb.append("<br/>").append(failure).append(",ADDRESS: ").append(ioModbus.getIdentifier()).append("Import Error");
            }
        }
        if (failure > 0) {
            throw new ServiceException(failSb.toString());
        }
        modbusConfigCache.setModbusConfigCacheByProductId(productId);
        return AjaxResult.success(succSb.toString());
    }

    /**
     * 数据寄存器导入
     * @param list
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult importDataModbus(List<ModbusDataImport> list, Long productId, Integer type){
        assert !CollectionUtils.isEmpty(list) : "导入数据寄存器列表为空";
        assert !Objects.isNull(productId) : "产品id为空";
        int success = 0;
        int failure = 0;
        StringBuilder succSb = new StringBuilder();
        StringBuilder failSb = new StringBuilder();
        for (ModbusDataImport dataModbus : list) {
            try {
                ThingsModelValueItem thingModels = thingsModelService.getSingleThingModels(productId, dataModbus.getIdentifier());
                String dataType = thingModels.getDatatype().getType();
                if (!dataType.equals("bool")) {
                    ModbusConfig select = this.selectByIdentify(productId, dataModbus.getIdentifier());
                    if (Objects.isNull(select)) {
                        ModbusConfig modbusConfig = new ModbusConfig();
                        BeanUtils.copyProperties(dataModbus, modbusConfig);
                        modbusConfig.setCreateTime(DateUtils.getNowDate());
                        modbusConfig.setProductId(productId);
                        modbusConfig.setDelFlag("0");
                        modbusConfig.setType(type);
                        this.insertModbusConfig(modbusConfig);
                        success++;
                        succSb.append("<br/>").append(success).append(",ADDRESS: ").append(dataModbus.getIdentifier());
                    }else {
                        failure++;
                        failSb.append("<br/>").append(failure).append(",NO IO TYPE: ").append(dataModbus.getIdentifier()).append("Import Error");
                    }
                }else {
                    failure++;
                    failSb.append("<br/>").append(failure).append(",NO DATA TYPE: ").append(dataModbus.getIdentifier()).append("Import Error");
                }
            }catch (Exception e){
                log.error("error：", e);
                failure++;
                failSb.append("<br/>").append(failure).append(",ADDRESS: ").append(dataModbus.getIdentifier()).append("Import Error");
            }
        }
        if (failure > 0) {
            throw new ServiceException(failSb.toString());
        }
        modbusConfigCache.setModbusConfigCacheByProductId(productId);
        return AjaxResult.success(succSb.toString());
    }

    /**
     * 转换IO寄存器导出
     * @param config
     * @return
     */
    public List<ModbusIoImport> exportTransIO(ModbusConfig config){
        List<ModbusIoImport> result = new ArrayList<>();
        List<ModbusConfig> modbusConfigs = this.selectModbusConfigList(config);
        for (ModbusConfig modbusConfig : modbusConfigs) {
            ModbusIoImport ioImport = new ModbusIoImport();
            BeanUtils.copyProperties(modbusConfig,ioImport);
            result.add(ioImport);
        }
        return result;
    }

    /**
     * 转换数据寄存器导出
     * @param config
     * @return
     */
    public List<ModbusDataImport> exportTransData(ModbusConfig config){
        List<ModbusDataImport> result = new ArrayList<>();
        List<ModbusConfig> modbusConfigs = this.selectModbusConfigList(config);
        for (ModbusConfig modbusConfig : modbusConfigs) {
            ModbusDataImport dataImport = new ModbusDataImport();
            BeanUtils.copyProperties(modbusConfig,dataImport);
            result.add(dataImport);
        }
        return result;
    }

    /**
     * 根据产品id和标识符获取modbus配置
     * @param productId
     * @param identity
     * @return
     */
    @Override
    public ModbusConfig selectByIdentify(Long productId,String identity){
        return modbusConfigMapper.selectByIdentify(productId,identity);
    }

    /**
     * 获取modbus配置简要字段
     * @param config
     * @return
     */
    @Override
    public List<ModbusConfig> selectShortListByProductId(ModbusConfig config){
        return modbusConfigMapper.selectShortListByProductId(config);
    }

    /**
     * 获取组装后的modbus参数配置集合
     * @param productId
     * @return
     */
    public Map<String,List<ModbusConfig>> getPollingStr(Long productId){
        Map<String,List<ModbusConfig>> results = new HashMap<>();
        ModbusConfig modbusConfig = new ModbusConfig();
        modbusConfig.setProductId(productId);
        List<ModbusConfig> modbusConfigList = this.selectShortListByProductId(modbusConfig);
        Map<Integer, List<ModbusConfig>> listMap = modbusConfigList.stream().collect(Collectors.groupingBy(ModbusConfig::getType));
        //处理读线圈
        List<ModbusConfig> IOList = listMap.get(1);
        // 01功能码
        List<ModbusConfig> IO01List = IOList.stream().filter(m -> m.getIsReadonly() == 0)
                .sorted(Comparator.comparing(ModbusConfig::getAddress)).collect(Collectors.toList());
        //02功能码
        List<ModbusConfig> IO02List = IOList.stream().filter(m -> m.getIsReadonly() == 1)
                .sorted(Comparator.comparing(ModbusConfig::getAddress)).collect(Collectors.toList());

        //处理读寄存器
        List<ModbusConfig> dataList = listMap.get(2);
        //03功能码
        List<ModbusConfig> data03Lit = dataList.stream().filter(m -> m.getIsReadonly() == 0)
                .sorted(Comparator.comparing(ModbusConfig::getAddress)).collect(Collectors.toList());
        //04功能码
        List<ModbusConfig> data04List = dataList.stream().filter(m -> m.getIsReadonly() == 1)
                .sorted(Comparator.comparing(ModbusConfig::getAddress)).collect(Collectors.toList());
        results.put(ModbusCode.Read01.getHex(),IO01List);
        results.put(ModbusCode.Read02.getHex(),IO02List);
        results.put(ModbusCode.Read03.getHex(),data03Lit);
        results.put(ModbusCode.Read04.getHex(),data04List);
        return results;
    }
}
