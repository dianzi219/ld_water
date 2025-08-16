package com.fastbee.controller.device;

import com.fastbee.common.annotation.Log;
import com.fastbee.common.constant.HttpStatus;
import com.fastbee.common.core.controller.BaseController;
import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.core.domain.entity.SysRole;
import com.fastbee.common.core.domain.model.LoginUser;
import com.fastbee.common.core.page.TableDataInfo;
import com.fastbee.common.enums.BusinessType;
import com.fastbee.common.utils.MessageUtils;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.SecurityUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.common.utils.poi.ExcelUtil;
import com.fastbee.iot.domain.Device;
import com.fastbee.iot.model.DeviceAssignmentVO;
import com.fastbee.iot.model.DeviceImportVO;
import com.fastbee.iot.model.DeviceRelateUserInput;
import com.fastbee.iot.model.SerialNumberVO;
import com.fastbee.iot.service.IDeviceService;
import com.fastbee.iot.model.dto.ThingsModelDTO;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.CollectionUtils;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 设备Controller
 *
 * @author kerwincui
 * @date 2021-12-16
 */
@Api(tags = "设备管理")
@RestController
@RequestMapping("/iot/device")
public class DeviceController extends BaseController
{
    @Autowired
    private IDeviceService deviceService;


    /**
     * 查询设备列表
     */
    @PreAuthorize("@ss.hasPermi('iot:device:list')")
    @GetMapping("/list")
    @ApiOperation("设备分页列表")
    public TableDataInfo list(Device device)
    {
        startPage();
        // 限制当前用户机构
        if (null == device.getDeptId()) {
            device.setDeptId(getLoginUser().getDeptId());
        }
        return getDataTable(deviceService.selectDeviceList(device));
    }

    /**
     * 查询未分配授权码设备列表
     */
    @PreAuthorize("@ss.hasPermi('iot:device:list')")
    @GetMapping("/unAuthlist")
    @ApiOperation("设备分页列表")
    public TableDataInfo unAuthlist(Device device)
    {
        startPage();
        if (null == device.getDeptId()) {
            device.setDeptId(getLoginUser().getDeptId());
        }
        return getDataTable(deviceService.selectUnAuthDeviceList(device));
    }

    /**
     * 查询分组可添加设备
     */
    @PreAuthorize("@ss.hasPermi('iot:device:list')")
    @GetMapping("/listByGroup")
    @ApiOperation("查询分组可添加设备分页列表")
    public TableDataInfo listByGroup(Device device)
    {
        startPage();
        LoginUser loginUser = getLoginUser();
        if (null == loginUser.getDeptId()) {
            device.setTenantId(loginUser.getUserId());
            return getDataTable(deviceService.listTerminalUserByGroup(device));
        }
        if (null == device.getDeptId()) {
            device.setDeptId(getLoginUser().getDeptId());
        }
        return getDataTable(deviceService.selectDeviceListByGroup(device));
    }

    /**
     * 查询设备简短列表，主页列表数据
     */
    @PreAuthorize("@ss.hasPermi('iot:device:list')")
    @GetMapping("/shortList")
    @ApiOperation("设备分页简短列表")
    public TableDataInfo shortList(Device device)
    {
        startPage();
        LoginUser loginUser = getLoginUser();
        if (null == loginUser.getDeptId()) {
            // 终端用户查询设备
            device.setTenantId(loginUser.getUserId());
            return getDataTable(deviceService.listTerminalUser(device));
        }
        if (null == device.getDeptId()) {
            device.setDeptId(getLoginUser().getDeptId());
        }
        if (Objects.isNull(device.getTenantId())){
            device.setTenantId(getLoginUser().getUserId());
        }
        if (null == device.getShowChild()) {
            device.setShowChild(false);
        }
        return getDataTable(deviceService.selectDeviceShortList(device));
    }

    /**
     * 查询所有设备简短列表
     */
    @PreAuthorize("@ss.hasPermi('iot:device:list')")
    @GetMapping("/all")
    @ApiOperation("查询所有设备简短列表")
    public TableDataInfo allShortList(Device device)
    {
        Device queryDevice = new Device();
        // 兼容组态分享多租户
        if (null == device || null == device.getDeptId()) {
            queryDevice.setDeptId(SecurityUtils.getLoginUser().getUser().getDeptId());
        } else {
            queryDevice.setDeptId(device.getDeptId());
        }
        queryDevice.setShowChild(true);
        return getDataTable(deviceService.selectAllDeviceShortList(queryDevice));
    }

