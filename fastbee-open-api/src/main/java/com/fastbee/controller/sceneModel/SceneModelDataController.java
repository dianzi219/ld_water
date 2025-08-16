package com.fastbee.controller.sceneModel;

import com.fastbee.common.annotation.Log;
import com.fastbee.common.constant.HttpStatus;
import com.fastbee.common.core.controller.BaseController;
import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.core.page.TableDataExtendInfo;
import com.fastbee.common.core.page.TableDataInfo;
import com.fastbee.common.enums.BusinessType;
import com.fastbee.common.enums.scenemodel.SceneModelVariableTypeEnum;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.poi.ExcelUtil;
import com.fastbee.iot.domain.SceneModelData;
import com.fastbee.iot.model.scenemodel.SceneModelDataDTO;
import com.fastbee.iot.service.ISceneModelDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 【请填写功能名称】Controller
 *
 * @author kerwincui
 * @date 2024-05-21
 */
@Api(tags = "场景变量管理")
@RestController
@RequestMapping("/scene/modelData")
public class SceneModelDataController extends BaseController
{
    @Resource
    private ISceneModelDataService sceneModelDataService;

    /**
     * 查询所有变量列表
     */
    @PreAuthorize("@ss.hasPermi('scene:modelData:list')")
    @GetMapping("/list")
    @ApiOperation("查询所有变量列表")
    public TableDataInfo list(SceneModelData sceneModelData)
    {
        if (null == sceneModelData.getSceneModelId()) {
            throw new ServiceException("请传入场景管理id");
        }
        if (null == sceneModelData.getEnable()) {
            sceneModelData.setEnable(1);
        }
        startPage();
        List<SceneModelDataDTO> list = sceneModelDataService.selectSceneModelDataDTOList(sceneModelData);
        return getDataTable(list);
    }

    /**
     * 查询关联设备变量的列表
     */
    @PreAuthorize("@ss.hasPermi('scene:modelDeviceData:list')")
    @GetMapping("/listByType")
    @ApiOperation("查询关联设备变量的列表")
    public TableDataExtendInfo listByType(SceneModelData sceneModelData)
    {
        if (null == sceneModelData.getSceneModelId()
                && !SceneModelVariableTypeEnum.THINGS_MODEL.getType().equals(sceneModelData.getVariableType())) {
            throw new ServiceException("请传入场景管理id");
        }
        return sceneModelDataService.listByType(sceneModelData);
    }

    /**
     * 导出【请填写功能名称】列表
     */
    @PreAuthorize("@ss.hasPermi('scene:modelData:export')")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ApiOperation("导出场景变量列表")
    public void export(HttpServletResponse response, SceneModelData sceneModelData)
    {
        List<SceneModelDataDTO> list = sceneModelDataService.selectSceneModelDataDTOList(sceneModelData);
        ExcelUtil<SceneModelDataDTO> util = new ExcelUtil<>(SceneModelDataDTO.class);
        util.exportExcel(response, list, "【请填写功能名称】数据");
    }

    /**
     * 获取【请填写功能名称】详细信息
     */
    @PreAuthorize("@ss.hasPermi('scene:modelData:query')")
    @GetMapping(value = "/{id}")
    @ApiOperation("获取场景变量详情")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(sceneModelDataService.selectSceneModelDataById(id));
    }

    /**
     * 新增【请填写功能名称】
     */
    @PreAuthorize("@ss.hasPermi('scene:modelData:add')")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.INSERT)
    @PostMapping
    @ApiOperation("新增场景变量")
    public AjaxResult add(@RequestBody SceneModelData sceneModelData)
    {
        return toAjax(sceneModelDataService.insertSceneModelData(sceneModelData));
    }

    /**
     * 修改【请填写功能名称】
     */
    @PreAuthorize("@ss.hasPermi('scene:modelData:edit')")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.UPDATE)
    @PutMapping
    @ApiOperation("编辑场景变量")
    public AjaxResult edit(@RequestBody SceneModelData sceneModelData)
    {
        return toAjax(sceneModelDataService.updateSceneModelData(sceneModelData));
    }

    /**
     * 删除【请填写功能名称】
     */
    @PreAuthorize("@ss.hasPermi('scene:modelData:remove')")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    @ApiOperation("删除场景变量")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(sceneModelDataService.deleteSceneModelDataByIds(ids));
    }

    /**
     * 修改【请填写功能名称】
     */
    @PreAuthorize("@ss.hasPermi('scene:modelData:editEnable')")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.UPDATE)
    @PostMapping("/editEnable")
    @ApiOperation("编辑场景变量")
    public AjaxResult editEnable(@RequestBody SceneModelData sceneModelData)
    {
        return toAjax(sceneModelDataService.editEnable(sceneModelData));
    }
}
