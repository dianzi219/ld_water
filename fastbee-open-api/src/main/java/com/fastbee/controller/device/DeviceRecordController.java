package com.fastbee.controller.device;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.iot.model.DeviceRecordVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fastbee.common.annotation.Log;
import com.fastbee.common.core.controller.BaseController;
import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.enums.BusinessType;
import com.fastbee.iot.domain.DeviceRecord;
import com.fastbee.iot.service.IDeviceRecordService;
import com.fastbee.common.utils.poi.ExcelUtil;
import com.fastbee.common.core.page.TableDataInfo;

/**
 * 设备记录Controller
 *
 * @author zhangzhiyi
 * @date 2024-07-16
 */
@RestController
@RequestMapping("/iot/record")
public class DeviceRecordController extends BaseController
{
    @Resource
    private IDeviceRecordService deviceRecordService;

    /**
     * 查询设备记录列表
     */
    @PreAuthorize("@ss.hasPermi('iot:device:record:list')")
    @GetMapping("/list")
    public TableDataInfo list(DeviceRecord deviceRecord)
    {
//        startPage();
        if (null == deviceRecord.getOperateDeptId()) {
            SysUser user = getLoginUser().getUser();
            deviceRecord.setTenantId(user.getDept().getDeptUserId());
        }
        Page<DeviceRecordVO> deviceRecordVOPage = deviceRecordService.selectDeviceRecordList(deviceRecord);
        return getDataTable(deviceRecordVOPage.getRecords(), deviceRecordVOPage.getTotal());
    }

    /**
     * 导出设备记录列表
     */
    @PreAuthorize("@ss.hasPermi('iot:device:record:export')")
    @Log(title = "设备记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, DeviceRecord deviceRecord)
    {
        Page<DeviceRecordVO> deviceRecordVOPage = deviceRecordService.selectDeviceRecordList(deviceRecord);
        ExcelUtil<DeviceRecordVO> util = new ExcelUtil<>(DeviceRecordVO.class);
        util.exportExcel(response, deviceRecordVOPage.getRecords(), "设备记录数据");
    }

    /**
     * 获取设备记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('iot:device:record:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(deviceRecordService.selectDeviceRecordById(id));
    }

    /**
     * 删除设备记录
     */
    @PreAuthorize("@ss.hasPermi('iot:device:record:remove')")
    @Log(title = "设备记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(deviceRecordService.deleteDeviceRecordByIds(ids));
    }
}