    /**
     * 导出设备列表
     */
    @PreAuthorize("@ss.hasPermi('iot:device:export')")
    @Log(title = "设备", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ApiOperation("导出设备")
    public void export(HttpServletResponse response, Device device)
    {
        List<Device> list = deviceService.selectDeviceList(device);
        ExcelUtil<Device> util = new ExcelUtil<Device>(Device.class);
        util.exportExcel(response, list, "设备数据");
    }

    /**
     * 获取设备详细信息
     */
    @PreAuthorize("@ss.hasPermi('iot:device:query')")
    @GetMapping(value = "/{deviceId}")
    @ApiOperation("获取设备详情")
    public AjaxResult getInfo(@PathVariable("deviceId") Long deviceId)
    {
        Device device = deviceService.selectDeviceByDeviceId(deviceId);
        // 判断当前用户是否有设备分享权限 （设备所属机构管理员和设备所属用户有权限）
        LoginUser loginUser = getLoginUser();
        List<SysRole> roles = loginUser.getUser().getRoles();
        //判断当前用户是否为设备所属机构管理员
        if(roles.stream().anyMatch(a-> "admin".equals(a.getRoleKey()))){
            device.setIsOwner(1);
        } else {
            //判断当前用户是否是设备所属用户
            if (Objects.equals(device.getTenantId(), loginUser.getUserId())){
                device.setIsOwner(1);
            }else {
                device.setIsOwner(0);
            }
        }
        return AjaxResult.success(device);
    }

    /**
     * 根据设备编号详细信息
     */
    @PreAuthorize("@ss.hasPermi('iot:device:query')")
    @GetMapping(value = "/getDeviceBySerialNumber/{serialNumber}")
    @ApiOperation("根据设备编号获取设备详情")
    public AjaxResult getInfoBySerialNumber(@PathVariable("serialNumber") String serialNumber)
    {
        return AjaxResult.success(deviceService.selectDeviceBySerialNumber(serialNumber));
    }

    /**
     * 获取设备统计信息
     */
    @PreAuthorize("@ss.hasPermi('iot:device:query')")
    @GetMapping(value = "/statistic")
    @ApiOperation("获取设备统计信息")
    public AjaxResult getDeviceStatistic()
    {
        return AjaxResult.success(deviceService.selectDeviceStatistic());
    }

    /**
     * 获取设备详细信息
     */
    @PreAuthorize("@ss.hasPermi('iot:device:query')")
    @GetMapping(value = "/runningStatus")
    @ApiOperation("获取设备详情和运行状态")
    public AjaxResult getRunningStatusInfo(Long deviceId)
    {
        return AjaxResult.success(deviceService.selectDeviceRunningStatusByDeviceId(deviceId));
    }

    /**
     * 新增设备
     */
    @PreAuthorize("@ss.hasPermi('iot:device:add')")
    @Log(title = "添加设备", businessType = BusinessType.INSERT)
    @PostMapping
    @ApiOperation("添加设备")
    public AjaxResult add(@RequestBody Device device)
    {
        return AjaxResult.success(deviceService.insertDevice(device));
    }

    /**
     * TODO --APP
     * 终端用户绑定设备
     */
    @PreAuthorize("@ss.hasPermi('iot:device:add')")
    @Log(title = "设备关联用户", businessType = BusinessType.UPDATE)
    @PostMapping("/relateUser")
    @ApiOperation("终端-设备关联用户")
    public AjaxResult relateUser(@RequestBody DeviceRelateUserInput deviceRelateUserInput)
    {
        if(deviceRelateUserInput.getUserId()==0 || deviceRelateUserInput.getUserId()==null){
            return AjaxResult.error(MessageUtils.message("device.user.id.null"));
        }
        if(deviceRelateUserInput.getDeviceNumberAndProductIds()==null || deviceRelateUserInput.getDeviceNumberAndProductIds().size()==0){
            return AjaxResult.error(MessageUtils.message("device.product.id.null"));
        }
        return deviceService.deviceRelateUser(deviceRelateUserInput);
    }

    /**
     * 修改设备
     */
    @PreAuthorize("@ss.hasPermi('iot:device:edit')")
    @Log(title = "修改设备", businessType = BusinessType.UPDATE)
    @PutMapping
    @ApiOperation("修改设备")
    public AjaxResult edit(@RequestBody Device device)
    {
        return deviceService.updateDevice(device);
    }

    /**
     * 重置设备状态
     */
    @PreAuthorize("@ss.hasPermi('iot:device:edit')")
    @Log(title = "重置设备状态", businessType = BusinessType.UPDATE)
    @PutMapping("/reset/{serialNumber}")
    @ApiOperation("重置设备状态")
    public AjaxResult resetDeviceStatus(@PathVariable String serialNumber)
    {
        Device device=new Device();
        device.setSerialNumber(serialNumber);
        return toAjax(deviceService.resetDeviceStatus(device.getSerialNumber()));
    }

    /**
     * 删除设备
     */
    @PreAuthorize("@ss.hasPermi('iot:device:remove')")
    @Log(title = "删除设备", businessType = BusinessType.DELETE)
	@DeleteMapping("/{deviceIds}")
    @ApiOperation("批量删除设备")
    public AjaxResult remove(@PathVariable Long[] deviceIds) throws SchedulerException {
        return deviceService.deleteDeviceByDeviceId(deviceIds[0]);
    }

    /**
     * 生成设备编号
     */
    @PreAuthorize("@ss.hasPermi('iot:device:add')")
    @GetMapping("/generator")
    @ApiOperation("生成设备编号")
    public AjaxResult generatorDeviceNum(Integer type){
        return AjaxResult.success(MessageUtils.message("operate.success"),deviceService.generationDeviceNum(type));
    }

    /**
     * 获取设备MQTT连接参数
     * @param deviceId 设备主键id
     * @return
     */
    @PreAuthorize("@ss.hasPermi('iot:device:query')")
    @GetMapping("/getMqttConnectData")
    @ApiOperation("获取设备MQTT连接参数")
    public AjaxResult getMqttConnectData(Long deviceId){
        return AjaxResult.success(deviceService.getMqttConnectData(deviceId));
    }

    @PreAuthorize("@ss.hasPermi('iot:device:add')")
    @ApiOperation("下载设备导入模板")
    @PostMapping("/uploadTemplate")
    public void uploadTemplate(HttpServletResponse response, @RequestParam(name = "type") Integer type)
    {
        // 1-设备导入；2-设备分配
        if (1 == type) {
            ExcelUtil<DeviceImportVO> util = new ExcelUtil<>(DeviceImportVO.class);
            util.importTemplateExcel(response, "设备导入");
        } else if (2 == type) {
            ExcelUtil<DeviceAssignmentVO> util = new ExcelUtil<>(DeviceAssignmentVO.class);
            util.importTemplateExcel(response, "设备分配");
        }
    }

    @PreAuthorize("@ss.hasPermi('iot:device:add')")
    @ApiOperation("批量导入设备")
    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(@RequestParam("file") MultipartFile file, @RequestParam("productId") Long productId) throws Exception
    {
        if (null == file) {
            return error(MessageUtils.message("import.failed.file.null"));
        }
        ExcelUtil<DeviceImportVO> util = new ExcelUtil<>(DeviceImportVO.class);
        List<DeviceImportVO> deviceImportVOList = util.importExcel(file.getInputStream());
        if (CollectionUtils.isEmpty(deviceImportVOList)) {
            return error(MessageUtils.message("import.failed.data.null"));
        }
        DeviceImportVO deviceImportVO = deviceImportVOList.stream().filter(d -> StringUtils.isEmpty(d.getDeviceName())).findAny().orElse(null);
        if (null != deviceImportVO) {
            return error(MessageUtils.message("import.failed.device.name.null"));
        }
        String message = deviceService.importDevice(deviceImportVOList, productId);
        return StringUtils.isEmpty(message) ? success(MessageUtils.message("import.success")) : error(message);
    }

    @PreAuthorize("@ss.hasPermi('iot:device:assignment')")
    @ApiOperation("批量导入分配设备")
    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
    @PostMapping("/importAssignmentData")
    public AjaxResult importAssignmentData(@RequestParam("file") MultipartFile file,
                                           @RequestParam("productId") Long productId,
                                           @RequestParam("deptId") Long deptId) throws Exception
    {
        if (null == file) {
            return error(MessageUtils.message("import.failed.file.null"));
        }
        ExcelUtil<DeviceAssignmentVO> util = new ExcelUtil<>(DeviceAssignmentVO.class);
        List<DeviceAssignmentVO> deviceAssignmentVOS = util.importExcel(file.getInputStream());
        if (CollectionUtils.isEmpty(deviceAssignmentVOS)) {
            return error(MessageUtils.message("import.failed.device.name.null"));
        }
        DeviceAssignmentVO deviceAssignmentVO = deviceAssignmentVOS.stream().filter(d -> StringUtils.isEmpty(d.getDeviceName())).findAny().orElse(null);
        if (null != deviceAssignmentVO) {
            return error(MessageUtils.message("import.failed.device.name.null"));
        }
        String message = deviceService.importAssignmentDevice(deviceAssignmentVOS, productId, deptId);
        return StringUtils.isEmpty(message) ? success(MessageUtils.message("import.success")) : error(message);
    }

    /**
     * 分配设备
     * @param deptId 机构id
     * @param: deviceIds 设备id字符串
     * @return com.fastbee.common.core.domain.AjaxResult
     */
    @PreAuthorize("@ss.hasPermi('iot:device:assignment')")
    @ApiOperation("分配设备")
    @PostMapping("/assignment")
    public AjaxResult assignment(@RequestParam("deptId") Long deptId,
                                 @RequestParam("deviceIds") String deviceIds) {
        if (null == deptId) {
            return error(MessageUtils.message("device.dept.id.null"));
        }
        if (StringUtils.isEmpty(deviceIds)) {
            return error(MessageUtils.message("device.id.null"));
        }
        return deviceService.assignment(deptId, deviceIds);
    }

    /**
     * 回收设备
     * @param: deviceIds 设备id字符串
     * @return com.fastbee.common.core.domain.AjaxResult
     */
    @PreAuthorize("@ss.hasPermi('iot:device:recovery')")
    @ApiOperation("回收设备")
    @PostMapping("/recovery")
    public AjaxResult recovery(@RequestParam("deviceIds") String deviceIds,
                               @RequestParam("recoveryDeptId") Long recoveryDeptId) {
        if (StringUtils.isEmpty(deviceIds)) {
            return error("请选择设备");
        }
        return deviceService.recovery(deviceIds, recoveryDeptId);
    }

    /**
     * 批量生成设备编号
     */
    @PreAuthorize("@ss.hasPermi('iot:device:batchGenerator')")
    @PostMapping("/batchGenerator")
    @ApiOperation("批量生成设备编号")
    public void batchGeneratorDeviceNum(HttpServletResponse response,
                                              @RequestParam("count") Integer count){
        if (count > 200) {
            throw new ServiceException("最多只能生成200个！");
        }
        List<SerialNumberVO> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            SerialNumberVO serialNumberVO = new SerialNumberVO();
            String serialNumber = deviceService.generationDeviceNum(1);
            serialNumberVO.setSerialNumber(serialNumber);
            list.add(serialNumberVO);
        }
        ExcelUtil<SerialNumberVO> util = new ExcelUtil<>(SerialNumberVO.class);
        util.exportExcel(response, list, "设备编号");
    }

