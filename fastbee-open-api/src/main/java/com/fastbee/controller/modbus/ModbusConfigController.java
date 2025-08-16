package com.fastbee.controller.modbus;

import com.fastbee.common.annotation.Log;
import com.fastbee.common.core.controller.BaseController;
import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.core.page.TableDataInfo;
import com.fastbee.common.enums.BusinessType;
import com.fastbee.common.utils.MessageUtils;
import com.fastbee.common.utils.poi.ExcelUtil;
import com.fastbee.iot.domain.ModbusConfig;
import com.fastbee.iot.domain.ThingsModel;
import com.fastbee.iot.model.modbus.ModbusConfigVO;
import com.fastbee.iot.model.modbus.ModbusDataImport;
import com.fastbee.iot.model.modbus.ModbusIoImport;
import com.fastbee.iot.service.IModbusConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.aspectj.weaver.ast.Var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;

/**
 * modbus配置Controller
 *
 * @author kerwincui
 * @date 2024-05-22
 */
@RestController
@RequestMapping("/modbus/config")
@Api(tags = "modbus配置")
public class ModbusConfigController extends BaseController
{
    @Autowired
    private IModbusConfigService modbusConfigService;

    /**
     * 查询modbus配置列表
     */
    @PreAuthorize("@ss.hasPermi('modbus:config:list')")
    @GetMapping("/list")
    public TableDataInfo list(ModbusConfig modbusConfig)
    {
        startPage();
        List<ModbusConfig> list = modbusConfigService.selectModbusConfigList(modbusConfig);
        return getDataTable(list);
    }

    /**
     * 获取modbus配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('modbus:config:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(modbusConfigService.selectModbusConfigById(id));
    }

    /**
     * 新增modbus配置
     */
    @PreAuthorize("@ss.hasPermi('modbus:config:add')")
    @Log(title = "modbus配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ModbusConfig modbusConfig)
    {
        return toAjax(modbusConfigService.insertModbusConfig(modbusConfig));
    }

    /**
     * 修改modbus配置
     */
    @PreAuthorize("@ss.hasPermi('modbus:config:edit')")
    @Log(title = "modbus配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ModbusConfig modbusConfig)
    {
        return toAjax(modbusConfigService.updateModbusConfig(modbusConfig));
    }

    /**
     * 批量新增或修改modbus配置
     */
    @PreAuthorize("@ss.hasPermi('modbus:config:add')")
    @PostMapping("/addBatch")
    @ApiOperation("批量新增或修改modbus配置")
    public AjaxResult addBatch(@RequestBody ModbusConfigVO modbusConfigVO)
    {
        modbusConfigService.addOrUpModbusConfigBatch(modbusConfigVO.getConfigList(),
                modbusConfigVO.getProductId(), modbusConfigVO.getDelIds());
        return AjaxResult.success();
    }


    /**
     * 删除modbus配置
     */
    @PreAuthorize("@ss.hasPermi('modbus:config:remove')")
    @Log(title = "modbus配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(modbusConfigService.deleteModbusConfigByIds(ids));
    }


    /**
     * 导入MODBUS配置
     */
    @PreAuthorize("@ss.hasPermi('modbus:config:import')")
    @ApiOperation(value = "导入MODBUS配置")
    @PostMapping(value = "/importModbus")
    public AjaxResult importModbus(MultipartFile file, Integer type,Long productId) throws Exception{
        assert !Objects.isNull(type) : MessageUtils.message("modbus.type.null");
        if (type == 1){
            ExcelUtil<ModbusIoImport> excelUtil = new ExcelUtil<>(ModbusIoImport.class);
            List<ModbusIoImport> list = excelUtil.importExcel(file.getInputStream());
            return modbusConfigService.importIOModbus(list, productId, type);
        }else {
            ExcelUtil<ModbusDataImport> excelUtil = new ExcelUtil<>(ModbusDataImport.class);
            List<ModbusDataImport> list = excelUtil.importExcel(file.getInputStream());
            return modbusConfigService.importDataModbus(list, productId, type);
        }
    }


    /**
     * 导出modbus配置列表
     */
    @PreAuthorize("@ss.hasPermi('modbus:config:export')")
    @PostMapping(value = "/exportModbus")
    @ApiOperation(value = "导出MODBUS配置")
    public void export(HttpServletResponse response, ModbusConfig modbusConfig)
    {
        Integer type = modbusConfig.getType();
        assert !Objects.isNull(type) : MessageUtils.message("modbus.type.null");
        if (type == 1){
            List<ModbusIoImport> list = modbusConfigService.exportTransIO(modbusConfig);
            ExcelUtil<ModbusIoImport> util = new ExcelUtil<ModbusIoImport>(ModbusIoImport.class);
            util.exportExcel(response, list, "modbus配置数据");
        }else {
            List<ModbusDataImport> list = modbusConfigService.exportTransData(modbusConfig);
            ExcelUtil<ModbusDataImport> util = new ExcelUtil<ModbusDataImport>(ModbusDataImport.class);
            util.exportExcel(response, list, "modbus配置数据");
        }
    }

    @ApiOperation(value = "导入模板")
    @PostMapping("/modbusTemplate")
    public void modbusTemplate(HttpServletResponse response, @RequestParam(name= "type") Integer type){
        if (1 == type){
            ExcelUtil<ModbusIoImport> excelUtil = new ExcelUtil<>(ModbusIoImport.class);
            excelUtil.importTemplateExcel(response,"MODBUS-IO");
        }else {
            ExcelUtil<ModbusDataImport> excelUtil = new ExcelUtil<>(ModbusDataImport.class);
            excelUtil.importTemplateExcel(response,"MODBUS-DATA");
        }
    }
}
