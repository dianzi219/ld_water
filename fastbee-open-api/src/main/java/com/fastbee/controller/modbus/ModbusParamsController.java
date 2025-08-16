package com.fastbee.controller.modbus;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fastbee.common.annotation.Log;
import com.fastbee.common.core.controller.BaseController;
import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.enums.BusinessType;
import com.fastbee.iot.domain.ModbusParams;
import com.fastbee.iot.service.IModbusParamsService;
import com.fastbee.common.utils.poi.ExcelUtil;
import com.fastbee.common.core.page.TableDataInfo;

/**
 * 产品modbus配置参数Controller
 *
 * @author kerwincui
 * @date 2024-05-31
 */
@RestController
@RequestMapping("/modbus/params")
@Api(tags = "modbus配置参数")
public class ModbusParamsController extends BaseController
{
    @Autowired
    private IModbusParamsService modbusParamsService;

/**
 * 查询产品modbus配置参数列表
 */
@PreAuthorize("@ss.hasPermi('modbus:params:list')")
@GetMapping("/list")
@ApiOperation("查询产品modbus配置参数列表")
    public TableDataInfo list(ModbusParams modbusParams)
    {
        startPage();
        List<ModbusParams> list = modbusParamsService.selectModbusParamsList(modbusParams);
        return getDataTable(list);
    }

    /**
     * 导出产品modbus配置参数列表
     */
    @ApiOperation("导出产品modbus配置参数列表")
    @PreAuthorize("@ss.hasPermi('modbus:params:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, ModbusParams modbusParams)
    {
        List<ModbusParams> list = modbusParamsService.selectModbusParamsList(modbusParams);
        ExcelUtil<ModbusParams> util = new ExcelUtil<ModbusParams>(ModbusParams.class);
        util.exportExcel(response, list, "产品modbus配置参数数据");
    }

    /**
     * 获取产品modbus配置参数详细信息
     */
    @PreAuthorize("@ss.hasPermi('modbus:params:query')")
    @GetMapping(value = "/{id}")
    @ApiOperation("获取产品modbus配置参数详细信息")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(modbusParamsService.selectModbusParamsById(id));
    }

    /**
     * 新增产品modbus配置参数
     */
    @PreAuthorize("@ss.hasPermi('modbus:params:add')")
    @PostMapping("/addOrUpdate")
    @ApiOperation("新增产品modbus配置参数")
    public AjaxResult add(@RequestBody ModbusParams modbusParams)
    {
        return toAjax(modbusParamsService.addOrUpdate(modbusParams));
    }

    /**
     * 删除产品modbus配置参数
     */
    @PreAuthorize("@ss.hasPermi('modbus:params:remove')")
    @DeleteMapping("/{ids}")
    @ApiOperation("删除产品modbus配置参数")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(modbusParamsService.removeBatchByIds(Arrays.asList(ids)));
    }

    /**
     * 根据产品io获取modbus配置
     */
    @PreAuthorize("@ss.hasPermi('modbus:params:query')")
    @GetMapping(value = "/getByProductId")
    @ApiOperation("根据产品io获取modbus配置")
    public AjaxResult getByProductId(Long productId)
    {
        return success(modbusParamsService.getModbusParamsByProductId(productId));
    }
}