    /**
     * 查询变量概况
     */
    @PreAuthorize("@ss.hasPermi('iot:device:query')")
    @GetMapping("/listThingsModel")
    @ApiOperation("查询变量概况")
    public TableDataInfo listThingsModel(Integer pageNum, Integer pageSize, Long deviceId,  String modelName, Integer type)
    {
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("查询成功");
        List<ThingsModelDTO> thingsModelDTOList = deviceService.listThingsModel(deviceId);
        if (CollectionUtils.isEmpty(thingsModelDTOList)) {
            rspData.setRows(thingsModelDTOList);
            rspData.setTotal(new PageInfo(thingsModelDTOList).getTotal());
            return rspData;
        }
        List<ThingsModelDTO> list1;
        if (StringUtils.isNotEmpty(modelName)) {
            list1 = thingsModelDTOList.stream().filter(t -> t.getModelName().contains(modelName)).collect(Collectors.toList());
        } else {
            list1 = thingsModelDTOList;
        }
        List<ThingsModelDTO> list2;
        if (null != type) {
            list2 = list1.stream().filter(t -> t.getType().equals(type)).collect(Collectors.toList());
        } else {
            list2 = list1;
        }
        if (CollectionUtils.isNotEmpty(list2)) {
            List resultList = com.fastbee.common.utils.collection.CollectionUtils.startPage(list2, pageNum, pageSize);
            rspData.setRows(resultList);
        } else {
            rspData.setRows(new ArrayList<>());
        }
        rspData.setTotal(new PageInfo(list2).getTotal());
        return rspData;
    }

}
