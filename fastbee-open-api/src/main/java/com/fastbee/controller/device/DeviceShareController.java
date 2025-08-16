package com.fastbee.controller.device;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.fastbee.iot.domain.DeviceUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.fastbee.common.annotation.Log;
import com.fastbee.common.core.controller.BaseController;
import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.enums.BusinessType;
import com.fastbee.iot.domain.DeviceShare;
import com.fastbee.iot.service.IDeviceShareService;
import com.fastbee.common.utils.poi.ExcelUtil;
import com.fastbee.common.core.page.TableDataInfo;

/**
 * 设备分享Controller
 *
 * @author kerwincui
 * @date 2024-04-03
 */
@RestController
@RequestMapping("/iot/share")
public class DeviceShareController extends BaseController
{
    @Autowired
    private IDeviceShareService deviceShareService;

    /**
     * 查询设备分享列表
     */
    @PreAuthorize("@ss.hasPermi('iot:share:list')")
    @GetMapping("/list")
    public TableDataInfo list(DeviceShare deviceShare)
    {
        startPage();
        List<DeviceShare> list = deviceShareService.selectDeviceShareList(deviceShare);
        return getDataTable(list);
    }

    /**
     * 导出设备分享列表
     */
    @PreAuthorize("@ss.hasPermi('iot:share:export')")
    @Log(title = "设备分享", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, DeviceShare deviceShare)
    {
        List<DeviceShare> list = deviceShareService.selectDeviceShareList(deviceShare);
        ExcelUtil<DeviceShare> util = new ExcelUtil<DeviceShare>(DeviceShare.class);
        util.exportExcel(response, list, "设备分享数据");
    }

    /**
     * 获取设备分享详细信息
     */
    @PreAuthorize("@ss.hasPermi('iot:share:query')")
    @GetMapping(value = "/detail")
    public AjaxResult getInfo(Long deviceId,Long userId)
    {
        return success(deviceShareService.selectDeviceShareByDeviceIdAndUserId(deviceId,userId));
    }

    /**
     * 新增设备分享
     */
    @PreAuthorize("@ss.hasPermi('iot:share:add')")
    @Log(title = "设备分享", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody DeviceShare deviceShare)
    {
        return toAjax(deviceShareService.insertDeviceShare(deviceShare));
    }

    /**
     * 修改设备分享
     */
    @PreAuthorize("@ss.hasPermi('iot:share:edit')")
    @Log(title = "设备分享", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody DeviceShare deviceShare)
    {
        return toAjax(deviceShareService.updateDeviceShare(deviceShare));
    }

    /**
     * 删除设备分享
     */
    @PreAuthorize("@ss.hasPermi('iot:share:remove')")
    @Log(title = "设备分享", businessType = BusinessType.DELETE)
	@DeleteMapping("/{deviceIds}")
    public AjaxResult remove(@PathVariable Long[] deviceIds)
    {
        return toAjax(deviceShareService.deleteDeviceShareByDeviceIds(deviceIds));
    }

    /**
     * 删除设备分享
     */
    @PreAuthorize("@ss.hasPermi('iot:share:remove')")
    @Log(title = "设备分享", businessType = BusinessType.DELETE)
    @DeleteMapping()
    public AjaxResult delete(@RequestBody DeviceShare deviceShare)
    {
        return toAjax(deviceShareService.deleteDeviceShareByDeviceIdAndUserId(deviceShare));
    }


    /**
     * 获取设备分享用户信息
     */
    @GetMapping("/shareUser")
    @PreAuthorize("@ss.hasPermi('iot:share:user')")
    public AjaxResult userList(DeviceShare share)
    {
        return AjaxResult.success(deviceShareService.selectShareUser(share));
    }
}
