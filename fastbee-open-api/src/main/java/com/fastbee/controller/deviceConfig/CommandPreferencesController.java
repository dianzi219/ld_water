package com.fastbee.controller.deviceConfig;

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
import com.fastbee.iot.domain.CommandPreferences;
import com.fastbee.iot.service.ICommandPreferencesService;
import com.fastbee.common.utils.poi.ExcelUtil;
import com.fastbee.common.core.page.TableDataInfo;

/**
 * 指令偏好设置Controller
 *
 * @author kerwincui
 * @date 2024-06-29
 */
@RestController
@RequestMapping("/iot/preferences")
@Api(tags = "指令偏好设置")
public class CommandPreferencesController extends BaseController
{
    @Autowired
    private ICommandPreferencesService commandPreferencesService;

/**
 * 查询指令偏好设置列表
 */
@PreAuthorize("@ss.hasPermi('order:preferences:list')")
@GetMapping("/list")
@ApiOperation("查询指令偏好设置列表")
    public TableDataInfo list(CommandPreferences commandPreferences)
    {
        startPage();
        List<CommandPreferences> list = commandPreferencesService.selectCommandPreferencesList(commandPreferences);
        return getDataTable(list);
    }

    /**
     * 导出指令偏好设置列表
     */
    @ApiOperation("导出指令偏好设置列表")
    @PreAuthorize("@ss.hasPermi('order:preferences:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, CommandPreferences commandPreferences)
    {
        List<CommandPreferences> list = commandPreferencesService.selectCommandPreferencesList(commandPreferences);
        ExcelUtil<CommandPreferences> util = new ExcelUtil<CommandPreferences>(CommandPreferences.class);
        util.exportExcel(response, list, "指令偏好设置数据");
    }

    /**
     * 获取指令偏好设置详细信息
     */
    @PreAuthorize("@ss.hasPermi('order:preferences:query')")
    @GetMapping(value = "/{id}")
    @ApiOperation("获取指令偏好设置详细信息")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(commandPreferencesService.selectCommandPreferencesById(id));
    }

    /**
     * 新增指令偏好设置
     */
    @PreAuthorize("@ss.hasPermi('order:preferences:add')")
    @PostMapping
    @ApiOperation("新增指令偏好设置")
    public AjaxResult add(@RequestBody CommandPreferences commandPreferences)
    {
        return toAjax(commandPreferencesService.insertCommandPreferences(commandPreferences));
    }

    /**
     * 修改指令偏好设置
     */
    @PreAuthorize("@ss.hasPermi('order:preferences:edit')")
    @PutMapping
    @ApiOperation("修改指令偏好设置")
    public AjaxResult edit(@RequestBody CommandPreferences commandPreferences)
    {
        return toAjax(commandPreferencesService.updateCommandPreferences(commandPreferences));
    }

    /**
     * 删除指令偏好设置
     */
    @PreAuthorize("@ss.hasPermi('order:preferences:remove')")
    @DeleteMapping("/{ids}")
    @ApiOperation("删除指令偏好设置")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(commandPreferencesService.deleteCommandPreferencesByIds(ids));
    }
}
