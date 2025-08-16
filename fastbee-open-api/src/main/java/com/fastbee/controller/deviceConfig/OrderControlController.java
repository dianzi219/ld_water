package com.fastbee.controller.deviceConfig;

import com.fastbee.common.core.controller.BaseController;
import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.core.page.TableDataInfo;
import com.fastbee.common.utils.poi.ExcelUtil;
import com.fastbee.iot.domain.OrderControl;
import com.fastbee.iot.service.IOrderControlService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 指令权限控制Controller
 *
 * @author kerwincui
 * @date 2024-07-01
 */
@RestController
@RequestMapping("/order/control")
@Api(tags = "指令权限控制")
public class OrderControlController extends BaseController
{
    @Autowired
    private IOrderControlService orderControlService;

/**
 * 查询指令权限控制列表
 */
@PreAuthorize("@ss.hasPermi('order:control:list')")
@GetMapping("/list")
@ApiOperation("查询指令权限控制列表")
    public TableDataInfo list(OrderControl orderControl)
    {
        startPage();
        List<OrderControl> list = orderControlService.selectOrderControlList(orderControl);
        return getDataTable(list);
    }

    /**
     * 导出指令权限控制列表
     */
    @ApiOperation("导出指令权限控制列表")
    @PreAuthorize("@ss.hasPermi('order:control:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, OrderControl orderControl)
    {
        List<OrderControl> list = orderControlService.selectOrderControlList(orderControl);
        ExcelUtil<OrderControl> util = new ExcelUtil<OrderControl>(OrderControl.class);
        util.exportExcel(response, list, "指令权限控制数据");
    }

    /**
     * 获取指令权限控制详细信息
     */
    @PreAuthorize("@ss.hasPermi('order:control:query')")
    @GetMapping(value = "/{id}")
    @ApiOperation("获取指令权限控制详细信息")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(orderControlService.selectOrderControlById(id));
    }

    /**
     * 新增指令权限控制
     */
    @PreAuthorize("@ss.hasPermi('order:control:add')")
    @PostMapping
    @ApiOperation("新增指令权限控制")
    public AjaxResult add(@RequestBody OrderControl orderControl)
    {
        return toAjax(orderControlService.insertOrderControl(orderControl));
    }

    /**
     * 修改指令权限控制
     */
    @PreAuthorize("@ss.hasPermi('order:control:edit')")
    @PutMapping
    @ApiOperation("修改指令权限控制")
    public AjaxResult edit(@RequestBody OrderControl orderControl)
    {
        return toAjax(orderControlService.updateOrderControl(orderControl));
    }

    /**
     * 删除指令权限控制
     */
    @PreAuthorize("@ss.hasPermi('order:control:remove')")
    @DeleteMapping("/{ids}")
    @ApiOperation("删除指令权限控制")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(orderControlService.deleteOrderControlByIds(ids));
    }

    @GetMapping(value = "/get")
    @ApiOperation("获取指令权限")
    public AjaxResult getControl(Long deviceId,Long modelId)
    {
        return orderControlService.judgeThingsModel(deviceId, modelId);
    }
}
