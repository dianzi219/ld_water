package com.fastbee.controller.firmware;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.fastbee.common.core.mq.ota.OtaUpgradeDelayTask;
import com.fastbee.iot.domain.FirmwareTaskDetail;
import com.fastbee.iot.model.FirmwareTaskDetailInput;
import com.fastbee.iot.model.FirmwareTaskDetailOutput;
import com.fastbee.iot.model.FirmwareTaskDeviceStatistic;
import com.fastbee.iot.model.FirmwareTaskInput;
import com.fastbee.iot.data.service.IFirmwareTaskDetailService;
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
import com.fastbee.iot.domain.FirmwareTask;
import com.fastbee.iot.data.service.IFirmwareTaskService;
import com.fastbee.common.utils.poi.ExcelUtil;
import com.fastbee.common.core.page.TableDataInfo;

/**
 * 固件升级任务 Controller
 *
 * @author kerwincui
 * @date 2022-10-26
 */
@Api(tags = "固件升级任务")
@RestController
@RequestMapping("/iot/firmware/task")
public class FirmwareTaskController extends BaseController
{
    @Autowired
    private IFirmwareTaskService firmwareTaskService;
    @Autowired
    private IFirmwareTaskDetailService firmwareTaskDetailService;

    /**
     * 查询固件升级任务列表
     */
    @ApiOperation("查询固件升级任务列表")
    @PreAuthorize("@ss.hasPermi('iot:task:list')")
    @GetMapping("/list")
    public TableDataInfo list(FirmwareTask firmwareTask)
    {
        startPage();
        List<FirmwareTask> list = firmwareTaskService.selectFirmwareTaskList(firmwareTask);
        return getDataTable(list);
    }

    /**
     * 根据固件id查询下属设备列表
     */
    @ApiOperation("根据固件id查询下属设备列表")
    @GetMapping("/deviceList")
    public TableDataInfo deviceList(FirmwareTaskDetailInput FirmwareTaskDetailInput) {
        startPage();
        List<FirmwareTaskDetailOutput> list = firmwareTaskDetailService.selectFirmwareTaskDetailListByFirmwareId(FirmwareTaskDetailInput);
        return getDataTable(list);
    }

    /**
     * 固件升级设备统计
     */
    @ApiOperation("固件升级设备统计")
    @GetMapping("/deviceStatistic")
    public AjaxResult deviceStatistic(FirmwareTaskDetailInput FirmwareTaskDetailInput) {
        List<FirmwareTaskDeviceStatistic> list = firmwareTaskDetailService.deviceStatistic(FirmwareTaskDetailInput);
        return AjaxResult.success(list);
    }

    /**
     * 导出固件升级任务列表
     */
    @ApiOperation("导出固件升级任务列表")
    @PreAuthorize("@ss.hasPermi('iot:task:export')")
    @Log(title = "固件升级任务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, FirmwareTask firmwareTask)
    {
        List<FirmwareTask> list = firmwareTaskService.selectFirmwareTaskList(firmwareTask);
        ExcelUtil<FirmwareTask> util = new ExcelUtil<FirmwareTask>(FirmwareTask.class);
        util.exportExcel(response, list, "固件升级任务数据");
    }

    /**
     * 获取固件升级任务详细信息
     */
    @ApiOperation("获取固件升级任务详细信息")
    @PreAuthorize("@ss.hasPermi('iot:task:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(firmwareTaskService.getById(id));
    }

    /**
     * 新增固件升级任务
     */
    @ApiOperation("新增固件升级任务")
    @PreAuthorize("@ss.hasPermi('iot:task:add')")
    @Log(title = "固件升级任务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody FirmwareTaskInput firmwareTaskInput) {
        Long taskId = firmwareTaskService.insertFirmwareTask(firmwareTaskInput);
        return AjaxResult.success(taskId);
    }

    /**
     * 修改固件升级任务
     */
    @ApiOperation("修改固件升级任务")
    @PreAuthorize("@ss.hasPermi('iot:task:edit')")
    @Log(title = "固件升级任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FirmwareTask firmwareTask)
    {
        return toAjax(firmwareTaskService.updateById(firmwareTask));
    }

    /**
     * 删除固件升级任务
     */
    @ApiOperation("删除固件升级任务")
    @PreAuthorize("@ss.hasPermi('iot:task:remove')")
    @Log(title = "固件升级任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(firmwareTaskService.deleteBatchByIds(Arrays.asList(ids)));
    }

    /**
     * 固件升级
     */
    @PostMapping("/upgrade")
    @PreAuthorize("@ss.hasPermi('iot:task:upgrade')")
    @ApiOperation(value = "固件升级", httpMethod = "POST", response = AjaxResult.class, notes = "固件升级")
    public AjaxResult upgrade(@RequestBody OtaUpgradeDelayTask task){
        firmwareTaskService.upgrade(task);
        return AjaxResult.success();
    }

    @GetMapping(value = "/upgrade/detail")
    @ApiOperation(value = "查询OTA升级详情列表")
    public TableDataInfo taskDetails(FirmwareTaskDetail detail){
        startPage();
        List<FirmwareTaskDetail> detailList = firmwareTaskDetailService.selectFirmwareTaskDetailList(detail);
        return getDataTable(detailList);
    }


}
