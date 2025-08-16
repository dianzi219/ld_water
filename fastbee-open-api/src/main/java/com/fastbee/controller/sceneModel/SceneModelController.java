package com.fastbee.controller.sceneModel;

import com.fastbee.common.annotation.Log;
import com.fastbee.common.core.controller.BaseController;
import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.core.page.TableDataInfo;
import com.fastbee.common.enums.BusinessType;
import com.fastbee.common.utils.poi.ExcelUtil;
import com.fastbee.iot.domain.SceneModel;
import com.fastbee.iot.service.ISceneModelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 场景管理Controller
 *
 * @author kerwincui
 * @date 2024-05-20
 */
@RestController
@RequestMapping("/scene/model")
@Api(tags = "场景管理")
public class SceneModelController extends BaseController
{
    @Resource
    private ISceneModelService sceneModelService;

    /**
     * 查询场景管理列表
     */
    @PreAuthorize("@ss.hasPermi('scene:model:list')")
    @GetMapping("/list")
    @ApiOperation("查询场景管理列表")
    public TableDataInfo list(SceneModel sceneModel)
    {
        startPage();
        List<SceneModel> list = sceneModelService.selectSceneModelList(sceneModel);
        return getDataTable(list);
    }

    /**
     * 导出场景管理列表
     */
    @PreAuthorize("@ss.hasPermi('scene:model:export')")
    @Log(title = "场景管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ApiOperation("导出场景管理列表")
    public void export(HttpServletResponse response, SceneModel sceneModel)
    {
        List<SceneModel> list = sceneModelService.selectSceneModelList(sceneModel);
        ExcelUtil<SceneModel> util = new ExcelUtil<SceneModel>(SceneModel.class);
        util.exportExcel(response, list, "场景管理数据");
    }

    /**
     * 获取场景管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('scene:model:query')")
    @GetMapping(value = "/{sceneModelId}")
    @ApiOperation("获取场景管理详细信息")
    public AjaxResult getInfo(@PathVariable("sceneModelId") Long sceneModelId)
    {
        return success(sceneModelService.selectSceneModelBySceneModelId(sceneModelId));
    }

    /**
     * 新增场景管理
     */
    @PreAuthorize("@ss.hasPermi('scene:model:add')")
    @Log(title = "场景管理", businessType = BusinessType.INSERT)
    @PostMapping
    @ApiOperation("新增场景管理")
    public AjaxResult add(@RequestBody SceneModel sceneModel)
    {
        return toAjax(sceneModelService.insertSceneModel(sceneModel));
    }

    /**
     * 修改场景管理
     */
    @PreAuthorize("@ss.hasPermi('scene:model:edit')")
    @Log(title = "场景管理", businessType = BusinessType.UPDATE)
    @PutMapping
    @ApiOperation("修改场景管理")
    public AjaxResult edit(@RequestBody SceneModel sceneModel)
    {
        return toAjax(sceneModelService.updateSceneModel(sceneModel));
    }

    /**
     * 删除场景管理
     */
    @PreAuthorize("@ss.hasPermi('scene:model:remove')")
    @Log(title = "场景管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{sceneModelIds}")
    @ApiOperation("删除场景管理")
    public AjaxResult remove(@PathVariable Long[] sceneModelIds)
    {
        return toAjax(sceneModelService.deleteSceneModelBySceneModelIds(sceneModelIds));
    }
}
